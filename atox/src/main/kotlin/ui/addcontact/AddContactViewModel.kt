// SPDX-FileCopyrightText: 2019-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.addcontact

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import ltd.evilcorp.atox.App
import ltd.evilcorp.atox.tox.ToxStarter
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.domain.feature.ContactManager
import ltd.evilcorp.domain.tox.Tox
import ltd.evilcorp.domain.tox.ToxID
import ltd.evilcorp.domain.tox.ToxSaveStatus
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

class AddContactViewModel(app: App) : AndroidViewModel(app), DIAware {
    override val di by closestDI()

    private val contactManager: ContactManager by instance()
    private val tox: Tox by instance()
    private val toxStarter: ToxStarter by instance()

    val toxId by lazy { tox.toxId }
    val contacts: LiveData<List<Contact>> = contactManager.getAll().asLiveData()

    fun isToxRunning() = tox.started
    fun tryLoadTox(): Boolean = toxStarter.tryLoadTox(null) == ToxSaveStatus.Ok

    fun addContact(toxId: ToxID, message: String) = contactManager.add(toxId, message)
}
