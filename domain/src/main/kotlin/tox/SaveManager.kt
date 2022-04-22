// SPDX-FileCopyrightText: 2019 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.tox

interface SaveManager {
    fun list(): List<String>
    fun save(publicKey: PublicKey, saveData: ByteArray)
    fun load(publicKey: PublicKey): ByteArray?
}
