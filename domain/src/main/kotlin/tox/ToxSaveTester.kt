// SPDX-FileCopyrightText: 2020-2021 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.domain.tox

import im.tox.tox4j.core.exceptions.ToxNewException
import im.tox.tox4j.impl.jni.ToxCoreImpl
import im.tox.tox4j.impl.jni.ToxCryptoImpl

enum class ToxSaveStatus {
    Ok,
    BadFormat,
    Encrypted,
    OutOfMemory,
    Null,
    PortAlloc,
    BadProxyHost,
    BadProxyPort,
    BadProxyType,
    ProxyNotFound,
    SaveNotFound,
}

fun testToxSave(options: SaveOptions, password: String?): ToxSaveStatus = try {
    val toxOptions = if (password == null) {
        options.toToxOptions()
    } else {
        val salt = ToxCryptoImpl.getSalt(options.saveData)
        val passkey = ToxCryptoImpl.passKeyDeriveWithSalt(password.toByteArray(), salt)
        options.copy(saveData = ToxCryptoImpl.decrypt(options.saveData, passkey)).toToxOptions()
    }
    val t = ToxCoreImpl(toxOptions)
    t.close()
    ToxSaveStatus.Ok
} catch (e: ToxNewException) {
    when (e.code()!!) {
        ToxNewException.Code.LOAD_BAD_FORMAT -> ToxSaveStatus.BadFormat
        ToxNewException.Code.LOAD_ENCRYPTED -> ToxSaveStatus.Encrypted
        ToxNewException.Code.MALLOC -> ToxSaveStatus.OutOfMemory
        ToxNewException.Code.NULL -> ToxSaveStatus.Null
        ToxNewException.Code.PORT_ALLOC -> ToxSaveStatus.PortAlloc
        ToxNewException.Code.PROXY_BAD_HOST -> ToxSaveStatus.BadProxyHost
        ToxNewException.Code.PROXY_BAD_PORT -> ToxSaveStatus.BadProxyPort
        ToxNewException.Code.PROXY_BAD_TYPE -> ToxSaveStatus.BadProxyType
        ToxNewException.Code.PROXY_NOT_FOUND -> ToxSaveStatus.ProxyNotFound
    }
}
