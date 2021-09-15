# Keemun
Add the library to your `build.gradle.kts` file.
```kotlin
implementation("family.amma:keemun:1.1.0")
```

If you want to use old version
```kotlin
implementation("family.amma:tea:2.2.4")
```


## Multiplatform part

### Entities
```kotlin
@Parcelize
data class Model(
    val user: User?
) : Parcelable

data class Props(
    // Example
    // English - "Age: 30"
    // Russian - "Возраст: 30"
    // etc
    val localizedAge: String?
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
typealias SomeFeatureParams = FeatureParams<Model, Msg, Effect>
typealias SomeFeatureEffectHandler = EffectHandler<Effect, Msg>

internal fun someFeatureParams(effectHandler: SomeFeatureEffectHandler): SomeFeatureParams =
    FeatureParams(
        init = init,
        update = modelUpdater,
        effectHandler = effectHandler
    )

val init = InitFeature<Model, Effect> { previous ->
    val model = previous ?: Model(user = null)
    model to setOf(Effect.LoadUser)
}

val updater = Update<Model, Msg> { msg, model ->
    when (msg) {
        is Msg.UserWasLoaded -> model.copy(user = msg.user) to emptySet()
    }  
}

fun effectHandler(repo: UserRepository): SomeFeatureEffectHandler = EffectHandler { effect, dispatch ->
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
// ViewState<Model, Props> = suspend (Model) -> Props 
private fun someViewState(getContext: () -> Context): ViewState<Model, Props> = { model ->
    Props(
        localizedAge = model.user?.age?.let { getContext().getString(R.string.age, it) }
    )
}
```

### Add to your Fragment/Activity

```kotlin
class SomeFragment(featureParams: () -> SomeFeatureParams) : Fragment(R.layout.fragment_some) {
    private val feature by androidConnectors(featureParams, viewState = someViewState(::requireContext))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        feature.render(this) { props ->
            ageTextView.text = props.localizedAge
        }
        // OR 
        // feature.collectWithLifecycle(viewLifecycleOwner, Lifecycle.State.STARTED) { props -> }
    }
}
```

### Send messages to change state

```kotlin
feature dispatch Msg.Foo
feature syncDispatch Msg.Bar
```
