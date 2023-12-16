package com.team1.bohemian

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [CurrentLocationData::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDataDao(): LocationDataDao

    companion object {
        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 데이터베이스 업그레이드 작업을 수행할 수 있습니다.
            }
        }
        private const val DATABASE_NAME = "app_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
@Dao
interface LocationDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLocationData(locationData: CurrentLocationData)

    @Query("SELECT * FROM location_data")
    fun getAllLocationData(): List<CurrentLocationData>

    @Query("DELETE FROM location_data")
    fun deleteAll()
}
