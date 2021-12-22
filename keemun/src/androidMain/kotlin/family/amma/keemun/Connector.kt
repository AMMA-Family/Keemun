package family.amma.keemun

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import family.amma.keemun.feature.Feature

/**
 * Wrapper for [Feature] with saving state and lifecycle handling.
 */
class Connector<State : Parcelable, Msg : Any, ViewState : Any>(
    createFeature: (CoroutineScope, State?) -> Feature<State, Msg>,
    private val stateTransform: StateTransform<State, ViewState>,
    savedStateHandle: SavedStateHandle
) : ViewModel(), Feature<ViewState, Msg> {
    private val feature: Feature<State, Msg>

    override val scope: CoroutineScope get() = viewModelScope

    @OptIn(ExperimentalCoroutinesApi::class)
    override val states: Flow<ViewState>
        get() = feature.states.mapLatest(stateTransform::invoke).flowOn(Dispatchers.Default)

    init {
        feature = createFeature(scope, savedStateHandle.get(MODEL_KEY))
        scope.launch {
            feature.states.collect {
                savedStateHandle.set(MODEL_KEY, it)
            }
        }
    }

    override infix fun dispatch(msg: Msg) = feature dispatch msg

    override suspend fun syncDispatch(msg: Msg) = feature syncDispatch msg

    /** ViewModelFactory for passing [feature] to [Connector]. */
    class Factory<State : Parcelable, Msg : Any, ViewState : Any>(
        owner: SavedStateRegistryOwner,
        defaultArgs: Bundle? = null,
        private val feature: (CoroutineScope, State?) -> Feature<State, Msg>,
        private val getStateTransform: () -> StateTransform<State, ViewState>,
    ) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
        @Suppress(names = ["UNCHECKED_CAST"])
        override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T =
            Connector(feature, getStateTransform(), handle) as T
    }

    private companion object {
        private const val MODEL_KEY = "android_connector"
    }
}

/** Render [State] with [lifecycleState]. */
@OptIn(FlowPreview::class)
fun <State : Any, Msg : Any> Feature<State, Msg>.render(
    fragment: Fragment,
    debounceTime: Long = 0,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    flowCollector: FlowCollector<State>
): Job =
    states
        .let { if (debounceTime > 0) it.debounce(debounceTime) else it }
        .collectWithLifecycle(fragment.viewLifecycleOwner, lifecycleState, flowCollector)

/** Collecting [T] with [lifecycleState] (without [Lifecycle.State.DESTROYED]). */
fun <T> Flow<T>.collectWithLifecycle(
    lifecycleOwner: LifecycleOwner,
    lifecycleState: Lifecycle.State,
    flowCollector: FlowCollector<T>
): Job {
    val collectBlock: suspend CoroutineScope.() -> Unit = { collect(flowCollector) }
    return when (lifecycleState) {
        Lifecycle.State.INITIALIZED -> lifecycleOwner.lifecycleScope.launch(block = collectBlock)
        Lifecycle.State.CREATED -> lifecycleOwner.lifecycleScope.launchWhenCreated(collectBlock)
        Lifecycle.State.STARTED -> lifecycleOwner.lifecycleScope.launchWhenStarted(collectBlock)
        Lifecycle.State.RESUMED -> lifecycleOwner.lifecycleScope.launchWhenResumed(collectBlock)
        Lifecycle.State.DESTROYED -> error("The block cannot be launched in the DESTROYED state")
    }
}
