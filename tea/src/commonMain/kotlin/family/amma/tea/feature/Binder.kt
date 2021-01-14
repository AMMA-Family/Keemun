package family.amma.tea.feature

import family.amma.tea.Effect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Scope holder for binding. */
interface Binder {
    val scope: CoroutineScope

    /** [feature] [State] -> [other]. */
    fun <State : Any, Msg : Any, Value : Any?> bind(
        feature: Feature<State, *>,
        other: Feature<*, Msg>,
        effect: (State) -> Effect<Msg>
    ): Job =
        bind(feature, other, valueExtractor = { it }, effect)

    /** [feature] [State] -> [feature] [Value] -> [other]. */
    fun <State : Any, Msg : Any, Value : Any?> bind(
        feature: Feature<State, *>,
        other: Feature<*, Msg>,
        valueExtractor: (State) -> Value,
        effect: (Value) -> Effect<Msg>
    ): Job = scope.launch(effectContext) {
        feature
            .states
            .map { valueExtractor(it) }
            .distinctUntilChanged()
            .collect { value -> effect(value).invoke(this, other::syncDispatch) }
    }
}
