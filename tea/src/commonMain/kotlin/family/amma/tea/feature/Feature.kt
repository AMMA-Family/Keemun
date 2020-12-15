package family.amma.tea.feature

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import family.amma.tea.Dispatch
import family.amma.tea.Effect
import family.amma.tea.InitWithPrevious
import family.amma.tea.Update
import family.amma.tea.View

/**
 * A common interface, the implementation of which controls all entities and starts the entire processing mechanism.
 * [Props] - view state, [Msg] - messages with which we will transform the Model.
 */
interface Feature<Msg : Any, Props : Any> {
    /** [Props] to render to the screen. */
    val props: Flow<Props>

    /** Feature scope. */
    val scope: CoroutineScope

    /** Sending messages asynchronously. */
    infix fun dispatch(msg: Msg)

    /** Sending messages synchronously. */
    suspend fun syncDispatch(msg: Msg)

    suspend fun <OtherMsg : Any> bind(other: Feature<OtherMsg, *>, transform: (Msg) -> Effect<OtherMsg>)
}

/** Sequential sending of an unspecified number of messages. */
inline infix fun <Msg : Any, Props : Any> Feature<Msg, Props>.sequentialDispatch(
    crossinline lambda: suspend (dispatch: Dispatch<Msg>) -> Unit
) {
    scope.launch { lambda.invoke(::syncDispatch) }
}

/**
 * [Model] - common state, [Props] - view state, [Msg] - messages with which we will transform the [Model].
 * @param previousModel Previous state. Not null if the process was killed by the system and restored to its previous state.
 * @param init Lambda [InitWithPrevious], which creates default state and default effect.
 * @param update Lambda [Update], in which we respond to messages and update the model.
 * @param view A lambda [View] that maps [Model] to [Props].
 * @param featureScope The main scope on which all coroutines will be launched.
 * @param onEachModel The callback that we will pull on every time the model is updated.
 * @param effectContext The context in which the effects will run.
 * @param renderContext The context in which we will render the interface.
 */
class TeaFeature<Model : Any, Msg : Any, Props : Any>(
    previousModel: Model?,
    init: InitWithPrevious<Model, Msg>,
    private val update: Update<Model, Msg>,
    private val view: View<Model, Props>,
    featureScope: CoroutineScope,
    private val onEachModel: Dispatch<Model>,
    private val effectContext: CoroutineDispatcher,
    private val renderContext: CoroutineDispatcher
) : Feature<Msg, Props>, CoroutineScope by featureScope {
    private val messageSharedFlow = MutableSharedFlow<Msg>()
    private val states: MutableStateFlow<Model>

    override val props: Flow<Props>
        @OptIn(ExperimentalCoroutinesApi::class)
        get() = states.mapLatest(view::invoke).flowOn(Dispatchers.Default)

    override val scope: CoroutineScope get() = this

    init {
        val (defaultModel, startEffect) = init(previousModel)
        states = MutableStateFlow(defaultModel)
        launch(renderContext) { states.collect(onEachModel) }
        launch(effectContext) {
            messageSharedFlow
                .onSubscription { startEffect?.let(::schedule) }
                .collect(::handle)
        }
    }

    private fun handle(msg: Msg) {
        val (newState, effect) = update(msg, states.value)
        states.value = newState
        effect?.let(::schedule)
    }

    override infix fun dispatch(msg: Msg) {
        scope.launch { syncDispatch(msg) }
    }

    override suspend fun syncDispatch(msg: Msg) {
        messageSharedFlow.emit(msg)
    }

    private fun schedule(effect: Effect<Msg>) =
        launch(effectContext) { effect(::syncDispatch) }

    override suspend fun <OtherMsg : Any> bind(other: Feature<OtherMsg, *>, transform: (Msg) -> Effect<OtherMsg>) {
        withContext(effectContext) {
            messageSharedFlow.collect { msg ->
                transform(msg).invoke(this, other::syncDispatch)
            }
        }
    }
}
