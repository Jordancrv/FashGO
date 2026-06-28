package com.example.fashgo.ui.screens.sales

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fashgo.data.entity.Producto
import com.example.fashgo.ui.theme.*
import com.example.fashgo.util.FormatUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSaleScreen(
    viewModel: SaleViewModel = viewModel(),
    onBack: () -> Unit,
    onSaleCompleted: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val abcByProductId by viewModel.abcByProductId.collectAsState()
    var showCheckout by remember { mutableStateOf(false) }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            viewModel.addToCartByQrCode(result.contents)
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Venta", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetSale()
                        onBack()
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (uiState.cart.isNotEmpty()) {
                        Badge(containerColor = MintGreen) {
                            Text("${uiState.cart.sumOf { it.cantidad }}", color = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            if (uiState.cart.isNotEmpty()) {
                Surface(
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total", color = TextSecondary, fontSize = 13.sp)
                                Text(
                                    FormatUtils.formatPrice(uiState.total),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = TextPrimary
                                )
                            }
                            Button(
                                onClick = { showCheckout = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.height(50.dp)
                            ) {
                                Icon(Icons.Filled.ShoppingCartCheckout, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cobrar", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        },
        containerColor = BackgroundWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar to add products
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar producto para agregar...", color = TextTertiary) },
                leadingIcon = { Icon(Icons.Outlined.Search, null, tint = TextSecondary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchChange("") }) {
                            Icon(Icons.Filled.Close, null, tint = TextSecondary)
                        }
                    } else {
                        IconButton(onClick = {
                            val options = ScanOptions()
                            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                            options.setPrompt("Escanea el QR del producto")
                            options.setBeepEnabled(true)
                            options.setOrientationLocked(false)
                            scanLauncher.launch(options)
                        }) {
                            Icon(Icons.Filled.QrCodeScanner, contentDescription = "Escanear QR", tint = MintGreen)
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

            // Search results dropdown
            if (searchQuery.isNotEmpty() && searchResults.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .heightIn(max = 200.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    LazyColumn {
                        items(searchResults) { producto ->
                            ListItem(
                                headlineContent = {
                                    Text(producto.nombre, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                },
                                supportingContent = {
                                    Text(
                                        "${FormatUtils.formatPrice(producto.precio)} • Stock: ${producto.stock} • ABC: ${abcByProductId[producto.id] ?: producto.clasificacionAbc}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                },
                                trailingContent = {
                                    IconButton(
                                        onClick = { viewModel.addToCart(producto) },
                                        enabled = producto.stock > 0
                                    ) {
                                        Icon(
                                            Icons.Filled.AddCircle,
                                            contentDescription = "Agregar",
                                            tint = if (producto.stock > 0) MintGreen else TextTertiary
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            HorizontalDivider(color = Color(0xFFF0F2F5))
                        }
                    }
                }
            }

            // Error
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.ErrorOutline, null, tint = ErrorRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(error, color = ErrorRed, fontSize = 13.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.clearError() }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Close, null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            if (uiState.cart.isEmpty()) {
                // Empty cart state
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
                                    Icons.Outlined.ShoppingCart,
                                    contentDescription = null,
                                    tint = MintGreen,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Carrito vacío", fontWeight = FontWeight.Medium, color = TextSecondary)
                        Text("Busca productos para agregar", color = TextTertiary, fontSize = 13.sp)
                    }
                }
            } else {
                // Cart items
                Text(
                    "Carrito (${uiState.cart.size} productos)",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.cart, key = { it.producto.id }) { item ->
                        CartItemCard(
                            item = item,
                            abc = abcByProductId[item.producto.id] ?: item.producto.clasificacionAbc,
                            onQuantityChange = { viewModel.updateQuantity(item.producto.id, it) },
                            onRemove = { viewModel.removeFromCart(item.producto.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }
    }

    // Checkout dialog
    if (showCheckout) {
        CheckoutDialog(
            total = uiState.total,
            montoRecibido = uiState.montoRecibido,
            cambio = uiState.cambio,
            canal = uiState.canal,
            cliente = uiState.cliente,
            direccion = uiState.direccion,
            isProcessing = uiState.isProcessing,
            error = uiState.error,
            onMontoChange = { viewModel.onMontoRecibidoChange(it) },
            onCanalChange = { viewModel.onCanalChange(it) },
            onClienteChange = { viewModel.onClienteChange(it) },
            onDireccionChange = { viewModel.onDireccionChange(it) },
            onConfirm = {
                viewModel.completeSale {
                    showCheckout = false
                    onSaleCompleted()
                }
            },
            onDismiss = { showCheckout = false }
        )
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    abc: String,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.producto.nombre,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = TextPrimary
                )
                Text(
                    "${FormatUtils.formatPrice(item.precioUnitario)} c/u",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    "ABC: $abc • Stock: ${item.producto.stock}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            // Quantity controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { onQuantityChange(item.cantidad - 1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.RemoveCircleOutline,
                        contentDescription = "Menos",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    "${item.cantidad}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.width(24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = TextPrimary
                )
                IconButton(
                    onClick = { onQuantityChange(item.cantidad + 1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.AddCircleOutline,
                        contentDescription = "Más",
                        tint = MintGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Subtotal
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    FormatUtils.formatPrice(item.subtotal),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MintGreenDark
                )
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Eliminar",
                        tint = ErrorRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutDialog(
    total: Double,
    montoRecibido: String,
    cambio: Double,
    canal: String,
    cliente: String,
    direccion: String,
    isProcessing: Boolean,
    error: String?,
    onMontoChange: (String) -> Unit,
    onCanalChange: (String) -> Unit,
    onClienteChange: (String) -> Unit,
    onDireccionChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = CircleShape,
                    color = MintGreen.copy(alpha = 0.1f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.PointOfSale, null, tint = MintGreen, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Finalizar Venta", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Total
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LightMint),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total a cobrar", fontWeight = FontWeight.Medium, color = TextSecondary)
                        Text(
                            FormatUtils.formatPrice(total),
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = MintGreenDark
                        )
                    }
                }

                // Selección de Canal
                var expanded by remember { mutableStateOf(false) }
                val canales = listOf("Ventas Presenciales", "Falabella", "Mercado Libre", "Página Web")

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = canal,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Canal de Venta") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintGreen)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        canales.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    onCanalChange(option)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                if (canal != "Ventas Presenciales") {
                    OutlinedTextField(
                        value = cliente,
                        onValueChange = onClienteChange,
                        label = { Text("Cliente") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintGreen)
                    )
                    OutlinedTextField(
                        value = direccion,
                        onValueChange = onDireccionChange,
                        label = { Text("Dirección de Envío") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintGreen)
                    )
                } else {
                    // Monto recibido solo para presencial (asumiendo que en marketplace ya está pagado)
                    OutlinedTextField(
                        value = montoRecibido,
                        onValueChange = onMontoChange,
                        label = { Text("Monto recibido") },
                        leadingIcon = { Icon(Icons.Outlined.AttachMoney, null, tint = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MintGreen,
                            unfocusedBorderColor = Color(0xFFE6E8EC),
                            focusedLabelColor = MintGreen,
                            cursorColor = MintGreen
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                // Cambio
                if (cambio > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Cambio", fontWeight = FontWeight.Medium, color = TextSecondary)
                            Text(
                                FormatUtils.formatPrice(cambio),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = SuccessGreen
                            )
                        }
                    }
                }

                // Error
                error?.let {
                    Text(it, color = ErrorRed, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                shape = RoundedCornerShape(12.dp),
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Confirmar Venta", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}
