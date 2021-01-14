package family.amma.tea

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import family.amma.tea.feature.Binder
import kotlinx.coroutines.CoroutineScope

/** Android implementation of binding. */
open class AndroidBinder : Binder, ViewModel() {
    override val scope: CoroutineScope = viewModelScope
}
