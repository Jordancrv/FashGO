package com.example.fashgo.ui.screens.products

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fashgo.data.remote.model.Producto
import com.example.fashgo.ui.theme.*
import com.example.fashgo.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductListScreen(
    viewModel: ProductViewModel = viewModel(),
    onNavigateToDetail: (String) -> Unit,
    onNavigateToForm: (String?) -> Unit,
    onBack: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val abcByProductId by viewModel.abcByProductId.collectAsState()

    // Filters state
    var showFilters by remember { mutableStateOf(false) }
    val filters = mapOf(
        "PARTE INFERIOR" to listOf("Biker Shorts", "Calzas", "Faldas", "Jeans", "Shorts"),
        "PARTE SUPERIOR" to listOf("Blusa", "Chaleco Estandar", "Poleron Estandar", "Top", "Top Asimetrico"),
        "CUERPO ENTERO" to listOf("Vestido")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Inventario Centralizado", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Almacena todos los productos de la tienda", fontSize = 13.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToForm(null) },
                containerColor = MintGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nuevo", fontWeight = FontWeight.SemiBold)
            }
        },
        containerColor = BackgroundWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar producto...", color = TextTertiary) },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null, tint = TextSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Limpiar", tint = TextSecondary)
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MintGreen,
                    unfocusedBorderColor = Color(0xFFE6E8EC),
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                singleLine = true
            )

            // Category button & product count
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (showFilters) MintGreen.copy(alpha = 0.15f) else Color.White,
                    modifier = Modifier.clickable { showFilters = !showFilters },
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Filled.FilterList,
                            contentDescription = "Filtros",
                            tint = if (showFilters) MintGreenDark else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "Categorías",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (showFilters) MintGreenDark else TextSecondary
                        )
                        Icon(
                            if (showFilters) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = if (showFilters) MintGreenDark else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${products.size} producto(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            // Collapsible filter section
            AnimatedVisibility(
                visible = showFilters,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .heightIn(max = 350.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        filters.forEach { (group, items) ->
                            Text(
                                group,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextTertiary,
                                letterSpacing = 1.sp
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items.forEach { filter ->
                                    FilterChip(
                                        selected = searchQuery.equals(filter, ignoreCase = true),
                                        onClick = {
                                            if (searchQuery.equals(filter, ignoreCase = true)) viewModel.onSearchQueryChange("")
                                            else viewModel.onSearchQueryChange(filter)
                                            showFilters = false
                                        },
                                        label = { Text(filter, fontSize = 13.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MintGreen.copy(alpha = 0.2f),
                                            selectedLabelColor = MintGreenDark,
                                            containerColor = BackgroundWhite
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = searchQuery.equals(filter, ignoreCase = true),
                                            borderColor = Color(0xFFE6E8EC)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Product count removed (it's inline now)

            if (products.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = LightMint,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Outlined.Inventory2,
                                    contentDescription = null,
                                    tint = MintGreen,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No se encontraron productos",
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        if (searchQuery.isNotEmpty()) {
                            Text(
                                "Intenta con otro término de búsqueda",
                                color = TextTertiary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            abc = abcByProductId[product.id] ?: "C",
                            onClick = { onNavigateToDetail(product.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Producto,
    abc: String?,
    onClick: () -> Unit
) {
    val isClassA = abc == "A"

    val stockColor = when {
        !isClassA -> TextPrimary
        product.stock > 50 -> MintGreenDark
        product.stock in 21..50 -> WarningOrange
        else -> ErrorRed
    }

    val cardBg = when {
        !isClassA -> Color.White
        product.stock > 50 -> Color(0xFFE8F5E9)
        product.stock in 21..50 -> Color(0xFFFFF8E1)
        else -> Color(0xFFFFEBEE)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.nombre,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                if (product.codigoProducto.isNotBlank()) {
                    Text(
                        text = product.codigoProducto,
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (product.atributosResumen.isNotBlank()) {
                        Text(
                            product.atributosResumen,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    if (product.ubicacion.isNotEmpty()) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = TextTertiary
                        )
                        Text(
                            product.ubicacion,
                            fontSize = 11.sp,
                            color = TextTertiary,
                            maxLines = 1
                        )
                    }
                }
            }

            // Price, Stock & ABC
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = FormatUtils.formatPrice(product.precio),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = stockColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Stock: ${product.stock}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = stockColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (abc != null) Surface(
                    shape = CircleShape,
                    color = LightBlue,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = abc,
                            color = PrimaryBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

