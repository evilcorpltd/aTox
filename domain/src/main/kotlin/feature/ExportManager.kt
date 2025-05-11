// SPDX-FileCopyrightText: 2022 Akito <the@akito.ooo>
// SPDX-FileCopyrightText: 2023-2025 Robin Lindén <dev@robinlinden.eu>
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
import ltd.evilcorp.core.vo.PublicKey
import org.json.JSONArray
import org.json.JSONObject

class ExportManager @Inject constructor(private val messageRepository: MessageRepository) {
    fun generateExportMessagesJString(pk: PublicKey): String {
        val messages = runBlocking { messageRepository.get(pk).first() }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

        val root = JSONObject()
        root.put("version", 1)
        root.put("timestamp", dateFormat.format(Date()))
        root.put("contact_public_key", pk.string())

        val entries = JSONArray()
        for (message in messages) {
            val jsonMessage = JSONObject().apply {
                put("message", message.message)
                put("sender", message.sender.toString())
                put("type", message.type.toString())
                put("timestamp", dateFormat.format(Date(message.timestamp)))
            }
            entries.put(jsonMessage)
        }
        root.put("entries", entries)
        return root.toString(2)
    }
}
