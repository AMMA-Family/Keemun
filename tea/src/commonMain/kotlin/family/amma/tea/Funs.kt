package family.amma.tea

import kotlinx.coroutines.launch

/**
 * Create an empty [Effect].
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <Msg> none(): Effect<Msg>? = null

/**
 * Compose [effects] into a single [Effect].
 */
fun <Msg> batch(vararg effects: Effect<Msg>): Effect<Msg> =
    batch(effects.asIterable())

/**
 * Compose [effects] into a single [Effect].
 */
fun <Msg> batch(effects: Iterable<Effect<Msg>>): Effect<Msg> =
    { dispatch -> for (effect in effects) launch { effect(dispatch) } }

/**
 * Map [effect] of type [A] to [Effect] of [B] using [transform].
 */
fun <A, B> map(effect: Effect<A>, transform: (A) -> B): Effect<B> =
    { dispatch -> effect { a -> dispatch(transform(a)) } }

/**
 * Transform [next] of type [A] to [Effect] of type [B] to [Pair] of type [C] to [Effect] of type [D] using [transformA] and [transformB].
 */
fun <A, B, C, D> bimap(next: Pair<A, Effect<B>>, transformA: (A) -> C, transformB: (B) -> D): Pair<C, Effect<D>> =
    transformA(next.first) to map(next.second, transformB)

/**
 * [Effect] builder function.
 */
fun <Msg : Any> effect(block: Effect<Msg>): Effect<Msg> = block

/**
 * Wrap the message in an effect.
 * Example:
 * ```
 * val msg: Msg = Msg.ObserveUserInfo
 * val effect: Effect<Msg> = effect(msg)
 * ```
 */
fun <Msg : Any> effect(msg: Msg): Effect<Msg> =
    effect { dispatch -> dispatch(msg) }

/**
 * [Update] builder function.
 */
fun <State : Any, Msg : Any> update(block: Update<State, Msg>): Update<State, Msg> = block

/**
 * [ViewState] builder function.
 */
fun <State : Any, Props : Any> viewState(block: ViewState<State, Props>): ViewState<State, Props> = block
