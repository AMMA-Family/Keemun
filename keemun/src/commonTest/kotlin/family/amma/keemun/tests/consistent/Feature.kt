package family.amma.keemun.tests.consistent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import family.amma.keemun.*
import family.amma.keemun.feature.TeaFeature

data class User(val id: Int)

data class ConsistentModel(
    val progress: Boolean,
    val loadedUser: User?
)

sealed class ConsistentMsg {
    data class LoadUserById(val id: Int) : ConsistentMsg()
    data class UserWasLoaded(val user: User) : ConsistentMsg()
}

sealed class ConsistentEff {
    data class LoadUser(val id: Int) : ConsistentEff()
}

fun feature(scope: CoroutineScope) = TeaFeature(
    previousState = null,
    coroutineScope = scope,
    initFeature = init,
    update = consistentUpdate,
    effectHandlers = setOf(effectHandler)
)

private val init = InitFeature<ConsistentModel, ConsistentEff> { previous ->
    (previous ?: ConsistentModel(progress = false, loadedUser = null)) to emptySet()
}

val consistentUpdate = Update<ConsistentModel, ConsistentMsg, ConsistentEff> { msg, model ->
    when (msg) {
        is ConsistentMsg.LoadUserById ->
            model.copy(progress = true) to setOf(ConsistentEff.LoadUser(msg.id))

        is ConsistentMsg.UserWasLoaded ->
            model.copy(progress = false, loadedUser = msg.user) to emptySet()
    }
}

private val effectHandler = EffectHandler<ConsistentEff, ConsistentMsg> { eff, dispatch ->
    when (eff) {
        is ConsistentEff.LoadUser -> {
            val user = loadUserById(eff.id)
            dispatch(ConsistentMsg.UserWasLoaded(user))
        }
    }
}

private suspend fun loadUserById(id: Int): User =
    withContext(Dispatchers.Default) { User(id) }
