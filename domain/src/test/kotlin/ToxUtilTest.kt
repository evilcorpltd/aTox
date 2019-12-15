package ltd.evilcorp.domain

import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.core.enums.ToxUserStatus
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.UserStatus
import ltd.evilcorp.domain.tox.bytesToHex
import ltd.evilcorp.domain.tox.hexToBytes
import ltd.evilcorp.domain.tox.toConnectionStatus
import ltd.evilcorp.domain.tox.toUserStatus
import org.junit.Assert.*
import org.junit.Test
import java.util.*

private fun byteArrayOf(vararg bytes: Int) = ByteArray(bytes.size) { bytes[it].toByte() }

class ToxUtilTest {
    @Test
    fun connection_enums_can_be_converted() {
        assert(ToxConnection.values().size == ConnectionStatus.values().size)
        assert(ConnectionStatus.values().size == 3)

        ToxConnection.values().forEach {
            assertEquals(it.ordinal, it.toConnectionStatus().ordinal)
        }

        assertEquals(ToxConnection.NONE.ordinal, ConnectionStatus.None.ordinal)
        assertEquals(ToxConnection.UDP.ordinal, ConnectionStatus.UDP.ordinal)
        assertEquals(ToxConnection.TCP.ordinal, ConnectionStatus.TCP.ordinal)
    }

    @Test
    fun status_enums_can_be_converted() {
        assert(ToxUserStatus.values().size == UserStatus.values().size)
        assert(UserStatus.values().size == 3)

        ToxUserStatus.values().forEach {
            assertEquals(it.ordinal, it.toUserStatus().ordinal)
        }

        assertEquals(ToxUserStatus.NONE.ordinal, UserStatus.None.ordinal)
        assertEquals(ToxUserStatus.AWAY.ordinal, UserStatus.Away.ordinal)
        assertEquals(ToxUserStatus.BUSY.ordinal, UserStatus.Busy.ordinal)
    }

    @Test
    fun public_keys_can_be_converted() {
        val keyString = "76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39"
        assert(keyString.hexToBytes().size == 32)
        assertEquals(keyString, keyString.hexToBytes().bytesToHex().toUpperCase(Locale.ROOT))

        val keyBytes = byteArrayOf(
            0x76, 0x51, 0x84, 0x06, 0xF6, 0xA9, 0xF2, 0x21, 0x7E, 0x8D, 0xC4, 0x87, 0xCC, 0x78, 0x3C, 0x25,
            0xCC, 0x16, 0xA1, 0x5E, 0xB3, 0x6F, 0xF3, 0x2E, 0x33, 0x5A, 0x23, 0x53, 0x42, 0xC4, 0x8A, 0x39
        )

        assertEquals(keyBytes.size, keyString.hexToBytes().size)
        assert(keyBytes.contentEquals(keyString.hexToBytes()))

        val anotherKeyString = "7B6704162C6532A5A8F0840A3680672D0E9D3E62B6419FFD88D9880669482169"
        assertEquals(anotherKeyString, anotherKeyString.hexToBytes().bytesToHex().toUpperCase(Locale.ROOT))
        assertNotEquals(anotherKeyString.hexToBytes(), keyString.hexToBytes())
    }

    @Test
    fun casing_of_public_keys_does_not_matter() {
        val keyString = "76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39"
        assertArrayEquals(
            keyString.toUpperCase(Locale.ROOT).hexToBytes(),
            keyString.toLowerCase(Locale.ROOT).hexToBytes()
        )
    }
}
