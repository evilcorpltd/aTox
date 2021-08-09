// SPDX-FileCopyrightText: 2019-2020 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.core.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import ltd.evilcorp.core.db.UserDao
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.User
import ltd.evilcorp.core.vo.UserStatus

@Singleton
class UserRepository @Inject internal constructor(
    private val userDao: UserDao
) {
    fun exists(publicKey: String): Boolean =
        userDao.exists(publicKey)

    fun add(user: User) =
        userDao.save(user)

    fun update(user: User) =
        userDao.update(user)

    fun get(publicKey: String): Flow<User> =
        userDao.load(publicKey)

    fun updateName(publicKey: String, name: String) =
        userDao.updateName(publicKey, name)

    fun updateStatusMessage(publicKey: String, statusMessage: String) =
        userDao.updateStatusMessage(publicKey, statusMessage)

    fun updateConnection(publicKey: String, connectionStatus: ConnectionStatus) =
        userDao.updateConnection(publicKey, connectionStatus)

    fun updateStatus(publicKey: String, status: UserStatus) =
        userDao.updateStatus(publicKey, status)
}
