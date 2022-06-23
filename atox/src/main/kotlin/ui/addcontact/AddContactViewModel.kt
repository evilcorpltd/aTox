// SPDX-FileCopyrightText: 2019-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.addcontact

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import ltd.evilcorp.atox.tox.ToxStarter
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.domain.feature.ContactManager
import ltd.evilcorp.domain.tox.Tox
import ltd.evilcorp.domain.tox.ToxID
import ltd.evilcorp.domain.tox.ToxSaveStatus
import javax.inject.Inject

class AddContactViewModel @Inject constructor(
    private val contactManager: ContactManager,
    private val tox: Tox,
    private val toxStarter: ToxStarter,
) : ViewModel() {
    val toxId by lazy { tox.toxId }
    val contacts: LiveData<List<Contact>> = contactManager.getAll().asLiveData()

    fun isToxRunning() = tox.started
    fun tryLoadTox(): Boolean = toxStarter.tryLoadTox(null) == ToxSaveStatus.Ok

    fun addContact(toxId: ToxID, message: String) = contactManager.add(toxId, message)
}
