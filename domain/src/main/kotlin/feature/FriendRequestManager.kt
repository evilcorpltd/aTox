// SPDX-FileCopyrightText: 2019-2021 Robin Lindén
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.feature

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ltd.evilcorp.core.repository.ContactRepository
import ltd.evilcorp.core.repository.FriendRequestRepository
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.FriendRequest
import ltd.evilcorp.domain.tox.PublicKey
import ltd.evilcorp.domain.tox.Tox
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class FriendRequestManager(override val di: DI) : DIAware {
    private val scope: CoroutineScope by instance()
    private val contactRepository: ContactRepository by instance()
    private val friendRequestRepository: FriendRequestRepository by instance()
    private val tox: Tox by instance()

    fun getAll(): Flow<List<FriendRequest>> = friendRequestRepository.getAll()
    fun get(id: PublicKey): Flow<FriendRequest> = friendRequestRepository.get(id.string())

    fun accept(friendRequest: FriendRequest) = scope.launch {
        tox.acceptFriendRequest(PublicKey(friendRequest.publicKey))
        contactRepository.add(Contact(friendRequest.publicKey))
        friendRequestRepository.delete(friendRequest)
    }

    fun reject(friendRequest: FriendRequest) = scope.launch { friendRequestRepository.delete(friendRequest) }
}
