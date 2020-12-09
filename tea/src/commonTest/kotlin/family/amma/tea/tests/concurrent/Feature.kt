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
    data class ChangeFlow1(val newValue: Int) : ConcurrentMsg()
    data class ChangeFlow2(val newValue: Int) : ConcurrentMsg()
    data class ChangeFlow3(val newValue: Int) : ConcurrentMsg()
}

fun feature(scope: CoroutineScope, repeatCount: Int) = TeaFeature(
    previousModel = null,
    init = init(repeatCount),
    update = update,
    view = { it },
    featureScope = scope,
    onEachModel = {},
    renderContext = Dispatchers.Default
)

private fun init(repeatCount: Int): InitWithPrevious<ConcurrentModel, ConcurrentMsg> = { previous ->
    val model = previous ?: ConcurrentModel(flow1 = 0, flow2 = 0, flow3 = 0)
    model to batch(
        effect { dispatch ->
            (1..repeatCount).asFlow().collect { dispatch(ConcurrentMsg.ChangeFlow1(it)) }
        },
        effect { dispatch ->
            (1..repeatCount).asFlow().collect { dispatch(ConcurrentMsg.ChangeFlow2(it)) }
        },
        effect { dispatch ->
            (1..repeatCount).asFlow().collect { dispatch(ConcurrentMsg.ChangeFlow3(it)) }
        }
    )
}

private val update: Update<ConcurrentModel, ConcurrentMsg> = { msg, model ->
    when (msg) {
        is ConcurrentMsg.ChangeFlow1 -> model.copy(flow1 = msg.newValue) to none()
        is ConcurrentMsg.ChangeFlow2 -> model.copy(flow2 = msg.newValue) to none()
        is ConcurrentMsg.ChangeFlow3 -> model.copy(flow3 = msg.newValue) to none()
    }
}

