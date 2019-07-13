package ltd.evilcorp.atox.ui.contactlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.App
import ltd.evilcorp.atox.repository.ContactRepository
import ltd.evilcorp.atox.repository.FriendRequestRepository
import ltd.evilcorp.atox.tox.ToxThread
import ltd.evilcorp.atox.vo.Contact
import ltd.evilcorp.atox.vo.FriendRequest
import javax.inject.Inject

class ContactListViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val friendRequestRepository: FriendRequestRepository
) : ViewModel() {
    val contacts: LiveData<List<Contact>> by lazy { contactRepository.getAll() }
    val friendRequests: LiveData<List<FriendRequest>> by lazy { friendRequestRepository.getAll() }

    fun acceptFriendRequest(friendRequest: FriendRequest) {
        with(App.toxThread.handler) {
            sendMessage(
                obtainMessage(
                    ToxThread.msgAcceptFriendRequest,
                    friendRequest.publicKey
                )
            )
        }
    }

    fun rejectFriendRequest(friendRequest: FriendRequest) {
        GlobalScope.launch {
            friendRequestRepository.delete(friendRequest)
        }
    }

    fun deleteContact(contact: Contact) {
        with(App.toxThread.handler) {
            sendMessage(obtainMessage(ToxThread.msgDeleteContact, contact.publicKey))
        }
    }
}
