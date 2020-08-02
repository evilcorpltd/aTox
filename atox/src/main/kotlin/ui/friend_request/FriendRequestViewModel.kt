package ltd.evilcorp.atox.ui.friend_request

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject
import ltd.evilcorp.core.vo.FriendRequest
import ltd.evilcorp.domain.feature.FriendRequestManager
import ltd.evilcorp.domain.tox.PublicKey

class FriendRequestViewModel @Inject constructor(
    private val friendRequests: FriendRequestManager
) : ViewModel() {
    fun byId(pk: PublicKey): LiveData<FriendRequest> = friendRequests.get(pk)
    fun accept(request: FriendRequest) = friendRequests.accept(request)
    fun reject(request: FriendRequest) = friendRequests.reject(request)
}
