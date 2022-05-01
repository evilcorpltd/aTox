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
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class ContactManager(override val di: DI) : DIAware {
    private val scope: CoroutineScope by instance()
    private val contactRepository: ContactRepository by instance()
    private val tox: Tox by instance()

    fun get(publicKey: PublicKey) = contactRepository.get(publicKey.string())
    fun getAll() = contactRepository.getAll()

    fun add(toxID: ToxID, message: String) = scope.launch {
        tox.addContact(toxID, message)
        contactRepository.add(Contact(toxID.toPublicKey().string()))
    }

    fun delete(publicKey: PublicKey) = scope.launch {
        tox.deleteContact(publicKey)
        contactRepository.delete(Contact(publicKey.string()))
    }

    fun setDraft(pk: PublicKey, draft: String) = scope.launch {
        contactRepository.setDraftMessage(pk.string(), draft)
    }
}
