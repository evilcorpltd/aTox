// SPDX-FileCopyrightText: 2019-2025 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.core.vo

private fun hexToBytes(hex: String) = hex.chunked(2).map { it.uppercase().toInt(radix = 16).toByte() }.toByteArray()
private fun bytesToHex(bytes: ByteArray) = bytes.joinToString(separator = "") { "%02X".format(it) }

@JvmInline
value class PublicKey(private val value: String) {
    fun bytes() = hexToBytes(value)
    fun string() = value
    fun fingerprint() = value.take(8)

    companion object {
        fun fromBytes(publicKey: ByteArray) = PublicKey(bytesToHex(publicKey))
    }
}
