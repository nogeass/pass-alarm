package com.nogeass.passalarm.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nogeass.passalarm.data.local.dao.*
import com.nogeass.passalarm.data.local.entity.*

@Database(
    entities = [
        AlarmPlanEntity::class,
        SkipExceptionEntity::class,
        ScheduledTokenEntity::class,
        HolidayJpEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class PassAlarmDatabase : RoomDatabase() {
    abstract fun alarmPlanDao(): AlarmPlanDao
    abstract fun skipExceptionDao(): SkipExceptionDao
    abstract fun scheduledTokenDao(): ScheduledTokenDao
    abstract fun holidayJpDao(): HolidayJpDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE alarm_plan ADD COLUMN label TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE skip_exception ADD COLUMN planId INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE INDEX idx_skip_planId_date ON skip_exception(planId, date)")
                db.execSQL("UPDATE skip_exception SET planId = COALESCE((SELECT id FROM alarm_plan LIMIT 1), 0)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE alarm_plan ADD COLUMN soundId TEXT NOT NULL DEFAULT 'default'")
            }
        }
    }
}
