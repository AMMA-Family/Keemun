package family.amma.keemun.tests.concurrent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import family.amma.keemun.*
import family.amma.keemun.feature.TeaFeature

data class ConcurrentModel(val flow1: Int, val flow2: Int, val flow3: Int)

sealed class ConcurrentMsg {
    data class ChangeFlow1(val newValue: Int) : ConcurrentMsg()
    data class ChangeFlow2(val newValue: Int) : ConcurrentMsg()
    data class ChangeFlow3(val newValue: Int) : ConcurrentMsg()
}

sealed class ConcurrentEff {
    data class StartChangeFlow1(val repeatCount: Int) : ConcurrentEff()
    data class StartChangeFlow2(val repeatCount: Int) : ConcurrentEff()
    data class StartChangeFlow3(val repeatCount: Int) : ConcurrentEff()
}

fun feature(scope: CoroutineScope, repeatCount: Int) = TeaFeature(
    previousState = null,
    scope = scope,
    initFeature = init(repeatCount),
    update = update,
    effectHandler = effectHandler
)

private fun init(repeatCount: Int) = InitFeature<ConcurrentModel, ConcurrentEff> { previous ->
    val model = previous ?: ConcurrentModel(flow1 = 0, flow2 = 0, flow3 = 0)
    model to setOf(
        ConcurrentEff.StartChangeFlow1(repeatCount),
        ConcurrentEff.StartChangeFlow2(repeatCount),
        ConcurrentEff.StartChangeFlow3(repeatCount),
    )
}

private val update = Update<ConcurrentModel, ConcurrentMsg, ConcurrentEff> { msg, model ->
    when (msg) {
        is ConcurrentMsg.ChangeFlow1 -> model.copy(flow1 = msg.newValue) to emptySet()
        is ConcurrentMsg.ChangeFlow2 -> model.copy(flow2 = msg.newValue) to emptySet()
        is ConcurrentMsg.ChangeFlow3 -> model.copy(flow3 = msg.newValue) to emptySet()
    }
}

private val effectHandler = EffectHandler<ConcurrentEff, ConcurrentMsg> { eff, dispatch ->
    when (eff) {
        is ConcurrentEff.StartChangeFlow1 -> (1..eff.repeatCount).asFlow().collect { dispatch(ConcurrentMsg.ChangeFlow1(it)) }
        is ConcurrentEff.StartChangeFlow2 -> (1..eff.repeatCount).asFlow().collect { dispatch(ConcurrentMsg.ChangeFlow2(it)) }
        is ConcurrentEff.StartChangeFlow3 -> (1..eff.repeatCount).asFlow().collect { dispatch(ConcurrentMsg.ChangeFlow3(it)) }
    }
}

