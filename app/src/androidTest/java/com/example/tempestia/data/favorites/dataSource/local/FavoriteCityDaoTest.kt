package com.example.tempestia.data.favorites.dataSource.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tempestia.data.db.AppDatabase
import com.example.tempestia.data.favorites.dataSource.local.FavoriteCityDao
import com.example.tempestia.data.favorites.model.FavoriteCity
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoriteCityDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: FavoriteCityDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        dao = database.favoriteCityDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testFavoriteCityDao_insertAndRetrieveCity_returnsCorrectData() = runTest {
        // Given: Create a test city object to insert
        val city = FavoriteCity(
            id = "31.2_29.9",
            cityName = "Alexandria",
            lat = 31.2,
            lon = 29.9,
            country = "EG"
        )

        // When: Insert the city into the database and retrieve it by its coordinates
        dao.insertFavorite(city)
        val retrievedCity = dao.getCityByLatLng(31.2, 29.9)

        // Then: Assert the retrieved city is not null and matches the inserted city's name
        assertNotNull(retrievedCity)
        assertEquals("Alexandria", retrievedCity?.cityName)
    }

    @Test
    fun testFavoriteCityDao_deleteCity_removesFromDatabase() = runTest {
        // Given: Insert an initial city into the database
        val city = FavoriteCity(cityName = "Cairo", lat = 30.0, lon = 31.2)
        dao.insertFavorite(city)

        // When: Delete the city from the database and attempt to retrieve it again
        dao.deleteFavorite(city)
        val retrievedCity = dao.getCityByLatLng(30.0, 31.2)

        // Then: Assert the retrieved city is null, confirming successful deletion
        assertNull(retrievedCity)
    }

    @Test
    fun testFavoriteCityDao_setCityAsCurrent_clearsOldCurrentFlags() = runTest {
        // Given: Insert an initially current city, and prepare a secondary non-current city
        val city1 = FavoriteCity(cityName = "Alexandria", lat = 31.2, lon = 29.9, isCurrentLocation = true)
        val city2 = FavoriteCity(cityName = "Cairo", lat = 30.0, lon = 31.2, isCurrentLocation = false)
        dao.insertFavorite(city1)

        // When: Set the secondary city as the current one, and retrieve all cities from the flow
        dao.setCityAsCurrent(city2)
        val allCities = dao.getAllFavorites().first()
        val retrievedCity1 = allCities.find { it.cityName == "Alexandria" }
        val retrievedCity2 = allCities.find { it.cityName == "Cairo" }

        // Then: Assert the original city is no longer current, and the new city is marked as current
        assertEquals(false, retrievedCity1?.isCurrentLocation)
        assertEquals(true, retrievedCity2?.isCurrentLocation)
    }
}