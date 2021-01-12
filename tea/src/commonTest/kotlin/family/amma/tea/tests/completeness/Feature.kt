package family.amma.tea.tests.completeness

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import family.amma.tea.*
import family.amma.tea.feature.TeaFeature

data class CompletenessModel(val flow1: Int, val flow2: Int, val flow3: Int)

sealed class CompletenessMsg {
    object IncFlow1 : CompletenessMsg()
    object IncFlow2 : CompletenessMsg()
    object IncFlow3 : CompletenessMsg()
}

fun feature(scope: CoroutineScope, repeatCount: Int) = TeaFeature(
    previousState = null,
    featureScope = scope,
    initFeature = InitFeature(initWithPrevious(repeatCount)),
    update = update,
)

private fun initWithPrevious(repeatCount: Int): InitWithPrevious<CompletenessModel, CompletenessMsg> = { previous ->
    val model = previous ?: CompletenessModel(flow1 = 0, flow2 = 0, flow3 = 0)
    model to batch(
        effect { dispatch ->
            (1..repeatCount).asFlow().collect { dispatch(CompletenessMsg.IncFlow1) }
        },
        effect { dispatch ->
            (1..repeatCount).asFlow().collect { dispatch(CompletenessMsg.IncFlow2) }
        },
        effect { dispatch ->
            (1..repeatCount).asFlow().collect { dispatch(CompletenessMsg.IncFlow3) }
        }
    )
}

private val update: Update<CompletenessModel, CompletenessMsg> = { msg, model ->
    when (msg) {
        CompletenessMsg.IncFlow1 -> model.copy(flow1 = model.flow1 + 1) to none()
        CompletenessMsg.IncFlow2 -> model.copy(flow2 = model.flow2 + 1) to none()
        CompletenessMsg.IncFlow3 -> model.copy(flow3 = model.flow3 + 1) to none()
    }
}
