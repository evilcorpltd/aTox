// SPDX-FileCopyrightText: 2020-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.create_profile

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import ltd.evilcorp.atox.App
import ltd.evilcorp.atox.tox.ToxStarter
import ltd.evilcorp.core.vo.User
import ltd.evilcorp.domain.feature.UserManager
import ltd.evilcorp.domain.tox.PublicKey
import ltd.evilcorp.domain.tox.Tox
import ltd.evilcorp.domain.tox.ToxSaveStatus
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

class CreateProfileViewModel(app: App) : AndroidViewModel(app), DIAware {
    override val di by closestDI()

    private val resolver: ContentResolver by instance()
    private val userManager: UserManager by instance()
    private val tox: Tox by instance()
    private val toxStarter: ToxStarter by instance()

    val publicKey: PublicKey by lazy { tox.publicKey }

    fun startTox(save: ByteArray? = null, password: String? = null): ToxSaveStatus = toxStarter.startTox(save, password)
    fun tryImportToxSave(uri: Uri): ByteArray? = resolver.openInputStream(uri)?.readBytes()
    fun create(user: User) = userManager.create(user)
    fun verifyUserExists(publicKey: PublicKey) = userManager.verifyExists(publicKey)
}
