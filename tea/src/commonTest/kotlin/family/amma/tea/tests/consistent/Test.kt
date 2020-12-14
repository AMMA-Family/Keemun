package family.amma.tea.tests.consistent

import kotlinx.coroutines.*
import family.amma.tea.notTakeAfter
import family.amma.tea.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ConsistentTest {
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
        val scope = CoroutineScope(Dispatchers.Default)
        val feature = feature(scope)
        val userId = 101
        val expected = listOf(
            ConsistentModel(progress = false, loadedUser = null),
            ConsistentModel(progress = true, loadedUser = null),
            ConsistentModel(progress = false, loadedUser = User(id = userId))
        )
        scope.launch {
            feature.dispatch(ConsistentMsg.LoadUserById(id = userId))
        }

        val modelsList = feature
            .props
            .notTakeAfter(timeMillis = 300, expected = expected)
        try {
            assertEquals(actual = modelsList, expected = expected)
        } finally {
            scope.cancel()
        }
    }
}