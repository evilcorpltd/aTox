package ltd.evilcorp.atox.ui.edit_user_profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import ltd.evilcorp.core.vo.User
import ltd.evilcorp.core.vo.UserStatus
import ltd.evilcorp.domain.feature.UserManager
import ltd.evilcorp.domain.tox.Tox
import javax.inject.Inject

class EditUserProfileViewModel @Inject constructor(
    private val userManager: UserManager,
    private val tox: Tox
) : ViewModel() {
    val publicKey by lazy { tox.publicKey }
    val user: LiveData<User> = userManager.get(publicKey).asLiveData()
    var statusModifiedFromDropdown: Boolean = false

    fun setName(name: String) = userManager.setName(name)
    fun setStatusMessage(statusMessage: String) = userManager.setStatusMessage(statusMessage)
    fun setStatus(status: UserStatus) {
        statusModifiedFromDropdown = true
        userManager.setStatus(status)
    }
}
