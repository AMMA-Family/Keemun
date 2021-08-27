package family.amma.tea

import kotlinx.coroutines.CoroutineScope

/**
 * @param preEffect Effect that will return the necessary dependencies to initialize the state.
 * @param init Create a default state by previous state and dependencies and run start effects.
 *
 * `previous: State?` - If the launch occurs for the first time, then `previous == null`.
 *
 * If the system is being restored after the death of the process and the previous state was saved, then `previous != null`.
 */
data class InitFeature<State, Eff, Deps>(
    val preEffect: suspend CoroutineScope.() -> Deps,
    val init: (previous: State?, Deps) -> Pair<State, Set<Eff>>
)

/** Create [InitFeature] without pre-effect. */
@Suppress("FunctionName")
inline fun <State, Eff> InitFeature(
    crossinline withoutPreEffect: (previous: State?) -> Pair<State, Set<Eff>>
): InitFeature<State, Eff, Unit> =
    InitFeature(
        preEffect = {},
        init = { previous: State?, _ -> withoutPreEffect(previous) }
    )

/**
 * Dispatches a message to the runtime.
 */
typealias Dispatch<Msg> = suspend (msg: Msg) -> Unit

/**
 * Creates a next state and side-effects from a message and current state.
 */
typealias Update<State, Msg, Eff> = (msg: Msg, state: State) -> Pair<State, Set<Eff>>

/**
 * Creates view properties from the current state.
 */
typealias ViewState<Model, Props> = suspend (model: Model) -> Props

/**
 * Handling `eff` and `dispatch` messages.
 */
typealias EffectHandler<Eff, Msg> = suspend (eff: Eff, dispatch: Dispatch<Msg>) -> Unit
