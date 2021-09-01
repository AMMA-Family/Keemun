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
import family.amma.keemun.feature.transform
import kotlinx.coroutines.CoroutineScope

/**
 * Create [Feature] with restoration of the previous state (if it was).
 * You can change [key] and [storeProducer] to change the behavior of saving and restoring state.
 * @see FeatureParams
 * @see ViewState
 * @see InitializationOptions
 */
inline fun <reified Model : Parcelable, Msg : Any, Props : Any, Effect : Any> Fragment.androidConnectors(
    crossinline featureParams: () -> FeatureParams<Model, Msg, Effect>,
    viewState: ViewState<Model, Props>,
    defaultArgs: Bundle? = null,
    key: String? = Model::class.simpleName,
    noinline storeProducer: () -> ViewModelStore = ::getViewModelStore,
    initOptions: InitializationOptions = InitializationOptions.WithLifecycle(Lifecycle.Event.ON_CREATE)
): Lazy<Feature<Props, Msg>> =
    createVMLazy<Connector<Model, Msg, Props>, Model, Msg, Props>(
        feature = { scope, model -> teaFeature(scope, model, featureParams()) },
        viewState = viewState, defaultArgs = defaultArgs, key = key, storeProducer = storeProducer, initOptions = initOptions
    )

/**
 * Create [Feature] with restoration of the previous state (if it was).
 * You can change [key] and [storeProducer] to change the behavior of saving and restoring state.
 * @param transform Incoming message restrictions.
 * @see FeatureParams
 * @see ViewState
 * @see InitializationOptions
 */
inline fun <reified Model : Parcelable, Msg : Any, reified OutMsg : Msg, Props : Any, Effect : Any> Fragment.androidConnectors(
    crossinline featureParams: () -> FeatureParams<Model, Msg, Effect>,
    viewState: ViewState<Model, Props>,
    crossinline transform: (Feature<Model, Msg>) -> Feature<Model, OutMsg> = ::transform,
    defaultArgs: Bundle? = null,
    key: String? = Model::class.simpleName,
    noinline storeProducer: () -> ViewModelStore = ::getViewModelStore,
    initOptions: InitializationOptions = InitializationOptions.WithLifecycle(Lifecycle.Event.ON_CREATE)
): Lazy<Feature<Props, OutMsg>> =
    createVMLazy<Connector<Model, OutMsg, Props>, Model, OutMsg, Props>(
        feature = { scope, model -> transform(teaFeature(scope, model, featureParams())) },
        viewState = viewState, defaultArgs = defaultArgs, key = key, storeProducer = storeProducer, initOptions = initOptions
    )

/**
 * Create [Feature] with restoration of the previous state (if it was).
 * You can change [key] and [storeProducer] to change the behavior of saving and restoring state.
 * @see FeatureParams
 * @see ViewState
 * @see InitializationOptions
 */
inline fun <reified Model : Parcelable, Msg : Any, Props : Any, Effect : Any> Fragment.sharedAndroidConnectors(
    crossinline featureParams: () -> FeatureParams<Model, Msg, Effect>,
    viewState: ViewState<Model, Props>,
    defaultArgs: Bundle? = null,
    key: String? = Model::class.simpleName,
    noinline storeProducer: () -> ViewModelStore = { requireActivity().viewModelStore },
    initOptions: InitializationOptions = InitializationOptions.WithLifecycle(Lifecycle.Event.ON_CREATE)
): Lazy<Feature<Props, Msg>> =
    createVMLazy<Connector<Model, Msg, Props>, Model, Msg, Props>(
        feature = { scope, model -> teaFeature(scope, model, featureParams()) },
        viewState = viewState, defaultArgs = defaultArgs, key = key, storeProducer = storeProducer, initOptions = initOptions
    )

/**
 * Create [Feature] with restoration of the previous state (if it was).
 * You can change [key] and [storeProducer] to change the behavior of saving and restoring state.
 * @param transform Incoming message restrictions.
 * @see FeatureParams
 * @see ViewState
 * @see InitializationOptions
 */
