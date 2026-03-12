package com.example.tempestia.data.favorites.dataSource.local

import android.content.Context
import com.example.tempestia.data.favorites.model.FavoriteCity
import io.mockk.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FavoritesLocalDatasourceTest {

    private lateinit var mockContext: Context
    private lateinit var mockDao: FavoriteCityDao
    private lateinit var datasource: FavoritesLocalDatasource

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        mockDao = mockk(relaxed = true)

        datasource = FavoritesLocalDatasource(
            context = mockContext,
            favoriteCityDao = mockDao
        )
    }

    @Test
    fun testFavoritesLocalDatasource_insertFavorite_delegatesToDao() = runTest {
        // Given: Create a test city and mock the DAO's insert behavior
        val city = FavoriteCity(cityName = "Alexandria", lat = 31.2, lon = 29.9)
        coEvery { mockDao.insertFavorite(city) } just Runs

        // When: We call insertFavorite on the DataSource
        datasource.insertFavorite(city)

        // Then: Assert that the exact city was passed to the DAO exactly once
        coVerify(exactly = 1) { mockDao.insertFavorite(city) }
    }

    @Test
    fun testFavoritesLocalDatasource_getCityByLatLng_delegatesAndReturnsResult() = runTest {
        // Given: Create a test city and mock the DAO to return it when queried
        val city = FavoriteCity(cityName = "Alexandria", lat = 31.2, lon = 29.9)
        coEvery { mockDao.getCityByLatLng(31.2, 29.9) } returns city

        // When: We query the DataSource for a city by coordinates
        val result = datasource.getCityByLatLng(31.2, 29.9)

        // Then: Assert the result matches the mocked city and the DAO was queried correctly
        assertEquals(city, result)
        coVerify(exactly = 1) { mockDao.getCityByLatLng(31.2, 29.9) }
    }

    @Test
    fun testFavoritesLocalDatasource_getAllFavorites_delegatesToDao() {
        // Given: Mock a Flow of a city list to be returned by the DAO
        val fakeFlow = flowOf(listOf(FavoriteCity(cityName = "Alexandria", lat = 31.2, lon = 29.9)))
        every { mockDao.getAllFavorites() } returns fakeFlow

        // When: We request all favorites from the DataSource
        val result = datasource.getAllFavorites()

        // Then: Assert the returned Flow matches our fake Flow and the DAO was called
        assertEquals(fakeFlow, result)
        verify(exactly = 1) { mockDao.getAllFavorites() }
    }

    @Test
    fun testFavoritesLocalDatasource_deleteFavorite_delegatesToDao() = runTest {
        // Given: Create a test city and mock the DAO's delete behavior
        val city = FavoriteCity(cityName = "Cairo", lat = 30.0, lon = 31.2)
        coEvery { mockDao.deleteFavorite(city) } just Runs

        // When: We call deleteFavorite on the DataSource
        datasource.deleteFavorite(city)

        // Then: Assert that the exact city was passed to the DAO exactly once for deletion
        coVerify(exactly = 1) { mockDao.deleteFavorite(city) }
    }
}