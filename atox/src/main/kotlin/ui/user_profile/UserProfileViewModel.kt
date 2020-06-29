package ltd.evilcorp.atox.ui.user_profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject
import ltd.evilcorp.core.vo.User
import ltd.evilcorp.core.vo.UserStatus
import ltd.evilcorp.domain.feature.UserManager
import ltd.evilcorp.domain.tox.Tox

class UserProfileViewModel @Inject constructor(
    private val userManager: UserManager,
    private val tox: Tox
) : ViewModel() {
    val publicKey by lazy { tox.publicKey }
    val toxId by lazy { tox.toxId }
    val user: LiveData<User> by lazy { userManager.get(publicKey) }

    fun setName(name: String) = userManager.setName(name)
    fun setStatusMessage(statusMessage: String) = userManager.setStatusMessage(statusMessage)
    fun setStatus(status: UserStatus) = userManager.setStatus(status)
}
