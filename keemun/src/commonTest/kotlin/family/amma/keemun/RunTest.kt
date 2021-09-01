package family.amma.keemun

import kotlinx.coroutines.CoroutineScope

/**
 * There is no runBlocking in kotlin-js, so there is no runBlocking in mpp either.
 */
expect fun runTest(block: suspend CoroutineScope.() -> Unit)
