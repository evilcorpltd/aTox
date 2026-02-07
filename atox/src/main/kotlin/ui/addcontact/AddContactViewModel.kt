// SPDX-FileCopyrightText: 2019-2022 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.atox.ui.addcontact

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ltd.evilcorp.atox.tox.ToxStarter
import ltd.evilcorp.core.repository.MessageRepository
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.Message
import ltd.evilcorp.core.vo.MessageType
import ltd.evilcorp.core.vo.Sender
import ltd.evilcorp.domain.feature.ContactManager
import ltd.evilcorp.domain.tox.Tox
import ltd.evilcorp.domain.tox.ToxID
import ltd.evilcorp.domain.tox.ToxSaveStatus

class AddContactViewModel @Inject constructor(
    private val scope: CoroutineScope,
    private val messageRepository: MessageRepository,
    private val contactManager: ContactManager,
    private val tox: Tox,
    private val toxStarter: ToxStarter,
) : ViewModel() {
    val toxId by lazy { tox.toxId }
    val contacts: LiveData<List<Contact>> = contactManager.getAll().asLiveData()

    fun isToxRunning() = tox.started
    fun tryLoadTox(): Boolean = toxStarter.tryLoadTox(null) == ToxSaveStatus.Ok

    private fun addToChatLog(publicKey: String, message: String) = scope.launch {
        messageRepository.add(
            Message(
                publicKey,
                message,
                Sender.Sent,
                MessageType.Normal,
                0,
                Date().time,
            ),
        )
    }

    fun addContact(toxId: ToxID, message: String) = runBlocking {
        contactManager.add(toxId, message).join()
        addToChatLog(toxId.toPublicKey().string(), message)
    }
}
