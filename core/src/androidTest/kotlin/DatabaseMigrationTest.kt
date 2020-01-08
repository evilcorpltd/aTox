package ltd.evilcorp.core

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import ltd.evilcorp.core.db.Database
import ltd.evilcorp.core.db.MIGRATION_1_2
import ltd.evilcorp.core.db.MIGRATION_2_3
import ltd.evilcorp.core.vo.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

fun Boolean.toInt() = if (this) 1 else 0

private const val TEST_DB = "migration-test"

class DatabaseMigrationTest {
    @Rule
    @JvmField
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        Database::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate_1_to_2() {
        val publicKey = "76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39"
        val name = "robinli"
        val statusMessage = "Hello I am robot beep beep boop"
        val lastMessage = 100
        val status = UserStatus.Busy
        val connectionStatus = ConnectionStatus.TCP
        val isTyping = true
        val avatar = "file:///home/robin/fantastic_bird.png"

        var db = helper.createDatabase(TEST_DB, 1).apply {
            execSQL(
                "INSERT INTO contacts VALUES (" +
                    "'$publicKey'," +
                    "'$name'," +
                    "'$statusMessage'," +
                    "$lastMessage," +
                    "${status.ordinal}," +
                    "${connectionStatus.ordinal}," +
                    "${isTyping.toInt()}," +
                    "'$avatar'" +
                    ")"
            )
        }

        var cursor = db.query("SELECT * FROM contacts")
        cursor.moveToFirst()
        assertEquals(cursor.columnCount, 8)
        assertEquals(publicKey, cursor.getString(0))
        assertEquals(name, cursor.getString(1))
        assertEquals(statusMessage, cursor.getString(2))
        assertEquals(lastMessage, cursor.getInt(3))
        assertEquals(status.ordinal, cursor.getInt(4))
        assertEquals(connectionStatus.ordinal, cursor.getInt(5))
        assertEquals(isTyping.toInt(), cursor.getInt(6))
        assertEquals(avatar, cursor.getString(7))
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)
        val hasUnreadMessages = false

