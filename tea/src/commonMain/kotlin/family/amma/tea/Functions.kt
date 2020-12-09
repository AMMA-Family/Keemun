package family.amma.tea

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
