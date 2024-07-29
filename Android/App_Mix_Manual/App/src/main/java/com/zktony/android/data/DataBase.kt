package com.zktony.android.data

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zktony.android.BuildConfig
import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.dao.ErrorRecordDao
import com.zktony.android.data.dao.ExpectedDao
import com.zktony.android.data.dao.ExperimentRecordDao
import com.zktony.android.data.dao.MotorDao
import com.zktony.android.data.dao.NewCalibrationDao
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.dao.SettingDao
import com.zktony.android.data.dao.SportsLogDao
import com.zktony.android.data.entities.Calibration
import com.zktony.android.data.entities.ErrorRecord
import com.zktony.android.data.entities.Expected
import com.zktony.android.data.entities.ExperimentRecord
import com.zktony.android.data.entities.Motor
import com.zktony.android.data.entities.NewCalibration
import com.zktony.android.data.entities.Program
import com.zktony.android.data.entities.Setting
import com.zktony.android.data.entities.SportsLog

/**
 * @author 刘贺贺
 */
@Database(
    entities =
    [
        Motor::class,
        Calibration::class,
        Program::class,
        ExperimentRecord::class,
        ErrorRecord::class,
        SportsLog::class,
        Setting::class,
        NewCalibration::class,
        Expected::class,
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun MotorDao(): MotorDao
    abstract fun CalibrationDao(): CalibrationDao
    abstract fun ProgramDao(): ProgramDao
    abstract fun ExperimentRecordDao(): ExperimentRecordDao
    abstract fun SettingDao(): SettingDao
    abstract fun NewCalibrationDao(): NewCalibrationDao
    abstract fun ErrorRecordDao(): ErrorRecordDao
    abstract fun SportsLogDao(): SportsLogDao
    abstract fun ExpectedDao(): ExpectedDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {

        // 创建新的 program 表
        database.execSQL(
            """
                    CREATE TABLE program_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        displayText TEXT NOT NULL DEFAULT 'None',
                        startRange REAL NOT NULL DEFAULT 0.0,
                        endRange REAL NOT NULL DEFAULT 0.0,
                        thickness TEXT NOT NULL DEFAULT '1.0',
                        coagulant INTEGER NOT NULL DEFAULT 0,
                        volume REAL NOT NULL DEFAULT 0.0,
                        founder TEXT NOT NULL DEFAULT '',
                        createTime INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent()
        )

        // 将旧表 program 中的数据插入到新表 program_new 中
        database.execSQL(
            """
                    INSERT INTO program_new (id, displayText, startRange, endRange, thickness, coagulant, volume, founder, createTime)
                    SELECT id, displayText, startRange, endRange, thickness, coagulant, volume, founder, createTime FROM program
                """.trimIndent()
        )

        // 删除旧的 program 表
        database.execSQL("DROP TABLE program")

        // 将新表 program_new 重命名为 program
        database.execSQL("ALTER TABLE program_new RENAME TO program")



        database.execSQL(
            """
                    CREATE TABLE experimentrecord_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        startRange REAL NOT NULL DEFAULT 0.0,
                        endRange REAL NOT NULL DEFAULT 0.0,
                        thickness TEXT NOT NULL DEFAULT '1.0',
                        coagulant INTEGER NOT NULL DEFAULT 0,
                        volume REAL NOT NULL DEFAULT 0.0,
                        number INTEGER NOT NULL DEFAULT 0,
                        status TEXT NOT NULL DEFAULT '',
                        detail TEXT NOT NULL DEFAULT '',
                        createTime INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent()
        )

        database.execSQL(
            """
                    INSERT INTO experimentrecord_new (id, startRange, endRange, thickness, coagulant, volume, number,status,detail, createTime)
                    SELECT id, startRange, endRange, thickness, coagulant, volume, number,status,detail, createTime FROM experimentrecord
                """.trimIndent()
        )

        database.execSQL("DROP TABLE experimentrecord")

        database.execSQL("ALTER TABLE experimentrecord_new RENAME TO experimentrecord")

        /**
         * 1.0升级2.0需要创建expected表
         * 1.1升级2.0不需要创建
         */
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `expected` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`lowFillingDefault` REAL NOT NULL, " +
                    "`coagulantFillingDefault` REAL NOT NULL, " +
                    "`coagulantCleanDefault` REAL NOT NULL, " +
                    "`rinseFillingDefault` REAL NOT NULL, " +
                    "`higeRehearsalDefault` REAL NOT NULL, " +
                    "`higeFillingDefault` REAL NOT NULL, " +
                    "`rinseCleanDefault` REAL NOT NULL, " +
                    "`higeCleanDefault` REAL NOT NULL, " +
                    "`lowCleanDefault` REAL NOT NULL)"
        )

    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {

        // 执行 SQL 语句，添加新列
        database.execSQL("ALTER TABLE setting ADD COLUMN coagulantRinse REAL NOT NULL DEFAULT 10.0")

        database.execSQL("ALTER TABLE expected ADD COLUMN coagulantRinseDefault REAL NOT NULL DEFAULT 10.0")

    }
}