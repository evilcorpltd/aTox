// SPDX-FileCopyrightText: 2019-2025 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.contactprofile

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import javax.inject.Inject
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.PublicKey
import ltd.evilcorp.domain.feature.ContactManager

class ContactProfileViewModel @Inject constructor(contactManager: ContactManager) : ViewModel() {
    var publicKey: PublicKey = PublicKey("")
    val contact: LiveData<Contact> by lazy { contactManager.get(publicKey).asLiveData() }
}
