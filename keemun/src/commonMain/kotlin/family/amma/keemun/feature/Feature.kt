package family.amma.keemun.feature

import family.amma.keemun.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * A common interface, the implementation of which controls all entities and starts the entire processing mechanism.
 * [Msg] - messages with which we will transform [State].
 */
interface Feature<State : Any, Msg : Any> {
    /** Flow of states. */
    val states: Flow<State>

    /** The main scope on which all coroutines will be launched. */
    val scope: CoroutineScope

    /** Sending messages asynchronously. */
    infix fun dispatch(msg: Msg)

    /** Sending messages synchronously. */
    suspend infix fun syncDispatch(msg: Msg)
}

internal val effectContext: CoroutineDispatcher get() = Dispatchers.Default

/**
 * [State] - common state, [Msg] - messages with which we will transform the [State].
 * @param previousState Previous state. Not null if the process was killed by the system and restored to its previous state.
 * @param initFeature Lambda [InitFeature], which creates default state and default effect.
 * @param update Lambda [Update], in which we respond to messages and update the state.
 * @param effectHandler Lambda [EffectHandler], in which we implement effects and run impure functions.
 */
class TeaFeature<State : Any, Msg : Any, Effect : Any, Deps>(
    previousState: State?,
    initFeature: InitFeature<State, Effect, Deps>,
    private val update: Update<State, Msg, Effect>,
    private val effectHandler: EffectHandler<Effect, Msg>,
    override val scope: CoroutineScope
) : Feature<State, Msg> {
    private val messageSharedFlow = MutableSharedFlow<Msg>(replay = 10)
    private val stateFlow = MutableStateFlow<State?>(value = null)

    override val states: Flow<State> get() = stateFlow.filterNotNull()

    init {
        val (preEffect, init) = initFeature
        scope.launch(effectContext) {
            val (defaultState, startEffects) = init(previousState, preEffect())
            stateFlow.value = defaultState
            observeMessages(scope = this, defaultState, startEffects)
        }
    }

    private suspend fun observeMessages(scope: CoroutineScope, defaultState: State, startEffects: Set<Effect>) {
        var currentState = defaultState
        messageSharedFlow
            .onSubscription { effectHandler.process(startEffects, scope, ::syncDispatch) }
            .collect { msg ->
                val (newState, effects) = update(msg, currentState)
                currentState = newState
                stateFlow.value = newState
                effectHandler.process(effects, scope, ::syncDispatch)
            }
    }

    override infix fun dispatch(msg: Msg) {
        scope.launch { syncDispatch(msg) }
    }

    override suspend infix fun syncDispatch(msg: Msg) {
        messageSharedFlow.emit(msg)
    }
}
