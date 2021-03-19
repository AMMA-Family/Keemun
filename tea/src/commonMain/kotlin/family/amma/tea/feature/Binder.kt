package family.amma.tea.feature

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/** [feature] [Msg1] -> [other] [Msg2]. */
fun <Msg1 : Any, Msg2 : Any> bind(
    feature: Feature<*, Msg1>,
    other: Feature<*, Msg2>,
    map: (Msg1) -> Msg2?
): Job = feature.scope.launch(Dispatchers.Default) {
    feature
        .messages
        .collect { value -> map(value)?.let { other.syncDispatch(it) } }
}
