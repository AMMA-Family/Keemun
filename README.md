[![Maven Central](https://maven-badges.herokuapp.com/maven-central/family.amma/keemun/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/family.amma/keemun)

# Keemun
Keemun is multiplatform Tea library.


Add the library to your `build.gradle.kts` file.
```kotlin
implementation("family.amma:keemun:1.3.0")
```

## Multiplatform part

### Entities
```kotlin
data class State(
    val user: User?
)

sealed class Msg {
    data class UserWasLoaded(val user: User) : Msg()
}

sealed class Effect {
    object LoadUser : Effect()
}
```

### An example of creating `FeatureParams`:


```kotlin
typealias SomeFeatureParams = FeatureParams<State, Msg, Effect>
typealias SomeFeatureEffectHandler = EffectHandler<Effect, Msg>

internal fun someFeatureParams(effectHandler: SomeFeatureEffectHandler): SomeFeatureParams =
    FeatureParams(
        init = init,
        update = updater,
        effectHandler = effectHandler
    )

val init = InitFeature<State, Effect> { previous ->
    val state = previous ?: State(user = null)
    state to setOf(Effect.LoadUser)
}

val updater = Update<State, Msg> { msg, state ->
    when (msg) {
        is Msg.UserWasLoaded -> state.copy(user = msg.user) to emptySet()
    }  
}

fun effectHandler(repo: UserRepository) = SomeFeatureEffectHandler { effect, dispatch ->
    when (effect) {
        Effect.LoadUser -> {
            val user = repo.loadUser()
            dispatch(Msg.UserWasLoaded(user))
        }
    }
}
```

## Platform part (Android)

```kotlin
data class ViewState(
    // Example
    // English - "Age: 30"
    // Russian - "Возраст: 30"
    // etc
    val localizedAge: String?
)

// StateTransform<State, ViewState> = suspend (State) -> ViewState 
private fun stateTransform(getContext: () -> Context) = StateTransform<State, ViewState> { state ->
    ViewState(
        localizedAge = state.user?.age?.let { getContext().getString(R.string.age, it) }
    )
}
```

### Add to your Fragment/Activity

```kotlin
class SomeFragment(featureParams: () -> SomeFeatureParams) : Fragment(R.layout.fragment_some) {
    private val feature by androidConnectors(featureParams, getStateTransform = { stateTransform(::requireContext) })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        feature.render(this) { viewState ->
            ageTextView.text = viewState.localizedAge
        }
        // OR 
        // feature.collectWithLifecycle(viewLifecycleOwner, Lifecycle.State.STARTED) { viewState -> }
    }
}
```

### Send messages to change state

```kotlin
feature dispatch Msg.Foo
feature syncDispatch Msg.Bar
```
