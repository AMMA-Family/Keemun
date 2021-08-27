package family.amma.tea.tests.consistent

import family.amma.tea.collectAsync
import kotlinx.coroutines.*
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
        val modelsListDef = feature.states.collectAsync(scope)
        val userId = 101
        val expected = listOf(
            ConsistentModel(progress = false, loadedUser = null),
            ConsistentModel(progress = true, loadedUser = null),
            ConsistentModel(progress = false, loadedUser = User(id = userId))
        )
        feature.syncDispatch(ConsistentMsg.LoadUserById(id = userId))
        delay(300)
        try {
            assertEquals(actual = modelsListDef.await(), expected = expected)
        } finally {
            scope.cancel()
        }
    }
}