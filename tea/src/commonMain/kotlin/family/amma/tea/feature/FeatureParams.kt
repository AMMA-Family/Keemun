package family.amma.tea.feature

import family.amma.tea.InitFeature
import family.amma.tea.InitWithPrevious
import family.amma.tea.Update

/**
 * Required parameters for creating [Feature].
 * @see InitFeature
 * @see Update
 */
data class FeatureParams<State : Any, Msg : Any>(
    val init: InitFeature<State, Msg, *>,
    val update: Update<State, Msg>,
)

/** @see FeatureParams */
@Suppress("FunctionName")
fun <State : Any, Msg : Any> FeatureParams(
    init: InitWithPrevious<State, Msg>,
    update: Update<State, Msg>
) = FeatureParams(
    init = InitFeature(
        preEffect = {},
        init = { prev, _ -> init(prev) }
    ),
    update = update
)
