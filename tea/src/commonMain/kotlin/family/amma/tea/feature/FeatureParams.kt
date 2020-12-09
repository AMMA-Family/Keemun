package family.amma.tea.feature

import family.amma.tea.Dispatch
import family.amma.tea.InitWithPrevious
import family.amma.tea.Update
import family.amma.tea.View

/**
 * Required parameters for creating [TeaFeature].
 * @param onEachModel A callback in which you can put the required action every time the model changes.
 * For example, printing a model to the log, sending analytics or writing to prefs,
 * so that later it can be restored at any time, even if the user leaves the screen.
 * @see InitWithPrevious
 * @see Update
 * @see View
 */
class FeatureParams<Model : Any, Msg : Any, Props : Any>(
    val init: InitWithPrevious<Model, Msg>,
    val update: Update<Model, Msg>,
    val view: View<Model, Props>,
    val onEachModel: Dispatch<Model>? = null
)
