package family.amma.keemun.tests.completeness

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import family.amma.keemun.*
import family.amma.keemun.feature.TeaFeature

data class CompletenessModel(val flow1: Int, val flow2: Int, val flow3: Int)

sealed class CompletenessMsg {
    object IncFlow1 : CompletenessMsg()
    object IncFlow2 : CompletenessMsg()
    object IncFlow3 : CompletenessMsg()
}

sealed class CompletenessEff {
    data class StartIncFlow1(val repeatCount: Int) : CompletenessEff()
    data class StartIncFlow2(val repeatCount: Int) : CompletenessEff()
    data class StartIncFlow3(val repeatCount: Int) : CompletenessEff()
}

fun feature(scope: CoroutineScope, repeatCount: Int) = TeaFeature(
    previousState = null,
    coroutineScope = scope,
    initFeature = init(repeatCount),
    update = update,
    effectHandlers = setOf(effectHandler)
)

private fun init(repeatCount: Int) = InitFeature<CompletenessModel, CompletenessEff> { previous ->
    val model = previous ?: CompletenessModel(flow1 = 0, flow2 = 0, flow3 = 0)
    model to setOf(
        CompletenessEff.StartIncFlow1(repeatCount),
        CompletenessEff.StartIncFlow2(repeatCount),
        CompletenessEff.StartIncFlow3(repeatCount)
    )
}

private val update = Update<CompletenessModel, CompletenessMsg, CompletenessEff> { msg, model ->
    when (msg) {
        CompletenessMsg.IncFlow1 -> model.copy(flow1 = model.flow1 + 1) to emptySet()
        CompletenessMsg.IncFlow2 -> model.copy(flow2 = model.flow2 + 1) to emptySet()
        CompletenessMsg.IncFlow3 -> model.copy(flow3 = model.flow3 + 1) to emptySet()
    }
}

private val effectHandler = EffectHandler<CompletenessEff, CompletenessMsg> { eff, dispatch ->
    when (eff) {
        is CompletenessEff.StartIncFlow1 -> (1..eff.repeatCount).asFlow().collect { dispatch(CompletenessMsg.IncFlow1) }
        is CompletenessEff.StartIncFlow2 -> (1..eff.repeatCount).asFlow().collect { dispatch(CompletenessMsg.IncFlow2) }
        is CompletenessEff.StartIncFlow3 -> (1..eff.repeatCount).asFlow().collect { dispatch(CompletenessMsg.IncFlow3) }
    }
}
