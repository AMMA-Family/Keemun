package family.amma.keemun.feature

import family.amma.keemun.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

/**
 * [State] - common state, [Msg] - messages with which we will transform the [State].
 * @param previousState Previous state. Not null if the process was killed by the system and restored to its previous state.
 * @param initFeature Lambda [Init], which creates default state and default effect.
 * @param update Lambda [Update], in which we respond to messages and update the state.
 * @param effectHandlers Set of [EffectHandler], in which we implement effects and run impure functions.
 */
class TeaFeature<State : Any, Msg : Any, Effect : Any, Deps>(
    previousState: State?,
    initFeature: InitFeature<State, Effect, Deps>,
    private val update: Update<State, Msg, Effect>,
    private val effectHandlers: Set<EffectHandler<Effect, Msg>>,
    coroutineScope: CoroutineScope
) : Feature<State, Msg> {
    private val messageSharedFlow = MutableSharedFlow<Msg>(replay = 10)
    private val statesFlow = MutableSharedFlow<State>()

    override val states: SharedFlow<State> get() = statesFlow.asSharedFlow()
    override val scope: CoroutineScope = coroutineScope + Dispatchers.Default

    init {
        val (preEffect, init) = initFeature
        scope.launch {
            val (defaultState, startEffects) = init(previousState, preEffect())
            statesFlow.emit(defaultState)
            observeMessages(scope = this, defaultState, startEffects)
        }
    }

    private suspend fun observeMessages(scope: CoroutineScope, defaultState: State, startEffects: Set<Effect>) {
        var currentState = defaultState
        messageSharedFlow
            .onSubscription { effectHandlers.forEach { it.process(startEffects, scope, ::syncDispatch) } }
            .collect { msg ->
                val (newState, effects) = update(msg, currentState)
                currentState = newState
                statesFlow.emit(newState)
                effectHandlers.forEach { it.process(effects, scope, ::syncDispatch) }
            }
    }

    override infix fun dispatch(msg: Msg) {
        scope.launch { syncDispatch(msg) }
    }

    override suspend infix fun syncDispatch(msg: Msg) {
        messageSharedFlow.emit(msg)
    }
}
