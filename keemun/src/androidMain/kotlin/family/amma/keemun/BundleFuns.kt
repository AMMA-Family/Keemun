package family.amma.keemun

import android.os.Bundle
import android.os.Parcelable

/** Converter to bundle and vice versa. */
class BundleFuns<T : Any>(
    val toBundle: (T) -> Bundle,
    val fromBundle: (Bundle) -> T?,
)

/** Implementation for parcelable [T]. */
fun <T : Parcelable> parcelableBundleFuns(key: String) = BundleFuns(
    toBundle = { Bundle().apply { putParcelable(key, it) } },
    fromBundle = { bundle -> bundle.getParcelable<T>(key) }
)
