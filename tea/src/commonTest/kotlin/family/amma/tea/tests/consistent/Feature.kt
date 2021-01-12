package family.amma.tea.tests.consistent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import family.amma.tea.*
import family.amma.tea.feature.TeaFeature

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
    previousState = null,
    featureScope = scope,
    initFeature = InitFeature(init),
    update = consistentUpdate
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
