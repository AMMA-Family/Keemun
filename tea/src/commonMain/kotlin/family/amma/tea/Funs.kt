package family.amma.tea

/**
 * [Update] builder function.
 */
fun <State : Any, Msg : Any, Eff : Any> update(block: Update<State, Msg, Eff>): Update<State, Msg, Eff> = block

/**
 * [ViewState] builder function.
 */
fun <State : Any, Props : Any> viewState(block: ViewState<State, Props>): ViewState<State, Props> = block
