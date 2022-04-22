// SPDX-FileCopyrightText: 2019-2020 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.core.repository

import kotlinx.coroutines.flow.Flow
import ltd.evilcorp.core.db.FriendRequestDao
import ltd.evilcorp.core.vo.FriendRequest
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class FriendRequestRepository(override val di: DI) : DIAware {
    private val friendRequestDao: FriendRequestDao by instance()

    fun add(friendRequest: FriendRequest) =
        friendRequestDao.save(friendRequest)

    fun delete(friendRequest: FriendRequest) =
        friendRequestDao.delete(friendRequest)

    fun getAll(): Flow<List<FriendRequest>> =
        friendRequestDao.loadAll()

    fun get(publicKey: String): Flow<FriendRequest> =
        friendRequestDao.load(publicKey)
}
