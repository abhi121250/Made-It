package com.example.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.AuthUser
import com.example.auth.UserRole
import com.example.catalog.CatalogItem
import com.example.catalog.CatalogViewModel
import com.example.catalog.ItemType
import com.example.catalog.Store
import com.example.ui.components.BusinessCardSkeleton
import com.example.ui.components.BusinessListSkeleton
import com.example.ui.components.CatalogItemSkeleton
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    user: AuthUser,
    catalogViewModel: CatalogViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val store by catalogViewModel.store.collectAsState()
    val stores by catalogViewModel.stores.collectAsState()
    val categories by catalogViewModel.categories.collectAsState()
    val selectedCatId by catalogViewModel.selectedCategoryId.collectAsState()
    val allItems by catalogViewModel.items.collectAsState()
    val isLoadingFromFirestore by catalogViewModel.isLoadingFromFirestore.collectAsState()

    var showAddItemDialog by remember { mutableStateOf(false) }
    var activeTab by remember { mutableIntStateOf(0) } // 0: Catalog Offerings, 1: Neighborhood Business Directory

    val filteredItems = remember(allItems, selectedCatId) {
        if (selectedCatId == "cat_1") allItems else allItems.filter { it.categoryId == selectedCatId }
    }

    val syncTransition = rememberInfiniteTransition(label = "sync_rotation")
    val syncAngle by syncTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "sync_angle"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (user.role == UserRole.PROVIDER) "Store Catalog & Inventory" else "Local Stores & Services",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isLoadingFromFirestore) "Syncing with Firestore..." else "${store.area}, ${store.city} • Open ${store.openingTime} - ${store.closingTime}",
                            fontSize = 11.sp,
                            color = if (isLoadingFromFirestore) BrandBlue else TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("catalog_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Manual Firestore sync button to test / trigger skeleton animation anytime
                    IconButton(
                        onClick = { catalogViewModel.refreshFromFirestore() },
                        modifier = Modifier.testTag("sync_firestore_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Sync,
                            contentDescription = "Sync from Firestore",
                            tint = BrandBlue,
                            modifier = if (isLoadingFromFirestore) Modifier.rotate(syncAngle) else Modifier
                        )
                    }

                    if (user.role == UserRole.PROVIDER) {
                        IconButton(
                            onClick = { showAddItemDialog = true },
                            modifier = Modifier.testTag("add_item_action_btn")
                        ) {
                            Icon(Icons.Filled.AddCircle, contentDescription = "Add Item", tint = BrandBlue)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // View Mode Tab (Catalog vs Neighborhood Business Directory)
            item {
                Spacer(modifier = Modifier.height(2.dp))
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = BrandBlue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Filled.Storefront, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Store Catalog", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Filled.LocationCity, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Verified Businesses (${stores.size})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    )
                }
            }

            if (activeTab == 0) {
                // Neighborhood Businesses Quick Switcher
                item {
                    Text(
                        text = "Select Local Business",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(stores) { s ->
                            val isSelected = s.id == store.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { catalogViewModel.selectStore(s.id) },
                                label = { Text(s.name, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Store,
                                        contentDescription = s.name,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandBlue,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                ),
                                modifier = Modifier.testTag("business_chip_${s.id}")
                            )
                        }
                    }
                }

                // Store Banner Card or Skeleton Loader
                item {
                    if (isLoadingFromFirestore) {
                        BusinessCardSkeleton(modifier = Modifier.testTag("store_skeleton_loader"))
                    } else {
                        StoreDetailsCard(
                            store = store,
                            isProvider = user.role == UserRole.PROVIDER,
                            onToggleOrders = { catalogViewModel.toggleStoreAcceptingOrders() }
                        )
                    }
                }

                // Categories horizontal filter chips
                item {
                    Text(
                        text = "Browse Categories",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = cat.id == selectedCatId,
                                onClick = { catalogViewModel.selectCategory(cat.id) },
                                label = { Text(cat.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (cat.isService) Icons.Filled.Handyman else Icons.Filled.ShoppingBag,
                                        contentDescription = cat.name,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandBlue,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                ),
                                modifier = Modifier.testTag("cat_chip_${cat.slug}")
                            )
                        }
                    }
                }

                // Inventory / Offerings Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isLoadingFromFirestore) "Fetching Offerings..." else "Available Offerings (${filteredItems.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        if (user.role == UserRole.PROVIDER) {
                            TextButton(onClick = { showAddItemDialog = true }) {
                                Icon(Icons.Filled.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Product/Service", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // List of items or Skeleton items
                if (isLoadingFromFirestore) {
                    items(4) {
                        CatalogItemSkeleton(modifier = Modifier.testTag("item_skeleton_loader"))
                    }
                } else {
                    items(filteredItems) { item ->
                        CatalogItemRowCard(
                            item = item,
                            isProvider = user.role == UserRole.PROVIDER,
                            onToggleStock = { catalogViewModel.toggleItemStock(item.id) },
                            onDelete = { catalogViewModel.deleteItem(item.id) }
                        )
                    }
                }
            } else {
                // Neighborhood Businesses Directory Tab
                item {
                    Text(
                        text = "Verified Local Businesses in Indiranagar & Domlur",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                if (isLoadingFromFirestore) {
                    item {
                        BusinessListSkeleton(count = 4, modifier = Modifier.testTag("directory_skeleton_loader"))
                    }
                } else {
                    items(stores) { s ->
                        BusinessDirectoryCard(
                            store = s,
                            isSelected = s.id == store.id,
                            onSelect = {
                                catalogViewModel.selectStore(s.id)
                                activeTab = 0
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showAddItemDialog) {
        AddCatalogItemDialog(
            categories = categories.filter { it.id != "cat_1" },
            onDismiss = { showAddItemDialog = false },
            onSave = { newItem ->
                catalogViewModel.addItem(newItem)
                showAddItemDialog = false
            }
        )
    }
}

@Composable
private fun BusinessDirectoryCard(
    store: Store,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("directory_card_${store.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) BrandBlueSoft.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isSelected) BrandBlue else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) BrandBlue else AmberOrange),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Store,
                    contentDescription = store.name,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = store.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = if (store.isAcceptingOrders) EmeraldGreenSoft else AmberOrangeSoft,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (store.isAcceptingOrders) "OPEN" else "BUSY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (store.isAcceptingOrders) EmeraldGreen else AmberOrangeDark,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = store.description,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    lineHeight = 15.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = AmberOrange, modifier = Modifier.size(13.dp))
                        Text(text = "${store.rating}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(text = "•", fontSize = 11.sp, color = TextMuted)
                    Text(text = "${store.area}, ${store.city}", fontSize = 11.sp, color = TextSecondary)
                }
            }

            IconButton(onClick = onSelect) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "View Store",
                    tint = BrandBlue
                )
            }
        }
    }
}

@Composable
private fun StoreDetailsCard(
    store: Store,
    isProvider: Boolean,
    onToggleOrders: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Navy900)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AmberOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Storefront, contentDescription = "Store", tint = Navy900, modifier = Modifier.size(24.dp))
                    }
                    Column {
                        Text(
                            text = store.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.Star, contentDescription = "Rating", tint = AmberOrange, modifier = Modifier.size(14.dp))
                            Text(
                                text = "${store.rating} (${store.totalReviews} reviews)",
                                fontSize = 11.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Surface(
                    color = if (store.isAcceptingOrders) EmeraldGreenSoft else AmberOrangeSoft,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (store.isAcceptingOrders) "OPEN" else "CLOSED",
                        color = if (store.isAcceptingOrders) EmeraldGreen else AmberOrangeDark,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = store.description,
                fontSize = 12.sp,
                color = Color(0xFFCBD5E1),
                lineHeight = 16.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Filled.LocationOn, contentDescription = "Address", tint = AmberOrange, modifier = Modifier.size(14.dp))
                Text(
                    text = store.address,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            if (isProvider) {
                HorizontalDivider(color = Color(0xFF334155))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Accept Customer Orders / Bookings",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Switch(
                        checked = store.isAcceptingOrders,
                        onCheckedChange = { onToggleOrders() },
                        modifier = Modifier.testTag("store_open_switch")
                    )
                }
            }
        }
    }
}