        cursor = db.query("SELECT * FROM contacts")
        assertEquals(cursor.columnCount, 9)
        cursor.moveToFirst()
        assertEquals(publicKey, cursor.getString(0))
        assertEquals(name, cursor.getString(1))
        assertEquals(statusMessage, cursor.getString(2))
        assertEquals(lastMessage, cursor.getInt(3))
        assertEquals(status.ordinal, cursor.getInt(4))
        assertEquals(connectionStatus.ordinal, cursor.getInt(5))
        assertEquals(isTyping.toInt(), cursor.getInt(6))
        assertEquals(avatar, cursor.getString(7))
        assertEquals(hasUnreadMessages.toInt(), cursor.getInt(8))
        db.close()
    }

    @Test
    fun migrate_2_to_3() {
        val msg = Message(
            "76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39",
            "hello i am robot beep beep boop",
            Sender.Sent,
            MessageType.Normal,
            99,
            12L
        )

        var db = helper.createDatabase(TEST_DB, 2).apply {
            with(msg) {
                execSQL(
                    "INSERT INTO messages VALUES (" +
                        "$id," +
                        "'$publicKey'," +
                        "'$message'," +
                        "${sender.ordinal}," +
                        "$correlationId," +
                        "$timestamp)"
                )
            }
        }

        db.query("SELECT * FROM messages").let { cursor ->
            assertEquals(cursor.columnCount, 6)
            with(msg) {
                cursor.moveToFirst()
                assertEquals(id, cursor.getLong(0))
                assertEquals(publicKey, cursor.getString(1))
                assertEquals(message, cursor.getString(2))
                assertEquals(sender.ordinal, cursor.getInt(3))
                assertEquals(correlationId, cursor.getInt(4))
                assertEquals(timestamp, cursor.getLong(5))
            }
        }
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, MIGRATION_2_3)

        db.query("SELECT * FROM messages").let { cursor ->
            assertEquals(cursor.columnCount, 7)
            with(msg) {
                cursor.moveToFirst()
                assertEquals(id, cursor.getLong(0))
                assertEquals(publicKey, cursor.getString(1))
                assertEquals(message, cursor.getString(2))
                assertEquals(sender.ordinal, cursor.getInt(3))
                assertEquals(correlationId, cursor.getInt(4))
                assertEquals(timestamp, cursor.getLong(5))
                assertEquals(type.ordinal, cursor.getInt(6))
            }
        }
        db.close()
    }

    @Test
    fun run_all_migrations() {
        val contact = Contact(
            "76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39",
            "robinli",
            "Hello I am robot beep beep boop",
            100,
            UserStatus.Busy,
            ConnectionStatus.TCP,
            true,
            "file:///home/robin/fantastic_bird.png"
        )

        val msg = Message(
            "76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39",
            "hello i am robot beep beep boop",
            Sender.Sent,
            MessageType.Normal,
            99,
            12L
        )

        var db = helper.createDatabase(TEST_DB, 1).apply {
            with(contact) {
                execSQL(
                    "INSERT INTO contacts VALUES (" +
                        "'$publicKey'," +
                        "'$name'," +
                        "'$statusMessage'," +
                        "$lastMessage," +
                        "${status.ordinal}," +
                        "${connectionStatus.ordinal}," +
                        "${typing.toInt()}," +
                        "'$avatarUri')"
                )
            }
            with(msg) {
                execSQL(
                    "INSERT INTO messages VALUES (" +
                        "$id," +
                        "'$publicKey'," +
                        "'$message'," +
                        "${sender.ordinal}," +
                        "$correlationId," +
                        "$timestamp)"
                )
            }
        }

        db.query("SELECT * FROM contacts").let { cursor ->
            assertEquals(cursor.columnCount, 8)
            with(contact) {
                cursor.moveToFirst()
                assertEquals(publicKey, cursor.getString(0))
                assertEquals(name, cursor.getString(1))
                assertEquals(statusMessage, cursor.getString(2))
                assertEquals(lastMessage, cursor.getLong(3))
                assertEquals(status.ordinal, cursor.getInt(4))
                assertEquals(connectionStatus.ordinal, cursor.getInt(5))
                assertEquals(typing.toInt(), cursor.getInt(6))
                assertEquals(avatarUri, cursor.getString(7))
            }
        }
        db.query("SELECT * FROM messages").let { cursor ->
            assertEquals(cursor.columnCount, 6)
            with(msg) {
                cursor.moveToFirst()
                assertEquals(id, cursor.getLong(0))
                assertEquals(publicKey, cursor.getString(1))
                assertEquals(message, cursor.getString(2))
                assertEquals(sender.ordinal, cursor.getInt(3))
                assertEquals(correlationId, cursor.getInt(4))
                assertEquals(timestamp, cursor.getLong(5))
            }
        }
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, MIGRATION_1_2, MIGRATION_2_3)

        db.query("SELECT * FROM contacts").let { cursor ->
            assertEquals(cursor.columnCount, 9)
            with(contact) {
                cursor.moveToFirst()
                assertEquals(publicKey, cursor.getString(0))
                assertEquals(name, cursor.getString(1))
                assertEquals(statusMessage, cursor.getString(2))
                assertEquals(lastMessage, cursor.getLong(3))
                assertEquals(status.ordinal, cursor.getInt(4))
                assertEquals(connectionStatus.ordinal, cursor.getInt(5))
                assertEquals(typing.toInt(), cursor.getInt(6))
                assertEquals(avatarUri, cursor.getString(7))
                assertEquals(hasUnreadMessages.toInt(), cursor.getInt(8))
            }
        }
        db.query("SELECT * FROM messages").let { cursor ->
            assertEquals(cursor.columnCount, 7)
            with(msg) {
                cursor.moveToFirst()
                assertEquals(id, cursor.getLong(0))
                assertEquals(publicKey, cursor.getString(1))
                assertEquals(message, cursor.getString(2))
                assertEquals(sender.ordinal, cursor.getInt(3))
                assertEquals(correlationId, cursor.getInt(4))
                assertEquals(timestamp, cursor.getLong(5))
                assertEquals(type.ordinal, cursor.getInt(6))
            }
        }
        db.close()
    }
}
