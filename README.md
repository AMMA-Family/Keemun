# multiplatform-tea-architecture

## How to use:

### Add to your Fragment/Activity

```kotlin
// featureParams: () -> SomeFeatureParams
private val feature by androidConnectors(featureParams = featureParams)
```

### Entities
```kotlin
@Parcelize
data class Model(
    val name: String
) : Parcelable

data class Props(
    val nameLength: Int
)

sealed class Msg {
    object LoadUserName : Msg()
    data class UserNameWasLoaded(val name: String) : Msg()
}
```

### An example of creating `FeatureParams`:

```kotlin
typealias SomeFeatureParams = FeatureParams<Model, Msg, Props>

internal fun someFeatureParams(repo: Repository): SomeFeatureParams =
    FeatureParams(
        init = init,
        update = ModelUpdater(repo).update,
        view = view
    )
    
private val init: InitWithPrevious<Model, Msg> = { previous: Model? ->
    (previous ?: Model(
        name = null
    )) to effect(Msg.LoadUserName)
}

private val view: View<Model, Props> = { model ->
    Props(
        nameLength = model.name.length
    )
}

class ModelUpdater(private val repo: Repository) : Update<Model, Msg> {
    override fun invoke(msg: Msg, model: Model) =
        when (msg) {
            is Msg.LoadUserName -> model to effect { dispatch ->
                val name = repo.loadUserName()
                dispatch(Msg.UserNameWasLoaded(name))
            }   
            is Msg.UserNameWasLoaded -> model.copy(name = msg.name) to none()
        }
}
```

### Subscribe to the state

```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    feature.render(this) { props ->
        nameLengthTextView.text = props.nameLength
    }
    // OR 
    // feature.collectWithLifecycle(viewLifecycleOwner) { props -> }
}
```

### Send messages to change state
```kotlin
feature.dispatch(Msg.Foo)
feature dispatch Msg.Bar
```
