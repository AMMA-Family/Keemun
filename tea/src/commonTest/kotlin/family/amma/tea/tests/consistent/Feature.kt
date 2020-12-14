package family.amma.tea.tests.consistent

import kotlinx.coroutines.CoroutineScope
import family.amma.tea.effect
import family.amma.tea.none
import family.amma.tea.InitWithPrevious
import family.amma.tea.feature.TeaFeature
import family.amma.tea.Update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class User(val id: Int)

data class ConsistentModel(
    val progress: Boolean,
    val loadedUser: User?
)

sealed class ConsistentMsg {
    data class LoadUserById(val id: Int) : ConsistentMsg()
    data class UserWasLoaded(val user: User) : ConsistentMsg()
}

fun feature(scope: CoroutineScope) = TeaFeature(
    previousModel = null,
    init = init,
    update = consistentUpdate,
    view = { it },
    featureScope = scope,
    onEachModel = {},
    renderContext = Dispatchers.Default
)

private val init: InitWithPrevious<ConsistentModel, ConsistentMsg> = { previous ->
    (previous ?: ConsistentModel(progress = false, loadedUser = null)) to none()
}

val consistentUpdate: Update<ConsistentModel, ConsistentMsg> = { msg, model ->
    when (msg) {
        is ConsistentMsg.LoadUserById -> model.copy(progress = true) to effect { dispatch ->
            val user = loadUserById(msg.id)
            dispatch(ConsistentMsg.UserWasLoaded(user))
        }

        is ConsistentMsg.UserWasLoaded ->
            model.copy(progress = false, loadedUser = msg.user) to none()
    }
}

private suspend fun loadUserById(id: Int): User =
    withContext(Dispatchers.Default) { User(id) }
