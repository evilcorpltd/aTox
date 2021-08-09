// SPDX-FileCopyrightText: 2019-2020 aTox contributors
//
// SPDX-License-Identifier: GPL-3.0-only

package ltd.evilcorp.core.db

import androidx.room.TypeConverter
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.MessageType
import ltd.evilcorp.core.vo.Sender
import ltd.evilcorp.core.vo.UserStatus

class Converters private constructor() {
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

        @TypeConverter
        @JvmStatic
        fun toMessageType(type: Int): MessageType = MessageType.values()[type]

        @TypeConverter
        @JvmStatic
        fun fromMessageType(type: MessageType): Int = type.ordinal
    }
}
