// SPDX-FileCopyrightText: 2019-2021 Robin Lindén
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.feature

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ltd.evilcorp.core.repository.ContactRepository
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.domain.tox.PublicKey
import ltd.evilcorp.domain.tox.Tox
import ltd.evilcorp.domain.tox.ToxID
import java.util.Date
import javax.inject.Inject

class ContactManager @Inject constructor(
    private val scope: CoroutineScope,
    private val contactRepository: ContactRepository,
    private val tox: Tox,
) {
    fun get(publicKey: PublicKey) = contactRepository.get(publicKey.string())
    fun getAll() = contactRepository.getAll()

    fun add(toxID: ToxID, message: String) = scope.launch {
        val publicKeyTxt = toxID.toPublicKey().string()
        tox.addContact(toxID, message)
        contactRepository.add(Contact(publicKeyTxt))
        contactRepository.setLastMessage(publicKeyTxt, Date().time)
    }

    fun delete(publicKey: PublicKey) = scope.launch {
        tox.deleteContact(publicKey)
        contactRepository.delete(Contact(publicKey.string()))
    }

    fun setDraft(pk: PublicKey, draft: String) = scope.launch {
        contactRepository.setDraftMessage(pk.string(), draft)
    }
}
