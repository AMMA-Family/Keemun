package family.amma.tea

import kotlinx.coroutines.launch

/**
 * Create an empty [Effect].
 */
fun <Msg> none(): Effect<Msg> = {}

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

