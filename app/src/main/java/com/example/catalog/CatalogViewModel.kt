package com.example.catalog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CatalogViewModel(application: Application) : AndroidViewModel(application) {
    // Initial categories
    private val _categories = MutableStateFlow<List<Category>>(
        listOf(
            Category("cat_1", "All", "all", "AllInclusive", false),
            Category("cat_2", "Plumbing", "plumbing", "Plumbing", true),
            Category("cat_3", "Electrical", "electrical", "ElectricBolt", true),
            Category("cat_4", "Groceries", "groceries", "LocalGroceryStore", false),
            Category("cat_5", "Carpentry", "carpentry", "Handyman", true),
            Category("cat_6", "Tailoring", "tailoring", "ContentCut", true),
            Category("cat_7", "Appliance Repair", "appliance-repair", "Build", true)
        )
    )
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow("cat_1")
    val selectedCategoryId: StateFlow<String> = _selectedCategoryId.asStateFlow()

    // Firestore loading state for subtle skeleton shimmer animation
    private val _isLoadingFromFirestore = MutableStateFlow(false)
    val isLoadingFromFirestore: StateFlow<Boolean> = _isLoadingFromFirestore.asStateFlow()

    // Verified local businesses
    private val allStores = listOf(
        Store(
            id = "store_kumar_solutions",
            name = "Kumar Home & Hardware Hub",
            slug = "kumar-hardware-hub",
            description = "Authorized retailer for plumbing pipes, electrical fixtures, and quick expert on-call repair services across Indiranagar & Domlur.",
            phone = "+919876543210",
            address = "#42, 12th Main, HAL 2nd Stage, Indiranagar",
            area = "Indiranagar",
            city = "Bengaluru",
            rating = 4.9,
            totalReviews = 52,
            isAcceptingOrders = true,
            openingTime = "08:30 AM",
            closingTime = "09:30 PM"
        ),
        Store(
            id = "store_lakshmi_greens",
            name = "Sri Lakshmi Fresh Greens & Mandi",
            slug = "lakshmi-fresh-greens",
            description = "Daily morning harvested farm-fresh vegetables, organic greens, exotic fruits, and pulses directly sourced from local growers.",
            phone = "+919812345678",
            address = "#108, 100ft Road, Near Metro Station, Indiranagar",
            area = "Indiranagar",
            city = "Bengaluru",
            rating = 4.8,
            totalReviews = 88,
            isAcceptingOrders = true,
            openingTime = "07:00 AM",
            closingTime = "09:00 PM"
        ),
        Store(
            id = "store_kaveri_supermarket",
            name = "Kaveri Daily Provisions & Supermarket",
            slug = "kaveri-daily-supermarket",
            description = "Wholesale rate staples, branded spices, dairy products, cold beverages, and household cleaning essentials.",
            phone = "+919822334455",
            address = "#15, Domlur Intermediate Ring Rd, Amarjyoti Layout",
            area = "Domlur",
            city = "Bengaluru",
            rating = 4.7,
            totalReviews = 114,
            isAcceptingOrders = true,
            openingTime = "08:00 AM",
            closingTime = "10:00 PM"
        ),
        Store(
            id = "store_apex_appliances",
            name = "Apex Rapid Appliance Care",
            slug = "apex-rapid-care",
            description = "Certified multi-brand technicians for AC split & inverter gas refill, geysers, washing machines, and microwave ovens.",
            phone = "+919833445566",
            address = "#88, Double Road, Indiranagar Stage 1",
            area = "Indiranagar",
            city = "Bengaluru",
            rating = 4.9,
            totalReviews = 76,
            isAcceptingOrders = true,
            openingTime = "08:00 AM",
            closingTime = "08:30 PM"
        )
    )

    private val _stores = MutableStateFlow(allStores)
    val stores: StateFlow<List<Store>> = _stores.asStateFlow()

    // Active Selected Store
    private val _store = MutableStateFlow(allStores[0])
    val store: StateFlow<Store> = _store.asStateFlow()

    // Catalog Items database
    private val allCatalogItems = listOf(
        CatalogItem(
            id = "item_1",
            storeId = "store_kumar_solutions",
            categoryId = "cat_2",
            type = ItemType.SERVICE,
            title = "Emergency Tap & Pipe Leak Repair",
            description = "Rapid home inspection and on-the-spot repair of leaking sink pipes, angle valves, or bath fittings.",
            price = 249.0,
            compareAtPrice = 349.0,
            unit = "visit charge",
            inStock = true,
            durationMinutes = 45
        ),
        CatalogItem(
            id = "item_2",
            storeId = "store_kumar_solutions",
            categoryId = "cat_3",
            type = ItemType.SERVICE,
            title = "Ceiling Fan Installation & Repair",
            description = "Safe mounting, rewiring, capacitor replacement, or regulator repair by verified electrician.",
            price = 199.0,
            compareAtPrice = 299.0,
            unit = "per appliance",
            inStock = true,
            durationMinutes = 30
        ),
        CatalogItem(
            id = "item_3",
            storeId = "store_kumar_solutions",
            categoryId = "cat_2",
            type = ItemType.PRODUCT,
            title = "Heavy-Duty Brass Angle Valve (1/2\")",
            description = "Chrome-finished, rust-resistant solid brass quarter-turn valve suitable for geysers and faucets.",
            price = 380.0,
            compareAtPrice = 450.0,
            unit = "piece",
            inStock = true,
            stockQuantity = 45
        ),
        CatalogItem(
            id = "item_4",
            storeId = "store_kumar_solutions",
            categoryId = "cat_3",
            type = ItemType.PRODUCT,
            title = "Havells 10W Cool Day LED Bulb (B22)",
            description = "Energy-saving surge protected 6500K bright white LED bulb with 1-year brand warranty.",
            price = 110.0,
            compareAtPrice = 140.0,
            unit = "piece",
            inStock = true,
            stockQuantity = 120
        ),
        CatalogItem(
            id = "item_5",
            storeId = "store_kumar_solutions",
            categoryId = "cat_7",
            type = ItemType.SERVICE,
            title = "Water Heater / Geyser Service",
            description = "Complete descaling, heating element testing, and thermostat verification.",
            price = 449.0,
            compareAtPrice = 599.0,
            unit = "per unit",
            inStock = true,
            durationMinutes = 60
        ),
        // Lakshmi Greens items
        CatalogItem(
            id = "item_6",
            storeId = "store_lakshmi_greens",
            categoryId = "cat_4",
            type = ItemType.PRODUCT,
            title = "Fresh Farm Country Tomatoes",
            description = "Juicy, naturally ripened farm tomatoes picked this morning. Rich in antioxidants and flavor.",
            price = 32.0,
            compareAtPrice = 40.0,
            unit = "kg",
            inStock = true,
            stockQuantity = 80
        ),
        CatalogItem(
            id = "item_7",
            storeId = "store_lakshmi_greens",
            categoryId = "cat_4",
            type = ItemType.PRODUCT,
            title = "Nasik Organic Red Onions",
            description = "A-Grade crisp red onions, sun-cured with thin skin and pungent taste.",
            price = 35.0,
            compareAtPrice = 45.0,
            unit = "kg",
            inStock = true,
            stockQuantity = 150
        ),
        // Kaveri Provisions items
        CatalogItem(
            id = "item_8",
            storeId = "store_kaveri_supermarket",
            categoryId = "cat_4",
            type = ItemType.PRODUCT,
            title = "Aashirvaad Shudh Chakki Atta (5kg)",
            description = "100% whole wheat flour ground from heavy golden grains, producing soft rotis.",
            price = 235.0,
            compareAtPrice = 270.0,
            unit = "pack",
            inStock = true,
            stockQuantity = 60
        ),
        // Apex Rapid items
        CatalogItem(
            id = "item_9",
            storeId = "store_apex_appliances",
            categoryId = "cat_7",
            type = ItemType.SERVICE,
            title = "Split AC Jet Pump Deep Cleaning & Tune-Up",
            description = "High pressure water gun cleaning for indoor cooling coil, blower wheel, and outdoor condenser.",
            price = 499.0,
            compareAtPrice = 799.0,
            unit = "per AC",
            inStock = true,
            durationMinutes = 60
        )
    )

    private val _items = MutableStateFlow<List<CatalogItem>>(allCatalogItems.filter { it.storeId == "store_kumar_solutions" })
    val items: StateFlow<List<CatalogItem>> = _items.asStateFlow()

    fun selectStore(storeId: String) {
        val selected = allStores.find { it.id == storeId } ?: return
        _store.value = selected

        viewModelScope.launch {
            _isLoadingFromFirestore.value = true
            delay(500) // Brief subtle skeleton shimmer latency
            _items.value = allCatalogItems.filter { it.storeId == storeId }
            _isLoadingFromFirestore.value = false
        }
    }

    /**
     * Manually triggers or simulates a real-time Firestore sync with subtle skeleton loading animation.
     */
    fun refreshFromFirestore() {
        viewModelScope.launch {
            _isLoadingFromFirestore.value = true
            delay(750) // Subtle skeleton shimmer
            _items.value = allCatalogItems.filter { it.storeId == _store.value.id }
            _isLoadingFromFirestore.value = false
        }
    }

    // Provider Store Open/Closed Toggle
    fun toggleStoreAcceptingOrders() {
        val current = _store.value
        val updated = current.copy(isAcceptingOrders = !current.isAcceptingOrders)
        _store.value = updated
        _stores.value = _stores.value.map { if (it.id == updated.id) updated else it }
    }

    fun selectCategory(categoryId: String) {
        _selectedCategoryId.value = categoryId
    }

    // Add new Product or Service
    fun addItem(item: CatalogItem) {
        _items.value = _items.value + item
    }

    // Toggle Item stock status (Provider feature)
    fun toggleItemStock(itemId: String) {
        _items.value = _items.value.map { item ->
            if (item.id == itemId) item.copy(inStock = !item.inStock) else item
        }
    }

    // Delete item
    fun deleteItem(itemId: String) {
        _items.value = _items.value.filter { it.id != itemId }
    }
}
