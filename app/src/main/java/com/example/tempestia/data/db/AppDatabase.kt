package com.example.tempestia.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.tempestia.data.favorites.dataSource.local.FavoriteCityDao
import com.example.tempestia.data.favorites.model.FavoriteCity

@Database(entities = [FavoriteCity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun favoriteCityDao(): FavoriteCityDao

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