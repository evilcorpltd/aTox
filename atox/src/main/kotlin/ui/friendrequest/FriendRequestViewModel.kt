// SPDX-FileCopyrightText: 2020-2022 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.friendrequest

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ltd.evilcorp.core.repository.MessageRepository
import ltd.evilcorp.core.vo.FriendRequest
import ltd.evilcorp.core.vo.Message
import ltd.evilcorp.core.vo.MessageType
import ltd.evilcorp.core.vo.Sender
import ltd.evilcorp.domain.feature.FriendRequestManager
import ltd.evilcorp.domain.tox.PublicKey

class FriendRequestViewModel @Inject constructor(
    private val scope: CoroutineScope,
    private val friendRequests: FriendRequestManager,
    private val messageRepository: MessageRepository,
) : ViewModel() {
    fun byId(pk: PublicKey): LiveData<FriendRequest> = friendRequests.get(pk).asLiveData()
    fun accept(request: FriendRequest) = friendRequests.accept(request)
    fun reject(request: FriendRequest) = friendRequests.reject(request)
    fun addToChatLog(request: FriendRequest) = scope.launch {
        messageRepository.add(
            Message(
                request.publicKey,
                request.message,
                Sender.Received,
                MessageType.Normal,
                0,
                Date().time,
            ),
        )
    }
}
