package ltd.evilcorp.atox.ui.settings

import androidx.lifecycle.ViewModel
import ltd.evilcorp.domain.feature.UserManager
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val userManager: UserManager
) : ViewModel() {
    fun setName(name: String) = userManager.setName(name)
    fun setStatusMessage(message: String) = userManager.setStatusMessage(message)
}
