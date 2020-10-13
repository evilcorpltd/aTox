package ltd.evilcorp.core.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.Contact
import ltd.evilcorp.core.vo.FileKind
import ltd.evilcorp.core.vo.FileTransfer
import ltd.evilcorp.core.vo.FtNotStarted
import ltd.evilcorp.core.vo.Message
import ltd.evilcorp.core.vo.MessageType
import ltd.evilcorp.core.vo.Sender
import ltd.evilcorp.core.vo.UserStatus
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

fun Boolean.toInt() = if (this) 1 else 0

private const val TEST_DB = "migration-test"

@RunWith(AndroidJUnit4::class)
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
    fun migrate_3_to_4() {
        val ft = FileTransfer(
            "76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39",
            123,
            FileKind.Avatar.ordinal,
            9876,
            "bird.png2",
            false,
            FtNotStarted
        )

        var db = helper.createDatabase(TEST_DB, 3).apply {
            with(ft) {
                execSQL(
                    "INSERT INTO file_transfers VALUES (" +
                        "'$publicKey'," +
                        "$fileNumber," +
                        "$fileKind," +
                        "$fileSize," +
                        "'$fileName'," +
                        "${outgoing.toInt()}," +
                        "$progress)"
                )
            }
        }

        db.query("SELECT * FROM file_transfers").let { cursor ->
            assertEquals(7, cursor.columnCount)
            with(ft) {
                cursor.moveToFirst()
                assertEquals(publicKey, cursor.getString(0))
                assertEquals(fileNumber, cursor.getInt(1))
                assertEquals(fileKind, cursor.getInt(2))
                assertEquals(fileSize, cursor.getLong(3))
                assertEquals(fileName, cursor.getString(4))
                assertEquals(outgoing.toInt(), cursor.getInt(5))
                assertEquals(progress, cursor.getLong(6))
            }
        }
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 4, true, MIGRATION_3_4)
        // The table is nuked during the migration because it was useless before.
        assertEquals(db.query("SELECT * FROM messages").count, 0)
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

        val ft = FileTransfer(
            "76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39",
            123,
            FileKind.Avatar.ordinal,
            9876,
            "bird.png2",
            false,
            FtNotStarted
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
            with(ft) {
                execSQL(
                    "INSERT INTO file_transfers VALUES (" +
                        "'$publicKey'," +
                        "$fileNumber," +
                        "$fileKind," +
                        "$fileSize," +
                        "'$fileName'," +
                        "${outgoing.toInt()}," +
                        "$progress)"
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
        db.query("SELECT * FROM file_transfers").let { cursor ->
            assertEquals(7, cursor.columnCount)
            with(ft) {
                cursor.moveToFirst()
                assertEquals(publicKey, cursor.getString(0))
                assertEquals(fileNumber, cursor.getInt(1))
                assertEquals(fileKind, cursor.getInt(2))
                assertEquals(fileSize, cursor.getLong(3))
                assertEquals(fileName, cursor.getString(4))
                assertEquals(outgoing.toInt(), cursor.getInt(5))
                assertEquals(progress, cursor.getLong(6))
            }
        }
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)

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
