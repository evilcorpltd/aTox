// SPDX-FileCopyrightText: 2019-2025 Robin Lindén <dev@robinlinden.eu>
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.tox

import ltd.evilcorp.core.vo.PublicKey

@JvmInline
value class ToxID(private val value: String) {
    fun bytes() = value.hexToBytes()
    fun string() = value

    fun toPublicKey() = PublicKey(value.dropLast(12))

    companion object {
        fun fromBytes(toxId: ByteArray) = ToxID(toxId.bytesToHex())
    }
}

data class BootstrapNode(val address: String, val port: Int, val publicKey: PublicKey)
