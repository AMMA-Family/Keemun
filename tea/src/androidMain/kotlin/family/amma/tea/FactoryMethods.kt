package family.amma.tea

import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.savedstate.SavedStateRegistryOwner
import family.amma.tea.feature.Feature
import family.amma.tea.feature.FeatureParams
import family.amma.tea.feature.TeaFeature
import kotlinx.coroutines.CoroutineScope
import kotlin.reflect.KClass

/**
 * Create [Feature] with restoration of the previous state (if it was).
 * @see viewModels
 * @see FeatureParams
 * @see InitializationOptions
 */
inline fun <reified Model : Parcelable, Msg : Any, Props : Any, Effect : Any> Fragment.androidConnectors(
    crossinline featureParams: () -> FeatureParams<Model, Msg, Effect>,
    noinline viewState: ViewState<Model, Props>,
    defaultArgs: Bundle? = null,
    key: String? = Model::class.simpleName,
    noinline storeProducer: () -> ViewModelStore = { viewModelStore },
    initOptions: InitializationOptions = InitializationOptions.WithLifecycle(Lifecycle.Event.ON_CREATE)
): Lazy<Feature<Props, Msg>> =
    createVMLazy<Connector<Model, Msg, Props>, Model, Msg, Props>(
        feature = { scope, model -> teaFeature(scope, model, featureParams()) },
        viewState = viewState,
        defaultArgs = defaultArgs,
        key = key,
        storeProducer = storeProducer,
        initOptions = initOptions
    )

/**
 * Create [Feature] with restoration of the previous state (if it was).
 * @see viewModels
 * @see FeatureParams
 * @see InitializationOptions
 */
inline fun <reified Model : Parcelable, Msg : Any, Props : Any, Effect : Any> Fragment.sharedAndroidConnectors(
    crossinline featureParams: () -> FeatureParams<Model, Msg, Effect>,
    noinline viewState: ViewState<Model, Props>,
    defaultArgs: Bundle? = null,
    key: String? = Model::class.simpleName,
    noinline storeProducer: () -> ViewModelStore = { requireActivity().viewModelStore },
    initOptions: InitializationOptions = InitializationOptions.WithLifecycle(Lifecycle.Event.ON_CREATE)
): Lazy<Feature<Props, Msg>> =
    createVMLazy<Connector<Model, Msg, Props>, Model, Msg, Props>(
        feature = { scope, model -> teaFeature(scope, model, featureParams()) },
        viewState = viewState,
        defaultArgs = defaultArgs,
        key = key,
        storeProducer = storeProducer,
        initOptions = initOptions
    )

/**
 * Create [Feature] with restoration of the previous state (if it was).
 * @see viewModels
 * @see FeatureParams
 * @see InitializationOptions
 */
inline fun <reified Model : Parcelable, Msg : Any, Props : Any, Effect : Any> ComponentActivity.androidConnectors(
    crossinline featureParams: () -> FeatureParams<Model, Msg, Effect>,
    noinline viewState: ViewState<Model, Props>,
    defaultArgs: Bundle? = null,
    key: String? = Model::class.simpleName,
    initOptions: InitializationOptions = InitializationOptions.WithLifecycle(Lifecycle.Event.ON_CREATE)
): Lazy<Feature<Props, Msg>> =
    createVMLazy<Connector<Model, Msg, Props>, Model, Msg, Props>(
        feature = { scope, model -> teaFeature(scope, model, featureParams()) },
        viewState = viewState,
        defaultArgs = defaultArgs,
        key = key,
        storeProducer = { viewModelStore },
        initOptions = initOptions
    )

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

/** General method for creating a connector. */
inline fun <reified VM : ViewModel, Model : Parcelable, Msg : Any, Props : Any> SavedStateRegistryOwner.createVMLazy(
    noinline feature: (CoroutineScope, Model?) -> Feature<Model, Msg>,
    noinline viewState: ViewState<Model, Props>,
    noinline storeProducer: () -> ViewModelStore,
    key: String?,
    defaultArgs: Bundle?,
    initOptions: InitializationOptions
): Lazy<VM> {
    val factory = Connector.Factory(this, defaultArgs, feature, viewState)
    return withOptions(
        initOptions = initOptions,
        lazyObj = VMLazy(VM::class, storeProducer, key) { factory }
    )
}

/** A variation of [ViewModelLazy] with one difference: you can add a custom [key]. */
class VMLazy<VM : ViewModel>(
    private val viewModelClass: KClass<VM>,
    private val storeProducer: () -> ViewModelStore,
    private val key: String?,
    private val factoryProducer: () -> ViewModelProvider.Factory
) : Lazy<VM> {
    private var cached: VM? = null

    override val value: VM
        get() {
            val viewModel = cached
            return if (viewModel == null) {
                val factory = factoryProducer()
                try {
                    val store = storeProducer()
                    val vmKey = key ?: defaultKey()
                    ViewModelProvider(store, factory).get(vmKey, viewModelClass.java).also {
                        cached = it
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // for Huawei devices only.
                    factory.create(viewModelClass.java).also { cached = it }
                }
            } else {
                viewModel
            }
        }

    /**
     * I didn’t come up with this myself, this is a copy-paste of the default android implementation.
     * @see ViewModelProvider.get
     */
    private fun defaultKey(): String {
        val canonicalName = viewModelClass.java.canonicalName
            ?: throw IllegalArgumentException("Local and anonymous classes can not be ViewModels")
        return "androidx.lifecycle.ViewModelProvider.DefaultKey:$canonicalName"
    }

    override fun isInitialized() = cached != null
}

/** Applies [InitializationOptions] to [lazyObj]. */
fun <T> LifecycleOwner.withOptions(initOptions: InitializationOptions, lazyObj: Lazy<T>): Lazy<T> =
    when (initOptions) {
        InitializationOptions.Lazy -> lazyObj
        is InitializationOptions.WithLifecycle -> lazyObj.also {
            lifecycle.addObserver(LifecycleEventObserver { _, event ->
                if (event == initOptions.event) it.value
            })
        }
    }

/**
 * Initialization options [androidConnectors].
 * - [Lazy] The connector will be initialized only when accessed.
 * - [WithLifecycle] The connector will be initialized when [Lifecycle.Event] occurs.
 */
sealed class InitializationOptions {
    object Lazy : InitializationOptions()
    data class WithLifecycle(val event: Lifecycle.Event) : InitializationOptions()
}
