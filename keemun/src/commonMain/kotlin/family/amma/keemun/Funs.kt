package family.amma.keemun

import family.amma.keemun.feature.Feature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * [Update] builder function.
 */
fun <State : Any, Msg : Any, Effect : Any> update(block: Update<State, Msg, Effect>): Update<State, Msg, Effect> = block

/**
 * [ViewState] builder function.
 */
fun <State : Any, Props : Any> viewState(block: ViewState<State, Props>): ViewState<State, Props> = block

/** Process [effects] in [EffectHandler]. */
fun <Effect : Any, Msg : Any> EffectHandler<Effect, Msg>.process(
    effects: Set<Effect>,
    coroutineScope: CoroutineScope,
    dispatch: Dispatch<Msg>
) {
    for (effect in effects) coroutineScope.launch { invoke(effect, dispatch) }
}

/** (Feature<State, Msg>) -> Feature<State, OutMsg> */
inline fun <State : Any, Msg : Any, reified OutMsg : Msg> transform(current: Feature<State, Msg>): Feature<State, OutMsg> =
    object : Feature<State, OutMsg> {
        override val states: Flow<State> = current.states
        override val scope: CoroutineScope = current.scope
        override fun dispatch(msg: OutMsg) = current.dispatch(msg)
        override suspend fun syncDispatch(msg: OutMsg) = current.syncDispatch(msg)
    }
