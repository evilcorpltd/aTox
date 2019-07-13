package ltd.evilcorp.atox.db

import androidx.room.TypeConverter
import ltd.evilcorp.atox.vo.ConnectionStatus
import ltd.evilcorp.atox.vo.Sender
import ltd.evilcorp.atox.vo.UserStatus
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

internal class Converters {
    companion object {
        @TypeConverter
        @JvmStatic
        fun toStatus(status: Int): UserStatus = UserStatus.values()[status]

        @TypeConverter
        @JvmStatic
        fun fromStatus(status: UserStatus): Int = status.ordinal

        @TypeConverter
        @JvmStatic
        fun toConnection(connection: Int): ConnectionStatus = ConnectionStatus.values()[connection]

        @TypeConverter
        @JvmStatic
        fun fromConnection(connection: ConnectionStatus): Int = connection.ordinal

        @TypeConverter
        @JvmStatic
        fun toSender(sender: Int): Sender = Sender.values()[sender]

        @TypeConverter
        @JvmStatic
        fun fromSender(sender: Sender): Int = sender.ordinal

        private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        @TypeConverter
        @JvmStatic
        fun toOffsetDateTime(dateTime: String?): OffsetDateTime? =
            dateTime?.let {
                return formatter.parse(it, OffsetDateTime::from)
            }

        @TypeConverter
        @JvmStatic
        fun fromOffsetDateTime(dateTime: OffsetDateTime?): String? = dateTime?.format(formatter)
    }
}
