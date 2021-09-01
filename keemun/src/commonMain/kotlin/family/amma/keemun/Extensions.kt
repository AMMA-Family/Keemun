package family.amma.keemun

import family.amma.keemun.feature.Feature
import kotlinx.coroutines.launch

/** Sequential sending of an unspecified number of messages. */
inline infix fun <State : Any, Msg : Any> Feature<State, Msg>.sequentialDispatch(
    crossinline lambda: suspend (dispatch: Dispatch<Msg>) -> Unit
) {
    scope.launch { lambda(::syncDispatch) }
}
