package family.amma.tea

import kotlinx.coroutines.CoroutineScope

/**
 * Создать дефолтное состояние и запустить стартовые эффекты.
 * Если запуск происходит впервые, то `previous = null`.
 * Если же система восстанаваливается после смерти процесса и предыдущее состояние было сохранено,
 * то `previous != null`.
 *
 * Пример использования:
 * ```
 * val init: InitWithPrevious<UserModel, Msg> = { previous ->
 *     val defaultEffects = listOf<Effect<Msg>>(
 *         effect(Msg.StartAnimation),
 *         effect(Msg.UpdateProgress)
 *     )
 *     if (previous != null) {
 *         previous to batch(defaultEffects)
 *     } else {
 *         UserModel(
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
 * ```
 */
typealias InitWithPrevious<Model, Msg> = (previous: Model?) -> Next<Model, Msg>

/**
 * Dispatches a message to the runtime.
 */
typealias Dispatch<Msg> = suspend (msg: Msg) -> Unit

/**
 * Runs a side-effect away from the runtime.
 */
typealias Effect<Msg> = suspend CoroutineScope.(dispatch: Dispatch<Msg>) -> Any?

/**
 * A pair of the next state and side-effects.
 */
typealias Next<Model, Msg> = Pair<Model, Effect<Msg>>

/**
 * Creates a next state and side-effects from a message and current state.
 */
typealias Update<Model, Msg> = (msg: Msg, model: Model) -> Next<Model, Msg>

/**
 * Creates a next state and side-effects (Which will send the `OutMsg`) from a `InMsg` and current state.
 */
typealias CrossUpdate<Model, InMsg, OutMsg> = (msg: InMsg, model: Model) -> Next<Model, OutMsg>

/**
 * Creates view properties from the current state.
 */
typealias View<Model, Props> = (model: Model) -> Props

/**
 * [Effect] builder function.
 */
fun <Msg : Any> effect(block: Effect<Msg>): Effect<Msg> = block

/**
 * [Update] builder function.
 */
fun <Model : Any, Msg : Any> update(block: Update<Model, Msg>): Update<Model, Msg> = block

/**
 * [View] builder function.
 */
fun <Model : Any, Props : Any> view(block: View<Model, Props>): View<Model, Props> = block
