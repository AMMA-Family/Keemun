package family.amma.keemun

/**
 * Creates view properties from the current state.
 */
@Deprecated("Use Transform")
fun interface ViewState<Model, Props> {
    suspend operator fun invoke(model: Model): Props
}
