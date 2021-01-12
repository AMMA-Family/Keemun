package family.amma.tea

import kotlinx.coroutines.CoroutineScope

/**
 * Create a default state and run start effects.
 * If the launch occurs for the first time, then `previous == null`.
 * If the system is being restored after the death of the process and the previous state was saved, then `previous != null`.
 *
 * Example:
 * ```
 * val init: InitWithPrevious<UserState, Msg> = { previous ->
 *     val defaultEffects = listOf<Effect<Msg>>(
 *         effect(Msg.StartAnimation),
 *         effect(Msg.UpdateProgress)
 *     )
 *     if (previous != null) {
 *         previous to batch(defaultEffects)
 *     } else {
 *         UserState(
 *             name = null,
 *             photo = null
 *         ) to batch(
 *             defaultEffects,
 *             effect { dispatch ->
 *                 repo.loadUserInfo().fold(
 *                     success = { userInfo -> dispatch(Msg.UserInfoWasLoaded(userInfo) },
 *                     error = { cause -> dispatch(Msg.Error(cause) }
 *                 )
 *             }
 *         )
 *     }
 * }
 * ```
 */
typealias InitWithPrevious<State, Msg> = (previous: State?) -> Next<State, Msg>

/**
 * @param preEffect Effect that will return the necessary dependencies to initialize the state.
 * @param init Create a default state by previous state and dependencies and run start effects.
 *
 * `previous: State?` - If the launch occurs for the first time, then `previous == null`.
 *
 * If the system is being restored after the death of the process and the previous state was saved, then `previous != null`.
 */
data class InitFeature<State, Msg, Deps>(
    val preEffect: suspend CoroutineScope.() -> Deps,
    val init: (previous: State?, Deps) -> Next<State, Msg>
)

/** Create [InitFeature] without pre-effect. */
@Suppress("FunctionName")
fun <State, Msg> InitFeature(initWithPrevious: InitWithPrevious<State, Msg>): InitFeature<State, Msg, Unit> =
    InitFeature(
        preEffect = {},
        init = { previous: State?, _ -> initWithPrevious(previous) }
    )

/**
 * Dispatches a message to the runtime.
 */
typealias Dispatch<Msg> = suspend (msg: Msg) -> Unit

/**
 * Runs a side-effect away from the runtime.
 */
typealias Effect<Msg> = suspend CoroutineScope.(dispatch: Dispatch<Msg>) -> Unit

/**
 * A pair of the next state and side-effects.
 */
typealias Next<State, Msg> = Pair<State, Effect<Msg>?>

/**
 * Creates a next state and side-effects from a message and current state.
 */
typealias Update<State, Msg> = (msg: Msg, state: State) -> Next<State, Msg>

/**
 * Creates a next state and side-effects (Which will send the `OutMsg`) from a `InMsg` and current state.
 */
typealias CrossUpdate<State, InMsg, OutMsg> = (msg: InMsg, state: State) -> Next<State, OutMsg>

/**
 * Creates view properties from the current state.
 */
typealias ViewState<Model, Props> = suspend (model: Model) -> Props
