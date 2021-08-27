package family.amma.tea

/**
 * [Update] builder function.
 */
fun <State : Any, Msg : Any, Effect : Any> update(block: Update<State, Msg, Effect>): Update<State, Msg, Effect> = block

/**
 * [ViewState] builder function.
 */
fun <State : Any, Props : Any> viewState(block: ViewState<State, Props>): ViewState<State, Props> = block
