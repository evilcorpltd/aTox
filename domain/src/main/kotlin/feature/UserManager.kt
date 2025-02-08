// SPDX-FileCopyrightText: 2019-2025 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.feature

import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ltd.evilcorp.core.repository.UserRepository
import ltd.evilcorp.core.vo.PublicKey
import ltd.evilcorp.core.vo.User
import ltd.evilcorp.core.vo.UserStatus
import ltd.evilcorp.domain.tox.Tox

class UserManager @Inject constructor(
    private val scope: CoroutineScope,
    private val userRepository: UserRepository,
    private val tox: Tox,
) {
    fun get(pk: PublicKey) = userRepository.get(pk)

    fun create(user: User) = scope.launch {
        userRepository.add(user)
        tox.setName(user.name)
        tox.setStatusMessage(user.statusMessage)
    }

    fun verifyExists(pk: PublicKey) = scope.launch {
        if (!userRepository.exists(pk)) {
            val name = tox.getName()
            val statusMessage = tox.getStatusMessage()
            val user = User(pk, name, statusMessage)
            userRepository.add(user)
        }
    }

    fun setName(name: String) = scope.launch {
        tox.setName(name)
        userRepository.updateName(tox.publicKey, name)
    }

    fun setStatusMessage(statusMessage: String) = scope.launch {
        tox.setStatusMessage(statusMessage)
        userRepository.updateStatusMessage(tox.publicKey, statusMessage)
    }

    fun setStatus(status: UserStatus) = scope.launch {
        tox.setStatus(status)
        userRepository.updateStatus(tox.publicKey, status)
    }
}
