package family.amma.tea

import kotlinx.coroutines.CoroutineScope

/**
 * В kotlin-js нет `runBlocking`, поэтому в мпп его тоже нет.
 * Приходится самому делать функцию для запуска тестов.
 */
expect fun runTest(block: suspend CoroutineScope.() -> Unit)
