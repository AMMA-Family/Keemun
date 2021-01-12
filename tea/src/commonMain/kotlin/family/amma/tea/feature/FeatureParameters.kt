package family.amma.tea.feature

import family.amma.tea.InitFeature
import family.amma.tea.InitWithPrevious
import family.amma.tea.Update

/**
 * Required parameters for creating [Feature].
 * @see InitFeature
 * @see Update
 */
data class FeatureParameters<State : Any, Msg : Any, Deps>(
    val init: InitFeature<State, Msg, Deps>,
    val update: Update<State, Msg>,
)

/** @see FeatureParameters */
typealias FeatureParams<Model, Msg> = FeatureParameters<Model, Msg, Unit>

/** @see FeatureParameters */
@Suppress("FunctionName")
fun <State : Any, Msg : Any> FeatureParams(
    init: InitWithPrevious<State, Msg>,
    update: Update<State, Msg>
) = FeatureParameters(
    init = InitFeature(
        preEffect = {},
        init = { prev, _ -> init(prev) }
    ),
    update = update
)
