// SPDX-FileCopyrightText: 2019-2020 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.tox

enum class ProxyType {
    None,
    HTTP,
    SOCKS5,
}

@Suppress("ArrayInDataClass")
data class SaveOptions(
    val saveData: ByteArray?,
    val udpEnabled: Boolean,
    val proxyType: ProxyType,
    val proxyAddress: String,
    val proxyPort: Int,
)
