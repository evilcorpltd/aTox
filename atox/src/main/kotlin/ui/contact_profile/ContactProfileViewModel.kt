// SPDX-FileCopyrightText: 2019-2020 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.contact_profile

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import ltd.evilcorp.atox.App
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.domain.feature.ContactManager
import ltd.evilcorp.domain.tox.PublicKey
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

class ContactProfileViewModel(app: App) : AndroidViewModel(app), DIAware {
    override val di by closestDI()

    private val contactManager: ContactManager by instance()

    var publicKey: PublicKey = PublicKey("")
    val contact: LiveData<Contact> by lazy { contactManager.get(publicKey).asLiveData() }
}
