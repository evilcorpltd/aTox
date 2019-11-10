package ltd.evilcorp.atox.feature

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ltd.evilcorp.atox.tox.PublicKey
import ltd.evilcorp.atox.tox.Tox
import ltd.evilcorp.core.repository.ContactRepository
import ltd.evilcorp.core.repository.FriendRequestRepository
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.FriendRequest
import javax.inject.Inject

class FriendRequestManager @Inject constructor(
    private val contactRepository: ContactRepository,
    private val friendRequestRepository: FriendRequestRepository,
    private val tox: Tox
) : CoroutineScope by GlobalScope {
    fun getAll(): LiveData<List<FriendRequest>> = friendRequestRepository.getAll()

    fun accept(friendRequest: FriendRequest) = launch {
        tox.acceptFriendRequest(PublicKey(friendRequest.publicKey))
        contactRepository.add(Contact(friendRequest.publicKey))
        friendRequestRepository.delete(friendRequest)
    }

    fun reject(friendRequest: FriendRequest) = launch { friendRequestRepository.delete(friendRequest) }
}
