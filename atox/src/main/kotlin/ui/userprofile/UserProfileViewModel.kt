// SPDX-FileCopyrightText: 2020 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.userprofile

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import ltd.evilcorp.core.vo.User
import ltd.evilcorp.core.vo.UserStatus
import ltd.evilcorp.domain.feature.UserManager
import ltd.evilcorp.domain.tox.Tox
import javax.inject.Inject

class UserProfileViewModel @Inject constructor(
    private val userManager: UserManager,
    private val tox: Tox,
) : ViewModel() {
    val publicKey by lazy { tox.publicKey }
    val toxId by lazy { tox.toxId }
    val user: LiveData<User> = userManager.get(publicKey).asLiveData()

    fun setName(name: String) = userManager.setName(name)
    fun setStatusMessage(statusMessage: String) = userManager.setStatusMessage(statusMessage)
    fun setStatus(status: UserStatus) = userManager.setStatus(status)
}
