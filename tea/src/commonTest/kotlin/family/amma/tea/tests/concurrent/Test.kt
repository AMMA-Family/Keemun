package family.amma.tea.tests.concurrent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import family.amma.tea.collectTime
import family.amma.tea.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ConcurrentTest {
    @Test
    fun run() = runTest {
        repeat(times = 10) { run(dispatchCount = 50) }
    }

    private suspend fun run(dispatchCount: Int) {
        val scope = CoroutineScope(Dispatchers.Default)
        val feature = feature(scope, dispatchCount)
        val lastModel = feature
            .props
            .collectTime(timeMillis = 100)
            .last()
        try {
            assertEquals(
                actual = lastModel,
                expected = ConcurrentModel(
                    flow1 = dispatchCount,
                    flow2 = dispatchCount,
                    flow3 = dispatchCount
                )
            )
        } finally {
            scope.cancel()
        }
    }
}