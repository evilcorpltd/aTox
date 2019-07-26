package ltd.evilcorp.atox.ui.contactlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.App
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
    private val userRepository: UserRepository
) : ViewModel() {
    lateinit var publicKey: String

    val contacts: LiveData<List<Contact>> by lazy { contactRepository.getAll() }
    val friendRequests: LiveData<List<FriendRequest>> by lazy { friendRequestRepository.getAll() }
    val user: LiveData<User> by lazy { userRepository.get(publicKey) }

    fun acceptFriendRequest(friendRequest: FriendRequest) = App.toxThread.acceptFriendRequest(friendRequest.publicKey)

    fun rejectFriendRequest(friendRequest: FriendRequest) = GlobalScope.launch {
        friendRequestRepository.delete(friendRequest)
    }

    fun deleteContact(contact: Contact) = App.toxThread.deleteContact(contact.publicKey)
}
