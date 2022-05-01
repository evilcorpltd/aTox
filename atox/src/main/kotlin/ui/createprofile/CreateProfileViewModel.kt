// SPDX-FileCopyrightText: 2020-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.createprofile

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import javax.inject.Inject
import ltd.evilcorp.atox.tox.ToxStarter
import ltd.evilcorp.core.vo.User
import ltd.evilcorp.domain.feature.UserManager
import ltd.evilcorp.domain.tox.PublicKey
import ltd.evilcorp.domain.tox.Tox
import ltd.evilcorp.domain.tox.ToxSaveStatus

class CreateProfileViewModel @Inject constructor(
    private val resolver: ContentResolver,
    private val userManager: UserManager,
    private val tox: Tox,
    private val toxStarter: ToxStarter,
) : ViewModel() {
    val publicKey: PublicKey by lazy { tox.publicKey }

    fun startTox(save: ByteArray? = null, password: String? = null): ToxSaveStatus = toxStarter.startTox(save, password)
    fun tryImportToxSave(uri: Uri): ByteArray? = resolver.openInputStream(uri)?.readBytes()
    fun create(user: User) = userManager.create(user)
    fun verifyUserExists(publicKey: PublicKey) = userManager.verifyExists(publicKey)
}
