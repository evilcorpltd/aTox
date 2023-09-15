// SPDX-FileCopyrightText: 2022 Akito <the@akito.ooo>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.feature

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import ltd.evilcorp.core.repository.MessageRepository
import ltd.evilcorp.core.vo.Message

class ExportManager @Inject constructor(
    private val messageRepository: MessageRepository,
) {
    // TODO @Akito: Increment version programmatically on major changes.
    private fun List<Message>.generateExportMessages(): ExportMessages {
        return ExportMessages(
            version = 1,
            timestamp = SimpleDateFormat(
                """yyyy-MM-dd'T'HH-mm-ss""",
                Locale.getDefault(),
            ).format(Date()),
            entries = this,
        )
    }

    private fun generateExportMessagesJString(messages: List<Message>): String =
        jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(messages.generateExportMessages())

    private fun getMessages(publicKey: String): List<Message> = runBlocking {
        messageRepository.get(publicKey).first()
    }

    fun generateExportMessagesJString(publicKey: String): String = generateExportMessagesJString(
        getMessages(publicKey),
    )
}

data class ExportMessages(
    val version: Int,
    val timestamp: String,
    val entries: List<Message>,
)
