package family.amma.keemun.feature

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * A common interface, the implementation of which controls all entities and starts the entire processing mechanism.
 * [Msg] - messages with which we will transform [State].
 */
interface Feature<State : Any, Msg : Any> {
    /** Flow of states. */
    val states: SharedFlow<State>

    /** The main scope on which all coroutines will be launched. */
    val scope: CoroutineScope

    /** Sending messages asynchronously. */
    infix fun dispatch(msg: Msg)

    /** Sending messages synchronously. */
    suspend infix fun syncDispatch(msg: Msg)
}
