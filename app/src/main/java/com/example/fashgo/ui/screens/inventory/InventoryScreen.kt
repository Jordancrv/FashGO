package com.example.fashgo.ui.screens.inventory

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fashgo.data.remote.model.Producto
import com.example.fashgo.ui.theme.BackgroundWhite
import com.example.fashgo.ui.theme.ErrorRed
import com.example.fashgo.ui.theme.LightBlue
import com.example.fashgo.ui.theme.LightMint
import com.example.fashgo.ui.theme.MintGreen
import com.example.fashgo.ui.theme.MintGreenDark
import com.example.fashgo.ui.theme.PrimaryBlue
import com.example.fashgo.ui.theme.SuccessGreen
import com.example.fashgo.ui.theme.TextPrimary
import com.example.fashgo.ui.theme.TextSecondary
import com.example.fashgo.ui.theme.TextTertiary
import com.example.fashgo.ui.theme.WarningOrange
import com.example.fashgo.util.FormatUtils
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = viewModel(),
    onBack: () -> Unit,
    onProductDetail: (String) -> Unit,
    onAddProduct: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val products by viewModel.products.collectAsState()
    val ingresos by viewModel.ingresos.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val abcByProductId by viewModel.abcByProductId.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showIngresoDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Producto?>(null) }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { content ->
            viewModel.getProductByQrCode(content) { product ->
                if (product != null) {
                    selectedProduct = product
                    showIngresoDialog = true
                }
            }
        }
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProduct, containerColor = MintGreen, contentColor = Color.White) {
                Icon(Icons.Filled.AddCircle, contentDescription = "Agregar producto")
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Productos e inventario", fontWeight = FontWeight.Bold) },
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
        containerColor = BackgroundWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab, containerColor = Color.White) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Inventario") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Movimientos") })
            }

            uiState.success?.let { message ->
                FeedbackCard(
                    text = message,
                    containerColor = SuccessGreen.copy(alpha = 0.1f),
                    contentColor = SuccessGreen,
                    icon = Icons.Filled.CheckCircle
                )
            }

            when (selectedTab) {
                0 -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search bar stays fixed
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = viewModel::onSearchChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            placeholder = { Text("Buscar por nombre, codigo, color o talla", color = TextTertiary) },
                            leadingIcon = { Icon(Icons.Outlined.Search, null, tint = TextSecondary) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchChange("") }) {
                                        Icon(Icons.Filled.Close, null, tint = TextSecondary)
                                    }
                                } else {
                                    IconButton(onClick = { launchScanner(scanLauncher) }) {
                                        Icon(Icons.Filled.QrCodeScanner, null, tint = MintGreen)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MintGreen,
                                unfocusedBorderColor = Color(0xFFE6E8EC),
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White
                            )
                        )

                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Summary cards scroll away
                            item {
                                InventorySummarySection(
                                    totalSkus = summary.totalSkus,
                                    totalUnits = summary.totalUnits,
                                    redCount = summary.redCount,
                                    totalValue = summary.totalValue
                                )
                            }

                            items(products, key = { it.id }) { product ->
                                InventoryProductCard(
                                    product = product,
                                    abc = abcByProductId[product.id] ?: "-",
                                    onDetail = { onProductDetail(product.id) },
                                    onAddStock = {
                                        selectedProduct = product
                                        showIngresoDialog = true
                                    }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(24.dp)) }
                        }
                    }
                }

                1 -> {
                    if (ingresos.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(shape = CircleShape, color = LightBlue, modifier = Modifier.size(72.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Outlined.History, null, tint = PrimaryBlue, modifier = Modifier.size(32.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Sin movimientos registrados", fontWeight = FontWeight.Medium, color = TextSecondary)
                            }
                        }
                    } else {
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(ingresos, key = { it.id }) { ingreso ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = SuccessGreen.copy(alpha = 0.1f),
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Filled.AddCircle, null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(ingreso.nombreProducto, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary)
                                            if (ingreso.codigoProducto.isNotBlank()) {
                                                Text(ingreso.codigoProducto, fontSize = 11.sp, color = TextTertiary)
                                            }
                                            Text(dateFormat.format(Date(ingreso.fecha)), fontSize = 11.sp, color = TextTertiary)
                                        }
                                        Text("+${ingreso.cantidad}", fontWeight = FontWeight.Bold, color = SuccessGreen, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showIngresoDialog && selectedProduct != null) {
        var cantidad by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showIngresoDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Registrar Ingreso", fontWeight = FontWeight.Bold)
                    Text(selectedProduct!!.nombre, fontSize = 14.sp, color = TextSecondary)
                    Text(selectedProduct!!.codigoProducto, fontSize = 12.sp, color = TextTertiary)
                    Text("Stock actual: ${selectedProduct!!.stock}", fontSize = 12.sp, color = TextTertiary)
                }
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = cantidad,
                        onValueChange = { cantidad = it },
                        label = { Text("Cantidad a agregar") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MintGreen,
                            unfocusedBorderColor = Color(0xFFE6E8EC),
                            focusedLabelColor = MintGreen,
                            cursorColor = MintGreen
                        )
                    )
                    uiState.error?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = ErrorRed, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.registrarIngreso(
                            productoId = selectedProduct!!.id,
                            nombreProducto = selectedProduct!!.nombre,
                            codigoProducto = selectedProduct!!.codigoProducto,
                            cantidad = cantidad
                        ) {
                            showIngresoDialog = false
                            cantidad = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Registrar", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showIngresoDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun InventorySummarySection(
    totalSkus: Int,
    totalUnits: Int,
    redCount: Int,
    totalValue: Double
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard("SKUs", "$totalSkus", LightMint, MintGreenDark, Modifier.weight(1f))
            SummaryCard("Unidades", "$totalUnits", LightBlue, PrimaryBlue, Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard("Alerta roja", "$redCount", WarningOrange.copy(alpha = 0.12f), WarningOrange, Modifier.weight(1f))
            SummaryCard("Valor", FormatUtils.formatPrice(totalValue), Color.White, TextPrimary, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, color = TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
private fun InventoryProductCard(
    product: Producto,
    abc: String,
    onDetail: () -> Unit,
    onAddStock: () -> Unit
) {
    val cardBg = when {
        product.stock > 9 -> Color(0xFFE8F5E9)
        product.stock in 5..9 -> Color(0xFFFFF8E1)
        else -> Color(0xFFFFEBEE)
    }

    val stockColor = when {
        product.stock > 9 -> MintGreenDark
        product.stock in 5..9 -> WarningOrange
        else -> ErrorRed
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        onClick = onDetail
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(product.nombre, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 15.sp)
                    Text(product.codigoProducto, color = TextTertiary, fontSize = 11.sp)
                    if (product.atributosResumen.isNotBlank()) {
                        Text(product.atributosResumen, color = TextSecondary, fontSize = 12.sp)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(FormatUtils.formatPrice(product.precio), fontWeight = FontWeight.Bold, color = MintGreenDark)
                    Spacer(modifier = Modifier.height(6.dp))
                    Pill(text = abc, background = LightBlue, content = PrimaryBlue)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${product.parteGeneral} · ${product.ubicacion}", color = TextSecondary, fontSize = 12.sp)
                Text("Stock: ${product.stock}", color = stockColor, fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDetail) { Text("Ver y editar", color = PrimaryBlue) }
                TextButton(onClick = onAddStock) { Text("Agregar cantidad", color = MintGreenDark) }
            }
        }
    }
}

@Composable
private fun Pill(text: String, background: Color, content: Color) {
    Surface(shape = RoundedCornerShape(999.dp), color = background) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = content,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun FeedbackCard(
    text: String,
    containerColor: Color,
    contentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, null, tint = contentColor, modifier = Modifier.size(18.dp))
            Text(text, color = contentColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

private fun launchScanner(scanLauncher: androidx.activity.result.ActivityResultLauncher<ScanOptions>) {
    val options = ScanOptions()
    options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
    options.setPrompt("Escanea el QR del producto")
    options.setBeepEnabled(true)
    options.setOrientationLocked(false)
    scanLauncher.launch(options)
}

