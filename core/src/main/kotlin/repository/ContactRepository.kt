// SPDX-FileCopyrightText: 2019-2020 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.core.repository

import kotlinx.coroutines.flow.Flow
import ltd.evilcorp.core.db.ContactDao
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.UserStatus
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class ContactRepository(override val di: DI) : DIAware {
    private val dao: ContactDao by instance()

    fun exists(publicKey: String): Boolean = dao.exists(publicKey)
    fun add(contact: Contact) = dao.save(contact)
    fun update(contact: Contact) = dao.update(contact)
    fun delete(contact: Contact) = dao.delete(contact)
    fun get(publicKey: String): Flow<Contact> = dao.load(publicKey)
    fun getAll(): Flow<List<Contact>> = dao.loadAll()
    fun resetTransientData() = dao.resetTransientData()

    fun setName(publicKey: String, name: String) = dao.setName(publicKey, name)
    fun setStatusMessage(publicKey: String, statusMessage: String) = dao.setStatusMessage(publicKey, statusMessage)
    fun setLastMessage(publicKey: String, lastMessage: Long) = dao.setLastMessage(publicKey, lastMessage)
    fun setUserStatus(publicKey: String, status: UserStatus) = dao.setUserStatus(publicKey, status)
    fun setConnectionStatus(publicKey: String, status: ConnectionStatus) = dao.setConnectionStatus(publicKey, status)
    fun setTyping(publicKey: String, typing: Boolean) = dao.setTyping(publicKey, typing)
    fun setAvatarUri(publicKey: String, uri: String) = dao.setAvatarUri(publicKey, uri)
    fun setHasUnreadMessages(publicKey: String, anyUnread: Boolean) = dao.setHasUnreadMessages(publicKey, anyUnread)
    fun setDraftMessage(publicKey: String, draft: String) = dao.setDraftMessage(publicKey, draft)
}
