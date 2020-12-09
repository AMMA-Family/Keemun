package family.amma.tea.tests.consistent

import kotlinx.coroutines.*
import family.amma.tea.collectTime
import family.amma.tea.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ConsistentTest {
    /** Проверка работы [consistentUpdate]. */
    @Test
    fun testUpdater1() {
        val defaultModel = ConsistentModel(progress = false, loadedUser = null)
        val msg = ConsistentMsg.LoadUserById(id = 101)
        val (model, _) = consistentUpdate(msg, defaultModel)
        assertEquals(
            actual = model,
            expected = ConsistentModel(progress = true, loadedUser = null)
        )
    }

    /** Проверка работы [consistentUpdate]. */
    @Test
    fun testUpdater2() {
        val defaultModel = ConsistentModel(progress = true, loadedUser = null)
        val user = User(id = 101)
        val msg = ConsistentMsg.UserWasLoaded(user)
        val (model, _) = consistentUpdate(msg, defaultModel)
        assertEquals(
            actual = model,
            expected = ConsistentModel(progress = false, loadedUser = user)
        )
    }

    @Test
    fun full() = runTest {
        repeat(times = 10) { fullInteraction() }
    }

    /** Интеграционный тест фичи. */
    private suspend fun fullInteraction() {
        val scope = CoroutineScope(Dispatchers.Default)
        val feature = feature(scope)
        val userId = 101

        GlobalScope.launch {
            delay(100)
            feature.dispatch(ConsistentMsg.LoadUserById(id = userId))
        }

        val modelsList = feature
            .props
            .collectTime(timeMillis = 300)
        try {
            assertEquals(
                actual = modelsList,
                expected = listOf(
                    ConsistentModel(progress = false, loadedUser = null),
                    ConsistentModel(progress = true, loadedUser = null),
                    ConsistentModel(progress = false, loadedUser = User(id = userId))
                )
            )
        } finally {
            scope.cancel()
        }
    }
}