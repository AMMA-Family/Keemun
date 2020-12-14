package family.amma.tea

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Collects all the elements within [timeMillis] milliseconds, and then returns the accumulated elements as a list.
 */
suspend fun <T> Flow<T>.notTakeAfter(timeMillis: Long, expected: T): List<T> {
    val result = ArrayList<T>()
    withTimeoutOrNull(timeMillis) {
        onEach { result.add(it) }.takeWhile { it != expected }.collect()
    }
    return result
}
