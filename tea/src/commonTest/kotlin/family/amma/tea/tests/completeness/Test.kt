package family.amma.tea.tests.completeness

import family.amma.tea.collectAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import family.amma.tea.runTest
import kotlinx.coroutines.delay
import kotlin.test.Test
import kotlin.test.assertEquals

class CompletenessTest {
    @Test
    fun run() = runTest {
        val dispatchCount = 50
        val expected = CompletenessModel(
            flow1 = dispatchCount,
            flow2 = dispatchCount,
            flow3 = dispatchCount
        )

        val scope = CoroutineScope(Dispatchers.Default)
        val feature = feature(scope, dispatchCount)

        val modelsListDef = feature.states.collectAsync(scope)
        delay(300)
        try {
            assertEquals(actual = modelsListDef.await().last(), expected = expected)
        } finally {
            scope.cancel()
        }
    }
}