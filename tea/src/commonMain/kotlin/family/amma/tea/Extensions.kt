package family.amma.tea

import family.amma.tea.feature.Feature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Sequential sending of an unspecified number of messages. */
inline infix fun <State : Any, Msg : Any> Feature<State, Msg>.sequentialDispatch(
    crossinline lambda: suspend CoroutineScope.(dispatch: Dispatch<Msg>) -> Unit
) {
    scope.launch { lambda(this, ::syncDispatch) }
}
