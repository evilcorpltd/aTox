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

    private val ft = FileTransfer(
        "76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39",
        123,
        FileKind.Avatar.ordinal,
        9876,
        "bird.png2",
        false,
        FtNotStarted
    )

    private val msg = Message(
        "76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39",
        "hello i am robot beep beep boop",
        Sender.Sent,
        MessageType.Normal,
        99,
        12L
    )

    private val contact = Contact(
        "76518406F6A9F2217E8DC487CC783C25CC16A15EB36FF32E335A235342C48A39",
        "robinli",
        "Hello I am robot beep beep boop",
        100,
        UserStatus.Busy,
        ConnectionStatus.TCP,
        true,
        "file:///home/robin/fantastic_bird.png"
    )

    @Test
    fun migrate_1_to_2() {
        helper.createDatabase(TEST_DB, 1).use { db ->
            with(contact) {
                db.execSQL(
                    """INSERT INTO contacts VALUES (
                        '$publicKey',
                        '$name',
                        '$statusMessage',
                        $lastMessage,
                        ${status.ordinal},
                        ${connectionStatus.ordinal},
                        ${typing.toInt()},
                        '$avatarUri')
                    """.trimIndent()
                )
            }

            val cursor = db.query("SELECT * FROM contacts").apply { moveToFirst() }
            assertEquals(cursor.columnCount, 8)
            with(contact) {
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

        helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2).use { db ->
            val cursor = db.query("SELECT * FROM contacts").apply { moveToFirst() }
            assertEquals(cursor.columnCount, 9)
            with(contact) {
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
    }

    @Test
    fun migrate_2_to_3() {
        helper.createDatabase(TEST_DB, 2).use { db ->
            with(msg) {
                db.execSQL(
                    """INSERT INTO messages VALUES (
                        $id,
                        '$publicKey',
                        '$message',
                        ${sender.ordinal},
                        $correlationId,
                        $timestamp)
                    """.trimIndent()
                )
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
        }

        helper.runMigrationsAndValidate(TEST_DB, 3, true, MIGRATION_2_3).use { db ->
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
        }
    }

    @Test
    fun migrate_3_to_4() {
        helper.createDatabase(TEST_DB, 3).use { db ->
            with(ft) {
                db.execSQL(
                    """INSERT INTO file_transfers VALUES (
                        '$publicKey',
                        $fileNumber,
                        $fileKind,
                        $fileSize,
                        '$fileName',
                        ${outgoing.toInt()},
                        $progress)
                    """.trimIndent()
                )
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
        }

        helper.runMigrationsAndValidate(TEST_DB, 4, true, MIGRATION_3_4).use { db ->
            // The table is nuked during the migration because it was useless before.
            assertEquals(db.query("SELECT * FROM file_transfers").count, 0)
        }
    }

    @Test
    fun migrate_4_to_5() {
        helper.createDatabase(TEST_DB, 4).use { db ->
            with(contact) {
                db.execSQL(
                    """INSERT INTO contacts VALUES (
                        '$publicKey',
                        '$name',
                        '$statusMessage',
                        $lastMessage,
                        ${status.ordinal},
                        ${connectionStatus.ordinal},
                        ${typing.toInt()},
                        '$avatarUri',
                        ${hasUnreadMessages.toInt()})
                    """.trimIndent()
                )
            }

            val cursor = db.query("SELECT * FROM contacts").apply { moveToFirst() }
            assertEquals(9, cursor.columnCount)
            with(contact) {
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

        helper.runMigrationsAndValidate(TEST_DB, 5, true, MIGRATION_4_5).use { db ->
            val cursor = db.query("SELECT * FROM contacts").apply { moveToFirst() }
            assertEquals(10, cursor.columnCount)
            with(contact) {
                assertEquals(publicKey, cursor.getString(0))
                assertEquals(name, cursor.getString(1))
                assertEquals(statusMessage, cursor.getString(2))
                assertEquals(lastMessage, cursor.getLong(3))
                assertEquals(status.ordinal, cursor.getInt(4))
                assertEquals(connectionStatus.ordinal, cursor.getInt(5))
                assertEquals(typing.toInt(), cursor.getInt(6))
                assertEquals(avatarUri, cursor.getString(7))
                assertEquals(hasUnreadMessages.toInt(), cursor.getInt(8))
                assertEquals(draftMessage, cursor.getString(9))
            }
        }
    }

    @Test
    fun run_all_migrations() {
        helper.createDatabase(TEST_DB, 1).use { db ->
            with(contact) {
                db.execSQL(
                    """INSERT INTO contacts VALUES (
                        '$publicKey',
                        '$name',
                        '$statusMessage',
                        $lastMessage,
                        ${status.ordinal},
                        ${connectionStatus.ordinal},
                        ${typing.toInt()},
                        '$avatarUri')
                    """.trimIndent()
                )
            }
            with(msg) {
                db.execSQL(
                    """INSERT INTO messages VALUES (
                        $id,
                        '$publicKey',
                        '$message',
                        ${sender.ordinal},
                        $correlationId,
                        $timestamp)
                    """.trimIndent()
                )
            }
            with(ft) {
                db.execSQL(
                    """INSERT INTO file_transfers VALUES (
                        '$publicKey',
                        $fileNumber,
                        $fileKind,
                        $fileSize,
                        '$fileName',
                        ${outgoing.toInt()},
                        $progress)
                    """.trimIndent()
                )
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
        }

        helper.runMigrationsAndValidate(TEST_DB, 5, true, *ALL_MIGRATIONS).use { db ->
            db.query("SELECT * FROM contacts").let { cursor ->
                assertEquals(cursor.columnCount, 10)
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
                    assertEquals(draftMessage, cursor.getString(9))
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
        }
    }
}
