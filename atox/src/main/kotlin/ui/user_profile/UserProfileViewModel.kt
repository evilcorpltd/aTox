// SPDX-FileCopyrightText: 2020 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.user_profile

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import ltd.evilcorp.atox.App
import ltd.evilcorp.core.vo.User
import ltd.evilcorp.core.vo.UserStatus
import ltd.evilcorp.domain.feature.UserManager
import ltd.evilcorp.domain.tox.Tox
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

class UserProfileViewModel(app: App) : AndroidViewModel(app), DIAware {
    override val di by closestDI()

    private val userManager: UserManager by instance()
    private val tox: Tox by instance()

    val publicKey by lazy { tox.publicKey }
    val toxId by lazy { tox.toxId }
    val user: LiveData<User> = userManager.get(publicKey).asLiveData()

    fun setName(name: String) = userManager.setName(name)
    fun setStatusMessage(statusMessage: String) = userManager.setStatusMessage(statusMessage)
    fun setStatus(status: UserStatus) = userManager.setStatus(status)
}
