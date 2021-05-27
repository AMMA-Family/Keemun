package family.amma.tea

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Collects all the elements asynchronously.
 */
fun <T> Flow<T>.collectAsync(coroutineScope: CoroutineScope): Deferred<List<T>> {
    val result = ArrayList<T>()
    coroutineScope.launch { collect { result.add(it) } }
    return coroutineScope.async { result }
}
