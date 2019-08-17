package ltd.evilcorp.atox.ui.contactlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.tox.ToxThread
import ltd.evilcorp.core.repository.ContactRepository
import ltd.evilcorp.core.repository.FriendRequestRepository
import ltd.evilcorp.core.repository.UserRepository
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.FriendRequest
import ltd.evilcorp.core.vo.User
import javax.inject.Inject

class ContactListViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val friendRequestRepository: FriendRequestRepository,
    private val userRepository: UserRepository,
    private val tox: ToxThread
) : ViewModel() {
    val publicKey by lazy { tox.publicKey }
    val toxId by lazy { tox.toxId }

    val contacts: LiveData<List<Contact>> by lazy { contactRepository.getAll() }
    val friendRequests: LiveData<List<FriendRequest>> by lazy { friendRequestRepository.getAll() }
    val user: LiveData<User> by lazy { userRepository.get(publicKey) }

    fun isToxRunning() = tox.started

    fun acceptFriendRequest(friendRequest: FriendRequest) = tox.acceptFriendRequest(friendRequest.publicKey)

    fun rejectFriendRequest(friendRequest: FriendRequest) = GlobalScope.launch {
        friendRequestRepository.delete(friendRequest)
    }

    fun deleteContact(contact: Contact) = tox.deleteContact(contact.publicKey)
}
