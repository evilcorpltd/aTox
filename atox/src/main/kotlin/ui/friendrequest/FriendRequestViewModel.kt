// SPDX-FileCopyrightText: 2020 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.friendrequest

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import ltd.evilcorp.core.vo.FriendRequest
import ltd.evilcorp.domain.feature.FriendRequestManager
import ltd.evilcorp.domain.tox.PublicKey
import javax.inject.Inject

class FriendRequestViewModel @Inject constructor(
    private val friendRequests: FriendRequestManager,
) : ViewModel() {
    fun byId(pk: PublicKey): LiveData<FriendRequest> = friendRequests.get(pk).asLiveData()
    fun accept(request: FriendRequest) = friendRequests.accept(request)
    fun reject(request: FriendRequest) = friendRequests.reject(request)
}
