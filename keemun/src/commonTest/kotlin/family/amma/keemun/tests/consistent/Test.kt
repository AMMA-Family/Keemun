package family.amma.keemun.tests.consistent

import family.amma.keemun.collectAsync
import kotlinx.coroutines.*
import family.amma.keemun.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ConsistentTest {
    @Test
    fun testUpdater1() {
        val userId = 101
        val defaultModel = ConsistentModel(progress = false, loadedUser = null)
        val msg = ConsistentMsg.LoadUserById(id = userId)
        val (model, effect) = consistentUpdate(msg, defaultModel)
        assertEquals(actual = model, expected = ConsistentModel(progress = true, loadedUser = null))
        assertEquals(actual = effect, expected = setOf(ConsistentEff.LoadUser(userId)))
    }

    @Test
    fun testUpdater2() {
        val defaultModel = ConsistentModel(progress = true, loadedUser = null)
        val user = User(id = 101)
        val msg = ConsistentMsg.UserWasLoaded(user)
        val (model, effect) = consistentUpdate(msg, defaultModel)
        assertEquals(actual = model, expected = ConsistentModel(progress = false, loadedUser = user))
        assertEquals(actual = effect, expected = emptySet())
    }

    @Test
    fun full() = runTest {
        val scope = CoroutineScope(Dispatchers.Default)
        val feature = feature(scope)
        val modelsListDef = feature.states.collectAsync(scope)
        val userId = 101
        val expected = listOf(
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