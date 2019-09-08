package ltd.evilcorp.atox.ui.contactlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import ltd.evilcorp.atox.feature.ContactManager
import ltd.evilcorp.atox.feature.FriendRequestManager
import ltd.evilcorp.atox.feature.UserManager
import ltd.evilcorp.atox.tox.PublicKey
import ltd.evilcorp.atox.tox.Tox
import ltd.evilcorp.core.vo.FriendRequest
import ltd.evilcorp.core.vo.User
import javax.inject.Inject

class ContactListViewModel @Inject constructor(
    private val contactManager: ContactManager,
    private val friendRequestManager: FriendRequestManager,
    private val userManager: UserManager,
    private val tox: Tox
) : ViewModel() {
    val publicKey by lazy { tox.publicKey }
    val toxId by lazy { tox.toxId }

    val user: LiveData<User> by lazy { userManager.get(publicKey) }
    val contacts = contactManager.getAll()
    val friendRequests = friendRequestManager.getAll()

    fun isToxRunning() = tox.started

    fun acceptFriendRequest(friendRequest: FriendRequest) = friendRequestManager.accept(friendRequest)
    fun rejectFriendRequest(friendRequest: FriendRequest) = friendRequestManager.reject(friendRequest)
    fun deleteContact(publicKey: PublicKey) = contactManager.delete(publicKey)
}
