package family.amma.keemun

import kotlinx.coroutines.CoroutineScope

/**
 * @param preEffect Effect that will return the necessary dependencies to initialize the state.
 * @param init Create a default state by previous state and dependencies and run start effects.
 *
 * `previous: State?` - If the launch occurs for the first time, then `previous == null`.
 *
 * If the system is being restored after the death of the process and the previous state was saved, then `previous != null`.
 */
data class InitFeature<State, Effect, Deps>(
    val preEffect: suspend () -> Deps,
    val init: (previous: State?, Deps) -> Pair<State, Set<Effect>>
)

/** Create [InitFeature] without pre-effect. */
@Suppress("FunctionName")
inline fun <State, Effect> InitFeature(
    crossinline withoutPreEffect: (previous: State?) -> Pair<State, Set<Effect>>
): InitFeature<State, Effect, Unit> =
    InitFeature(
        preEffect = {},
        init = { previous: State?, _ -> withoutPreEffect(previous) }
    )

/**
 * Dispatches a message to the runtime.
 */
fun interface Dispatch<in Msg> {
    suspend operator fun invoke(msg: Msg)
}

/**
 * Creates a next state and side-effects from a message and current state.
 */
fun interface Update<State, in Msg, Effect> {
    operator fun invoke(msg: Msg, state: State): Pair<State, Set<Effect>>
}

/**
 * Creates view properties from the current state.
 */
fun interface ViewState<Model, Props> {
    suspend operator fun invoke(model: Model): Props
}

/**
 * Handling `effect` and `dispatch` messages.
 */
fun interface EffectHandler<Effect, Msg> {
    suspend operator fun CoroutineScope.invoke(effect: Effect, dispatch: Dispatch<Msg>): Any?
}
