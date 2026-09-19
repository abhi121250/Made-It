package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.catalog.CatalogViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CatalogSkeletonTest {

    @Test
    fun `test stores and catalog items initialization`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = CatalogViewModel(app)

        val stores = viewModel.stores.value
        assertTrue("Stores list should not be empty", stores.isNotEmpty())
        assertEquals(4, stores.size)

        val activeStore = viewModel.store.value
        assertNotNull(activeStore)
        assertEquals("Kumar Home & Hardware Hub", activeStore.name)

        val items = viewModel.items.value
        assertTrue("Items list should contain catalog items", items.isNotEmpty())
        assertFalse("Initially should not be in loading state", viewModel.isLoadingFromFirestore.value)
    }

    @Test
    fun `test select store triggers items update`() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = CatalogViewModel(app)

        viewModel.selectStore("store_lakshmi_greens")
        // Advances time to allow subtle skeleton loading transition
        advanceTimeBy(600)

        assertEquals("Sri Lakshmi Fresh Greens & Mandi", viewModel.store.value.name)
        assertFalse(viewModel.isLoadingFromFirestore.value)
    }
}