@Composable
private fun CatalogItemRowCard(
    item: CatalogItem,
    isProvider: Boolean,
    onToggleStock: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            color = if (item.type == ItemType.SERVICE) BrandBlueSoft else AmberOrangeSoft,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (item.type == ItemType.SERVICE) "ON-DEMAND SERVICE" else "STORE PRODUCT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.type == ItemType.SERVICE) BrandBlue else AmberOrangeDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (!item.inStock) {
                            Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(4.dp)) {
                                Text(
                                    text = "OUT OF STOCK",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = item.description, fontSize = 12.sp, color = TextSecondary, lineHeight = 16.sp)
                }

                if (isProvider) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }

            HorizontalDivider(color = BorderLight)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "₹${item.price.toInt()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (item.compareAtPrice != null && item.compareAtPrice > item.price) {
                        Text(
                            text = "₹${item.compareAtPrice.toInt()}",
                            fontSize = 12.sp,
                            color = TextMuted,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                    Text(
                        text = "/ ${item.unit}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                if (isProvider) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (item.inStock) "In Stock" else "Unavailable",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (item.inStock) EmeraldGreen else MaterialTheme.colorScheme.error
                        )
                        Switch(
                            checked = item.inStock,
                            onCheckedChange = { onToggleStock() },
                            modifier = Modifier.size(36.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = { /* Customer add to cart or book quote */ },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = if (item.type == ItemType.SERVICE) Icons.Filled.CalendarMonth else Icons.Filled.ShoppingCart,
                            contentDescription = "Action",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (item.type == ItemType.SERVICE) "Book Service" else "Add Item",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddCatalogItemDialog(
    categories: List<com.example.catalog.Category>,
    onDismiss: () -> Unit,
    onSave: (CatalogItem) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var unitStr by remember { mutableStateOf("per item") }
    var isService by remember { mutableStateOf(false) }
    var selectedCatId by remember { mutableStateOf(categories.firstOrNull()?.id ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Catalog / Inventory", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !isService,
                        onClick = { isService = false; unitStr = "piece" },
                        label = { Text("Product") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = isService,
                        onClick = { isService = true; unitStr = "visit charge" },
                        label = { Text("Service") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isService) "Service Name" else "Product Title") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & specifications") }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Price (₹)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unitStr,
                        onValueChange = { unitStr = it },
                        label = { Text("Unit / Rate") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val priceVal = priceStr.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && priceVal > 0) {
                        onSave(
                            CatalogItem(
                                storeId = "store_kumar_solutions",
                                categoryId = selectedCatId,
                                type = if (isService) ItemType.SERVICE else ItemType.PRODUCT,
                                title = title,
                                description = description,
                                price = priceVal,
                                unit = unitStr,
                                inStock = true
                            )
                        )
                    }
                }
            ) {
                Text("Save to Store")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
