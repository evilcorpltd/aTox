// SPDX-FileCopyrightText: 2019-2025 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.core.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import ltd.evilcorp.core.db.ContactDao
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.PublicKey
import ltd.evilcorp.core.vo.UserStatus

@Singleton
class ContactRepository @Inject constructor(private val dao: ContactDao) {
    fun exists(publicKey: PublicKey): Boolean = dao.exists(publicKey)
    fun add(contact: Contact) = dao.save(contact)
    fun update(contact: Contact) = dao.update(contact)
    fun delete(contact: Contact) = dao.delete(contact)
    fun get(publicKey: PublicKey): Flow<Contact> = dao.load(publicKey)
    fun getAll(): Flow<List<Contact>> = dao.loadAll()
    fun resetTransientData() = dao.resetTransientData()

    fun setName(pk: PublicKey, name: String) = dao.setName(pk, name)
    fun setStatusMessage(pk: PublicKey, statusMessage: String) = dao.setStatusMessage(pk, statusMessage)
    fun setLastMessage(pk: PublicKey, lastMessage: Long) = dao.setLastMessage(pk, lastMessage)
    fun setUserStatus(pk: PublicKey, status: UserStatus) = dao.setUserStatus(pk, status)
    fun setConnectionStatus(pk: PublicKey, status: ConnectionStatus) = dao.setConnectionStatus(pk, status)
    fun setTyping(pk: PublicKey, typing: Boolean) = dao.setTyping(pk, typing)
    fun setAvatarUri(pk: PublicKey, uri: String) = dao.setAvatarUri(pk, uri)
    fun setHasUnreadMessages(pk: PublicKey, anyUnread: Boolean) = dao.setHasUnreadMessages(pk, anyUnread)
    fun setDraftMessage(pk: PublicKey, draft: String) = dao.setDraftMessage(pk, draft)
}
