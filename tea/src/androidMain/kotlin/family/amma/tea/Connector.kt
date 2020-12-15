package family.amma.tea

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
import family.amma.tea.feature.Feature
import family.amma.tea.feature.FeatureParams
import family.amma.tea.feature.TeaFeature
import kotlinx.coroutines.Dispatchers

/**
 * Wrapper for [Feature] with saving state and lifecycle handling.
 */
internal class Connector<Model : Parcelable, Msg : Any, Props : Any>(
    featureParams: FeatureParams<Model, Msg, Props>,
    savedStateHandle: SavedStateHandle
) : ViewModel(), Feature<Msg, Props> {
    private val feature: Feature<Msg, Props>

    override val props: Flow<Props> get() = feature.props

    override val scope: CoroutineScope get() = viewModelScope

    init {
        feature = TeaFeature(
            previousModel = savedStateHandle.get(MODEL_KEY),
            init = featureParams.init,
            update = featureParams.update,
            view = featureParams.view,
            featureScope = scope,
            effectContext = Dispatchers.Default,
            renderContext = Dispatchers.Main,
            onEachModel = {
                savedStateHandle.set(MODEL_KEY, it)
                featureParams.onEachModel?.invoke(it)
            }
        )
    }

    override infix fun dispatch(msg: Msg) = feature dispatch msg

    override suspend fun syncDispatch(msg: Msg) = feature.syncDispatch(msg)

    override suspend fun <OtherMsg : Any> bind(other: Feature<OtherMsg, *>, transform: (Msg) -> Effect<OtherMsg>) {
        feature.bind(other, transform)
    }

    /** ViewModelFactory for passing [featureParams] to [Connector]. */
    class Factory<Model : Parcelable, Msg : Any, Props : Any>(
        owner: SavedStateRegistryOwner,
        defaultArgs: Bundle? = null,
        private val featureParams: () -> FeatureParams<Model, Msg, Props>
    ) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

        @Suppress(names = ["UNCHECKED_CAST"])
        override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T =
            Connector(featureParams(), handle) as T
    }

    private companion object {
        private const val MODEL_KEY = "android_connector"
    }
}

/** Render [Props] with [lifecycleState]. */
inline fun <Msg : Any, Props : Any> Feature<Msg, Props>.render(
    fragment: Fragment,
    debounceTime: Long = 0,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline block: suspend (Props) -> Unit
): Job =
    props.collectWithLifecycle(fragment.viewLifecycleOwner, debounceTime, lifecycleState, block)

/** Collecting [T] with [lifecycleState] (without [Lifecycle.State.DESTROYED]). */
@OptIn(FlowPreview::class)
inline fun <T> Flow<T>.collectWithLifecycle(
    lifecycleOwner: LifecycleOwner,
    debounceTime: Long = 0,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline block: suspend (T) -> Unit
): Job {
    val collectBlock: suspend CoroutineScope.() -> Unit = {
        this@collectWithLifecycle
            .let { if (debounceTime > 0) it.debounce(debounceTime) else it }
            .collect(block)
    }
    return when (lifecycleState) {
        Lifecycle.State.INITIALIZED -> lifecycleOwner.lifecycleScope.launch(block = collectBlock)
        Lifecycle.State.CREATED -> lifecycleOwner.lifecycleScope.launchWhenCreated(collectBlock)
        Lifecycle.State.STARTED -> lifecycleOwner.lifecycleScope.launchWhenStarted(collectBlock)
        Lifecycle.State.RESUMED -> lifecycleOwner.lifecycleScope.launchWhenResumed(collectBlock)
        Lifecycle.State.DESTROYED -> error("The block cannot be launched in the DESTROYED state")
    }
}
