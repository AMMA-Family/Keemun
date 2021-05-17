package family.amma.tea.feature

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import family.amma.tea.Effect
import family.amma.tea.InitFeature
import family.amma.tea.InitWithPrevious
import family.amma.tea.Update

/**
 * A common interface, the implementation of which controls all entities and starts the entire processing mechanism.
 * [Msg] - messages with which we will transform [State].
 */
interface Feature<State : Any, Msg : Any> {
    /** Flow of states. */
    val states: Flow<State>

    /** Flow of messages. */
    val messages: SharedFlow<Msg>

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
 * @param initFeature Lambda [InitWithPrevious], which creates default state and default effect.
 * @param update Lambda [Update], in which we respond to messages and update the state.
 * @param featureScope The main scope on which all coroutines will be launched.
 */
class TeaFeature<State : Any, Msg : Any, Deps>(
    previousState: State?,
    initFeature: InitFeature<State, Msg, Deps>,
    private val update: Update<State, Msg>,
    featureScope: CoroutineScope
) : Feature<State, Msg>, CoroutineScope by featureScope {
    private val messageSharedFlow = MutableSharedFlow<Msg>(replay = 10)
    private val stateFlow = MutableStateFlow<State?>(null)

    override val states: Flow<State> get() = stateFlow.filterNotNull()
    override val messages: SharedFlow<Msg> get() = messageSharedFlow
    override val scope: CoroutineScope get() = this

    init {
        val (preEffect, init) = initFeature
        launch(effectContext) {
            val (defaultState, startEffect) = init(previousState, preEffect())
            stateFlow.value = defaultState
            observeMessages(defaultState, startEffect)
        }
    }

    private suspend fun observeMessages(defaultState: State, startEffect: Effect<Msg>?) {
        var currentState = defaultState
        messages
            .onSubscription { startEffect?.let(::schedule) }
            .collect { msg ->
                val (newState, effect) = update(msg, currentState)
                currentState = newState
                stateFlow.value = newState
                effect?.let(::schedule)
            }
    }

    override infix fun dispatch(msg: Msg) {
        scope.launch { syncDispatch(msg) }
    }

    override suspend infix fun syncDispatch(msg: Msg) {
        messageSharedFlow.emit(msg)
    }

    private fun schedule(effect: Effect<Msg>) =
        launch(effectContext) { effect(::syncDispatch) }
}
