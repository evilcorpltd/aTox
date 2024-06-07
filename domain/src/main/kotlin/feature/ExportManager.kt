// SPDX-FileCopyrightText: 2022 Akito <the@akito.ooo>
// SPDX-FileCopyrightText: 2023-2024 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.feature

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import ltd.evilcorp.core.repository.MessageRepository
import org.json.JSONArray
import org.json.JSONObject

class ExportManager @Inject constructor(private val messageRepository: MessageRepository) {
    fun generateExportMessagesJString(publicKey: String): String {
        val messages = runBlocking { messageRepository.get(publicKey).first() }

        val root = JSONObject()
        root.put("version", 1)
        root.put(
            "timestamp",
            SimpleDateFormat("""yyyy-MM-dd'T'HH-mm-ss""", Locale.getDefault()).format(Date()),
        )
        root.put("contact_public_key", publicKey)

        val entries = JSONArray()
        for (message in messages) {
            val jsonMessage = JSONObject().apply {
                put("message", message.message)
                put("sender", message.sender.toString())
                put("type", message.type.toString())
                put("timestamp", message.timestamp)
            }
            entries.put(jsonMessage)
        }
        root.put("entries", entries)
        return root.toString(2)
    }
}
