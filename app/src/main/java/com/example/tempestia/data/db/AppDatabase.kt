package com.example.tempestia.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tempestia.data.weather.model.Alert
import com.example.tempestia.data.weather.dataSource.locale.AlertsDao
import com.example.tempestia.data.favorites.dataSource.local.FavoriteCityDao
import com.example.tempestia.data.favorites.model.FavoriteCity

@Database(entities = [FavoriteCity::class, Alert::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoriteCityDao(): FavoriteCityDao
    abstract fun alertsDao(): AlertsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create the new table with the 'id' column
                db.execSQL("CREATE TABLE IF NOT EXISTS `favorite_cities_new` (`id` TEXT NOT NULL, `cityName` TEXT NOT NULL, `lat` REAL NOT NULL, `lon` REAL NOT NULL, `country` TEXT, `addedAt` INTEGER NOT NULL, `isCurrentLocation` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                // 2. Copy the old data over and generate the ID dynamically
                db.execSQL("INSERT INTO favorite_cities_new (id, cityName, lat, lon, country, addedAt, isCurrentLocation) SELECT lat || '_' || lon, cityName, lat, lon, country, addedAt, isCurrentLocation FROM favorite_cities")
                // 3. Delete the old table
                db.execSQL("DROP TABLE favorite_cities")
                // 4. Rename the new table
                db.execSQL("ALTER TABLE favorite_cities_new RENAME TO favorite_cities")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tempestia_database"
                ).addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}