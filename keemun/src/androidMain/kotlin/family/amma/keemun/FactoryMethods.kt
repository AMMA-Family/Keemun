package family.amma.keemun

import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import family.amma.keemun.feature.Feature
import family.amma.keemun.feature.FeatureParams
import family.amma.keemun.feature.TeaFeature
import kotlinx.coroutines.CoroutineScope

/**
 * Create [Feature] with restoration of the previous state (if it was).
 * You can change [key] and [storeProducer] to change the behavior of saving and restoring state.
 * @see FeatureParams
 * @see ViewState
 * @see InitializationOptions
 */
inline fun <reified State : Parcelable, Msg : Any, reified OutMsg : Msg, ViewState : Any, Effect : Any> Fragment.androidConnectors(
    crossinline featureParams: () -> FeatureParams<State, Msg, Effect>,
    stateTransform: StateTransform<State, ViewState>,
    defaultArgs: Bundle? = null,
    key: String? = State::class.simpleName,
    noinline storeProducer: () -> ViewModelStore = ::getViewModelStore,
    initOptions: InitializationOptions = InitializationOptions.WithLifecycle(Lifecycle.Event.ON_CREATE)
): Lazy<Feature<ViewState, OutMsg>> =
    createVMLazy<Connector<State, OutMsg, ViewState>, State, OutMsg, ViewState>(
        feature = { scope, model -> transform(teaFeature(scope, model, featureParams())) },
        viewState = stateTransform, defaultArgs = defaultArgs, key = key, storeProducer = storeProducer, initOptions = initOptions
    )

/**
 * Create [Feature] with restoration of the previous state (if it was).
 * You can change [key] and [storeProducer] to change the behavior of saving and restoring state.
 * @see FeatureParams
 * @see ViewState
 * @see InitializationOptions
 */
inline fun <reified State : Parcelable, Msg : Any, reified OutMsg : Msg, ViewState : Any, Effect : Any> Fragment.sharedAndroidConnectors(
    crossinline featureParams: () -> FeatureParams<State, Msg, Effect>,
    stateTransform: StateTransform<State, ViewState>,
    defaultArgs: Bundle? = null,
    key: String? = State::class.simpleName,
    noinline storeProducer: () -> ViewModelStore = { requireActivity().viewModelStore },
    initOptions: InitializationOptions = InitializationOptions.WithLifecycle(Lifecycle.Event.ON_CREATE)
): Lazy<Feature<ViewState, OutMsg>> =
    createVMLazy<Connector<State, OutMsg, ViewState>, State, OutMsg, ViewState>(
        feature = { scope, model -> transform(teaFeature(scope, model, featureParams())) },
        viewState = stateTransform, defaultArgs = defaultArgs, key = key, storeProducer = storeProducer, initOptions = initOptions
    )

/**
 * Create [Feature] with restoration of the previous state (if it was).
 * You can change [key] and [storeProducer] to change the behavior of saving and restoring state.
 * @see FeatureParams
 * @see ViewState
 * @see InitializationOptions
 */
@Deprecated("Use version with StateTransform")
inline fun <reified Model : Parcelable, Msg : Any, reified OutMsg : Msg, ViewState : Any, Effect : Any> ComponentActivity.androidConnectors(
    crossinline featureParams: () -> FeatureParams<Model, Msg, Effect>,
    stateTransform: StateTransform<Model, ViewState>,
    defaultArgs: Bundle? = null,
    key: String? = Model::class.simpleName,
    noinline storeProducer: () -> ViewModelStore = ::getViewModelStore,
    initOptions: InitializationOptions = InitializationOptions.WithLifecycle(Lifecycle.Event.ON_CREATE)
): Lazy<Feature<ViewState, OutMsg>> =
    createVMLazy<Connector<Model, OutMsg, ViewState>, Model, OutMsg, ViewState>(
        feature = { scope, model -> transform(teaFeature(scope, model, featureParams())) },
        viewState = stateTransform, defaultArgs = defaultArgs, key = key, storeProducer = storeProducer, initOptions = initOptions
    )

/** [TeaFeature] builder. */
fun <State : Parcelable, Msg : Any, Effect : Any> teaFeature(
    featureScope: CoroutineScope,
    previousState: State?,
    featureParams: FeatureParams<State, Msg, Effect>
): Feature<State, Msg> = TeaFeature(
    previousState = previousState,
    scope = featureScope,
    initFeature = featureParams.init,
    update = featureParams.update,
    effectHandler = featureParams.effectHandler
)
