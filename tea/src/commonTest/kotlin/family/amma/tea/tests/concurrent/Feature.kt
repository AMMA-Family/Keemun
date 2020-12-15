package family.amma.tea.tests.concurrent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import family.amma.tea.batch
import family.amma.tea.none
import family.amma.tea.InitWithPrevious
import family.amma.tea.Update
import family.amma.tea.effect
import family.amma.tea.feature.TeaFeature
import kotlinx.coroutines.Dispatchers

data class ConcurrentModel(val flow1: Int, val flow2: Int, val flow3: Int)

sealed class ConcurrentMsg {
    object IncFlow1 : ConcurrentMsg()
    object IncFlow2 : ConcurrentMsg()
    object IncFlow3 : ConcurrentMsg()
}

fun feature(scope: CoroutineScope, repeatCount: Int) = TeaFeature(
    previousModel = null,
    init = init(repeatCount),
    update = update,
    view = { it },
    featureScope = scope,
    onEachModel = {},
    effectContext = Dispatchers.Default,
    renderContext = Dispatchers.Default
)

private fun init(repeatCount: Int): InitWithPrevious<ConcurrentModel, ConcurrentMsg> = { previous ->
    val model = previous ?: ConcurrentModel(flow1 = 0, flow2 = 0, flow3 = 0)
    model to batch(
        effect { dispatch ->
            (1..repeatCount).asFlow().collect { dispatch(ConcurrentMsg.IncFlow1) }
        },
        effect { dispatch ->
            (1..repeatCount).asFlow().collect { dispatch(ConcurrentMsg.IncFlow2) }
        },
        effect { dispatch ->
            (1..repeatCount).asFlow().collect { dispatch(ConcurrentMsg.IncFlow3) }
        }
    )
}

private val update: Update<ConcurrentModel, ConcurrentMsg> = { msg, model ->
    when (msg) {
        is ConcurrentMsg.IncFlow1 -> model.copy(flow1 = model.flow1 + 1) to none()
        is ConcurrentMsg.IncFlow2 -> model.copy(flow2 = model.flow2 + 1) to none()
        is ConcurrentMsg.IncFlow3 -> model.copy(flow3 = model.flow3 + 1) to none()
    }
}

