package com.example.tempestia.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.tempestia.data.alerts.model.Alert
import com.example.tempestia.data.alerts.dataSource.locale.AlertsDao
import com.example.tempestia.data.favorites.dataSource.local.FavoriteCityDao
import com.example.tempestia.data.favorites.model.FavoriteCity

@Database(entities = [FavoriteCity::class, Alert::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoriteCityDao(): FavoriteCityDao
    abstract fun alertsDao(): AlertsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tempestia_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}