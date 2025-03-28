// SPDX-FileCopyrightText: 2019-2025 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.core.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import ltd.evilcorp.core.db.FriendRequestDao
import ltd.evilcorp.core.vo.FriendRequest
import ltd.evilcorp.core.vo.PublicKey

@Singleton
class FriendRequestRepository @Inject internal constructor(private val friendRequestDao: FriendRequestDao) {
    fun add(friendRequest: FriendRequest) = friendRequestDao.save(friendRequest)

    fun delete(friendRequest: FriendRequest) = friendRequestDao.delete(friendRequest)

    fun getAll(): Flow<List<FriendRequest>> = friendRequestDao.loadAll()

    fun get(pk: PublicKey): Flow<FriendRequest> = friendRequestDao.load(pk)

    fun count(): Int = friendRequestDao.count()
}
