package family.amma.tea

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel

fun <T : ViewModel> LifecycleOwner.binder(block: () -> T): Lazy<T> =
    withOptions(
        InitializationOptions.WithLifecycle(Lifecycle.Event.ON_CREATE),
        lazy(LazyThreadSafetyMode.NONE, block)
    )
