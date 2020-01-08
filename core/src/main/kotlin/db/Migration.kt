package ltd.evilcorp.core.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) = db.execSQL(
        "ALTER TABLE contacts ADD COLUMN has_unread_messages INTEGER NOT NULL DEFAULT 0"
    )
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) = db.execSQL(
        "ALTER TABLE messages ADD COLUMN type INTEGER NOT NULL DEFAULT 0"
    )
}
