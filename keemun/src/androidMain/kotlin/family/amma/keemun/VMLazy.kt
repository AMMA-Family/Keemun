package family.amma.keemun

import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import family.amma.keemun.feature.Feature
import kotlinx.coroutines.CoroutineScope
import kotlin.reflect.KClass

/** General method for creating a connector. */
inline fun <reified VM : ViewModel, State : Parcelable, Msg : Any, ViewState : Any> SavedStateRegistryOwner.createVMLazy(
    noinline feature: (CoroutineScope, State?) -> Feature<State, Msg>,
    viewState: StateTransform<State, ViewState>,
    noinline storeProducer: () -> ViewModelStore,
    key: String?,
    defaultArgs: Bundle?,
    initOptions: InitializationOptions
): Lazy<VM> {
    return withOptions(
        initOptions = initOptions,
        lazyObj = VMLazy(VM::class, storeProducer, key) {
            Connector.Factory(this, defaultArgs, feature, viewState)
        }
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
            return cached ?: kotlin.run {
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
