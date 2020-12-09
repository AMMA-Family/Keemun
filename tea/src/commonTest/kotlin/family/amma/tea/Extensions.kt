package family.amma.tea

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Collects all the elements within [timeMillis] milliseconds, and then returns the accumulated elements as a list.
 */
suspend fun <T> Flow<T>.collectTime(timeMillis: Long): List<T> {
    val result = ArrayList<T>()
    withTimeoutOrNull(timeMillis) {
        toList(destination = result)
    }
    return result
}
