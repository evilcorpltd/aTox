package ltd.evilcorp.atox.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import ltd.evilcorp.core.vo.User
import ltd.evilcorp.domain.feature.UserManager
import ltd.evilcorp.domain.tox.Tox
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val userManager: UserManager,
    private val tox: Tox
) : ViewModel() {
    private val publicKey by lazy { tox.publicKey }

    val user: LiveData<User> by lazy { userManager.get(publicKey) }

    fun setName(name: String) = userManager.setName(name)
    fun setStatusMessage(message: String) = userManager.setStatusMessage(message)
}
