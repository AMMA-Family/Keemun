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
import kotlinx.coroutines.flow.collect
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
class Connector<Model : Parcelable, Msg : Any, Props : Any>(
    createFeature: (CoroutineScope, Model?) -> Feature<Model, Msg>,
    private val viewState: ViewState<Model, Props>,
    savedStateHandle: SavedStateHandle
) : ViewModel(), Feature<Props, Msg> {
    private val feature: Feature<Model, Msg>

    override val scope: CoroutineScope get() = viewModelScope

    @OptIn(ExperimentalCoroutinesApi::class)
    override val states: Flow<Props>
        get() = feature.states.mapLatest(viewState::invoke).flowOn(Dispatchers.Default)

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
    class Factory<Model : Parcelable, Msg : Any, Props : Any>(
        owner: SavedStateRegistryOwner,
        defaultArgs: Bundle? = null,
        private val feature: (CoroutineScope, Model?) -> Feature<Model, Msg>,
        private val viewState: ViewState<Model, Props>,
    ) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
        @Suppress(names = ["UNCHECKED_CAST"])
        override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T =
            Connector(feature, viewState, handle) as T
    }

    private companion object {
        private const val MODEL_KEY = "android_connector"
    }
}

/** Render [State] with [lifecycleState]. */
@OptIn(FlowPreview::class)
inline fun <State : Any, Msg : Any> Feature<State, Msg>.render(
    fragment: Fragment,
    debounceTime: Long = 0,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline block: suspend (State) -> Unit
): Job =
    states
        .let { if (debounceTime > 0) it.debounce(debounceTime) else it }
        .collectWithLifecycle(fragment.viewLifecycleOwner, lifecycleState, block)

/** Collecting [T] with [lifecycleState] (without [Lifecycle.State.DESTROYED]). */
inline fun <T> Flow<T>.collectWithLifecycle(
    lifecycleOwner: LifecycleOwner,
    lifecycleState: Lifecycle.State,
    crossinline block: suspend (T) -> Unit
): Job {
    val collectBlock: suspend CoroutineScope.() -> Unit = { collect(block) }
    return when (lifecycleState) {
        Lifecycle.State.INITIALIZED -> lifecycleOwner.lifecycleScope.launch(block = collectBlock)
        Lifecycle.State.CREATED -> lifecycleOwner.lifecycleScope.launchWhenCreated(collectBlock)
        Lifecycle.State.STARTED -> lifecycleOwner.lifecycleScope.launchWhenStarted(collectBlock)
        Lifecycle.State.RESUMED -> lifecycleOwner.lifecycleScope.launchWhenResumed(collectBlock)
        Lifecycle.State.DESTROYED -> error("The block cannot be launched in the DESTROYED state")
    }
}
