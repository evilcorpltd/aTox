// SPDX-FileCopyrightText: 2019-2025 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.core.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import ltd.evilcorp.core.db.UserDao
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.PublicKey
import ltd.evilcorp.core.vo.User
import ltd.evilcorp.core.vo.UserStatus

@Singleton
class UserRepository @Inject constructor(private val userDao: UserDao) {
    fun exists(pk: PublicKey): Boolean = userDao.exists(pk)

    fun add(user: User) = userDao.save(user)

    fun update(user: User) = userDao.update(user)

    fun get(pk: PublicKey): Flow<User> = userDao.load(pk)

    fun updateName(pk: PublicKey, name: String) = userDao.updateName(pk, name)

    fun updateStatusMessage(pk: PublicKey, statusMessage: String) = userDao.updateStatusMessage(pk, statusMessage)

    fun updateConnection(pk: PublicKey, connectionStatus: ConnectionStatus) =
        userDao.updateConnection(pk, connectionStatus)

    fun updateStatus(pk: PublicKey, status: UserStatus) = userDao.updateStatus(pk, status)
}
