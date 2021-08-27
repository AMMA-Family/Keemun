package family.amma.tea.feature

import family.amma.tea.EffectHandler
import family.amma.tea.InitFeature
import family.amma.tea.Update

/**
 * Required parameters for creating [Feature].
 * @see InitFeature
 * @see Update
 * @see EffectHandler
 */
data class FeatureParams<State : Any, Msg : Any, Effect : Any>(
    val init: InitFeature<State, Effect, *>,
    val update: Update<State, Msg, Effect>,
    val effectHandler: EffectHandler<Effect, Msg>
)
