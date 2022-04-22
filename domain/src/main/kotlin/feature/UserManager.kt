// SPDX-FileCopyrightText: 2019-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.feature

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ltd.evilcorp.core.repository.UserRepository
import ltd.evilcorp.core.vo.User
import ltd.evilcorp.core.vo.UserStatus
import ltd.evilcorp.domain.tox.PublicKey
import ltd.evilcorp.domain.tox.Tox
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class UserManager(override val di: DI) : DIAware {
    private val scope: CoroutineScope by instance()
    private val userRepository: UserRepository by instance()
    private val tox: Tox by instance()

    fun get(publicKey: PublicKey) = userRepository.get(publicKey.string())

    fun create(user: User) = scope.launch {
        userRepository.add(user)
        tox.setName(user.name)
        tox.setStatusMessage(user.statusMessage)
    }

    fun verifyExists(publicKey: PublicKey) = scope.launch {
        if (!userRepository.exists(publicKey.string())) {
            val name = tox.getName()
            val statusMessage = tox.getStatusMessage()
            val user = User(publicKey.string(), name, statusMessage)
            userRepository.add(user)
        }
    }

    fun setName(name: String) = scope.launch {
        tox.setName(name)
        userRepository.updateName(tox.publicKey.string(), name)
    }

    fun setStatusMessage(statusMessage: String) = scope.launch {
        tox.setStatusMessage(statusMessage)
        userRepository.updateStatusMessage(tox.publicKey.string(), statusMessage)
    }

    fun setStatus(status: UserStatus) = scope.launch {
        tox.setStatus(status)
        userRepository.updateStatus(tox.publicKey.string(), status)
    }
}
