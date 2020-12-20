package ltd.evilcorp.domain.tox

import im.tox.tox4j.core.exceptions.ToxNewException
import im.tox.tox4j.impl.jni.ToxCoreImpl

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

fun testToxSave(options: SaveOptions): ToxSaveStatus = try {
    val t = ToxCoreImpl(options.toToxOptions())
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
