package ltd.evilcorp.core.db

import ltd.evilcorp.core.vo.MessageType
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {
    @Test
    fun `message type can be converted`() {
        MessageType.values().forEach { type ->
            assertEquals(type.ordinal, Converters.fromMessageType(type))
            assertEquals(type, Converters.toMessageType(type.ordinal))
        }
    }
}
