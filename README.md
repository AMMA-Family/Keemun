# Multiplatform TEA Architecture
Add the library to your `build.gradle.kts` file.
```kotlin
implementation("family.amma:tea:2.2.3")
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
```

### An example of creating `FeatureParams`:

```kotlin
typealias SomeFeatureParams = FeatureParams<Model, Msg>

internal fun someFeatureParams(repo: UserRepository): SomeFeatureParams =
    FeatureParams(
        init = init(repo),
        update = ModelUpdater(),
    )
    
private fun init(repo: UserRepository): InitWithPrevious<Model, Msg> = { previous: Model? ->
    (previous ?: Model(
        user = null
    )) to effect{ dispatch ->
        val user = repo.loadUser()
        dispatch(Msg.UserWasLoaded(user))
    }   
}

class ModelUpdater : Update<Model, Msg> {
    override fun invoke(msg: Msg, model: Model) =
        when (msg) {
            is Msg.UserWasLoaded -> model.copy(user = msg.user) to none()
        }
}
```

## Platform part (Android)

```kotlin
// ViewState<Model, Props> = suspend (Model) -> Props 
private fun someViewState(getContext: () -> Context): ViewState<Model, Props> = { model ->
    Props(
        localizedAge = model.user?.age?.let { context.getString(R.string.age, it) }
    )
}
```

### Add to your Fragment/Activity

```kotlin
class SomeFragment(featureParams: () -> SomeFeatureParams) : Fragment(R.layout.fragment_some) {
    private val feature by androidConnectors(featureParams, viewState = someViewState(::context))

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
feature.dispatch(Msg.Foo)
feature dispatch Msg.Bar
```
