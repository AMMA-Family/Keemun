package family.amma.keemun.feature

import family.amma.keemun.EffectHandler
import family.amma.keemun.InitFeature
import family.amma.keemun.Update

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