inline fun <reified Model : Parcelable, Msg : Any, reified OutMsg : Msg, Props : Any, Effect : Any> Fragment.sharedAndroidConnectors(
    crossinline featureParams: () -> FeatureParams<Model, Msg, Effect>,
    viewState: ViewState<Model, Props>,
    crossinline transform: (Feature<Model, Msg>) -> Feature<Model, OutMsg> = ::transform,
    defaultArgs: Bundle? = null,
    key: String? = Model::class.simpleName,
    noinline storeProducer: () -> ViewModelStore = { requireActivity().viewModelStore },
    initOptions: InitializationOptions = InitializationOptions.WithLifecycle(Lifecycle.Event.ON_CREATE)
): Lazy<Feature<Props, OutMsg>> =
    createVMLazy<Connector<Model, OutMsg, Props>, Model, OutMsg, Props>(
        feature = { scope, model -> transform(teaFeature(scope, model, featureParams())) },
        viewState = viewState, defaultArgs = defaultArgs, key = key, storeProducer = storeProducer, initOptions = initOptions
    )

/**
 * Create [Feature] with restoration of the previous state (if it was).
 * You can change [key] and [storeProducer] to change the behavior of saving and restoring state.
 * @see FeatureParams
 * @see ViewState
 * @see InitializationOptions
 */
inline fun <reified Model : Parcelable, Msg : Any, Props : Any, Effect : Any> ComponentActivity.androidConnectors(
    crossinline featureParams: () -> FeatureParams<Model, Msg, Effect>,
    viewState: ViewState<Model, Props>,
    defaultArgs: Bundle? = null,
    key: String? = Model::class.simpleName,
    noinline storeProducer: () -> ViewModelStore = ::getViewModelStore,
    initOptions: InitializationOptions = InitializationOptions.WithLifecycle(Lifecycle.Event.ON_CREATE)
): Lazy<Feature<Props, Msg>> =
    createVMLazy<Connector<Model, Msg, Props>, Model, Msg, Props>(
        feature = { scope, model -> teaFeature(scope, model, featureParams()) },
        viewState = viewState, defaultArgs = defaultArgs, key = key, storeProducer = storeProducer, initOptions = initOptions
    )

/**
 * Create [Feature] with restoration of the previous state (if it was).
 * You can change [key] and [storeProducer] to change the behavior of saving and restoring state.
 * @param transform Incoming message restrictions.
 * @see FeatureParams
 * @see ViewState
 * @see InitializationOptions
 */
inline fun <reified Model : Parcelable, Msg : Any, reified OutMsg : Msg, Props : Any, Effect : Any> ComponentActivity.androidConnectors(
    crossinline featureParams: () -> FeatureParams<Model, Msg, Effect>,
    viewState: ViewState<Model, Props>,
    crossinline transform: (Feature<Model, Msg>) -> Feature<Model, OutMsg> = ::transform,
    defaultArgs: Bundle? = null,
    key: String? = Model::class.simpleName,
    noinline storeProducer: () -> ViewModelStore = ::getViewModelStore,
    initOptions: InitializationOptions = InitializationOptions.WithLifecycle(Lifecycle.Event.ON_CREATE)
): Lazy<Feature<Props, OutMsg>> =
    createVMLazy<Connector<Model, OutMsg, Props>, Model, OutMsg, Props>(
        feature = { scope, model -> transform(teaFeature(scope, model, featureParams())) },
        viewState = viewState, defaultArgs = defaultArgs, key = key, storeProducer = storeProducer, initOptions = initOptions
    )

/** [TeaFeature] builder. */
fun <State : Parcelable, Msg : Any, Effect : Any> teaFeature(
    featureScope: CoroutineScope,
    previousState: State?,
    featureParams: FeatureParams<State, Msg, Effect>
): Feature<State, Msg> = TeaFeature(
    previousState = previousState,
    featureScope = featureScope,
    initFeature = featureParams.init,
    update = featureParams.update,
    effectHandler = featureParams.effectHandler
)
