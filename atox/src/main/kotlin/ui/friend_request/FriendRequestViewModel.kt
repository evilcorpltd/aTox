// SPDX-FileCopyrightText: 2020 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.friend_request

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import ltd.evilcorp.atox.App
import ltd.evilcorp.core.vo.FriendRequest
import ltd.evilcorp.domain.feature.FriendRequestManager
import ltd.evilcorp.domain.tox.PublicKey
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

class FriendRequestViewModel(app: App) : AndroidViewModel(app), DIAware {
    override val di by closestDI()

    private val friendRequests: FriendRequestManager by instance()

    fun byId(pk: PublicKey): LiveData<FriendRequest> = friendRequests.get(pk).asLiveData()
    fun accept(request: FriendRequest) = friendRequests.accept(request)
    fun reject(request: FriendRequest) = friendRequests.reject(request)
}
