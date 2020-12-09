package family.amma.tea

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Собирает все элементы в течение [timeMillis] миллисекунд,
 * а потом выдаёт накопленные элементы в виде списка.
 */
suspend fun <T> Flow<T>.collectTime(timeMillis: Long): List<T> {
    val result = ArrayList<T>()
    withTimeoutOrNull(timeMillis) {
        toList(destination = result)
    }
    return result
}
