package com.example.fashgo.ui.screens.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fashgo.ui.theme.BackgroundWhite
import com.example.fashgo.ui.theme.ErrorRed
import com.example.fashgo.ui.theme.LightMint
import com.example.fashgo.ui.theme.MintGreen
import com.example.fashgo.ui.theme.MintGreenDark
import com.example.fashgo.ui.theme.TextPrimary
import com.example.fashgo.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    productId: Long? = null,
    viewModel: ProductViewModel = viewModel(),
    onBack: () -> Unit
) {
    val isEditing = productId != null && productId > 0
    val uiState by viewModel.uiState.collectAsState()

    var parteGeneral by remember { mutableStateOf("Parte Superior") }
    var tipo by remember { mutableStateOf("") }
    var diseno by remember { mutableStateOf("") }
    var tipoManga by remember { mutableStateOf("") }
    var tipoCuello by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var talla by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var stockMinimo by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        if (isEditing) {
            viewModel.loadProduct(productId!!)
        }
    }

    LaunchedEffect(uiState.selectedProduct) {
        if (isEditing && !loaded && uiState.selectedProduct != null) {
            val p = uiState.selectedProduct!!
            parteGeneral = p.parteGeneral.ifBlank { "Parte Superior" }
            tipo = p.tipo
            diseno = p.diseno
            tipoManga = p.tipoManga
            tipoCuello = p.tipoCuello
            color = p.color
            talla = p.talla
            precio = p.precio.toString()
            stock = p.stock.toString()
            ubicacion = p.ubicacion
            stockMinimo = p.stockMinimo.toString()
            loaded = true
        }
    }

    LaunchedEffect(parteGeneral, tipo, diseno, tipoManga, tipoCuello, color, talla) {
        viewModel.updateSuggestedCode(
            parteGeneral = parteGeneral,
            tipo = tipo,
            diseno = diseno,
            tipoManga = tipoManga,
            tipoCuello = tipoCuello,
            color = color,
            talla = talla
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditing) "Editar Producto" else "Nuevo Producto", fontWeight = FontWeight.Bold)
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
        containerColor = BackgroundWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Catalogo del producto", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextPrimary)

                    Text("Parte general", color = TextSecondary, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Parte Superior", "Parte Inferior", "Enterizo").forEach { option ->
                            FilterChip(
                                selected = parteGeneral == option,
                                onClick = { parteGeneral = option },
                                label = { Text(option.replace("Parte ", "")) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MintGreen.copy(alpha = 0.15f),
                                    selectedLabelColor = MintGreenDark
                                )
                            )
                        }
                    }

                    FormTextField(tipo, { tipo = it; viewModel.clearMessages() }, "Tipo", Icons.Outlined.ShoppingBag)
                    FormTextField(diseno, { diseno = it; viewModel.clearMessages() }, if (parteGeneral == "Parte Inferior") "Diseno" else "Diseno opcional", Icons.Outlined.Category)

                    if (parteGeneral != "Parte Inferior") {
                        FormTextField(tipoCuello, { tipoCuello = it; viewModel.clearMessages() }, "Tipo de cuello", Icons.Outlined.Category)
                        FormTextField(tipoManga, { tipoManga = it; viewModel.clearMessages() }, "Tipo de manga", Icons.Outlined.Category)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FormTextField(
                            value = color,
                            onValueChange = { color = it; viewModel.clearMessages() },
                            label = "Color",
                            icon = Icons.Outlined.Palette,
                            modifier = Modifier.weight(1f)
                        )
                        FormTextField(
                            value = talla,
                            onValueChange = { talla = it; viewModel.clearMessages() },
                            label = "Talla",
                            icon = Icons.Outlined.Straighten,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = LightMint)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Codigo sugerido", color = MintGreenDark, fontSize = 12.sp)
                            Text(
                                uiState.suggestedCode.ifBlank { "Completa tipo, color y talla" },
                                color = MintGreenDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Inventario y precio", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextPrimary)

                    FormTextField(precio, { precio = it; viewModel.clearMessages() }, "Precio ($)", Icons.Outlined.AttachMoney, keyboardType = KeyboardType.Decimal)

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FormTextField(
                            value = stock,
                            onValueChange = { stock = it; viewModel.clearMessages() },
                            label = "Stock",
                            icon = Icons.Outlined.Inventory,
                            modifier = Modifier.weight(1f),
                            keyboardType = KeyboardType.Number
                        )
                        FormTextField(
                            value = stockMinimo,
                            onValueChange = { stockMinimo = it; viewModel.clearMessages() },
                            label = "Minimo",
                            icon = Icons.Outlined.Warning,
                            modifier = Modifier.weight(1f),
                            keyboardType = KeyboardType.Number
                        )
                    }

                    FormTextField(ubicacion, { ubicacion = it }, "Ubicacion", Icons.Outlined.LocationOn)
                }
            }

            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.ErrorOutline, null, tint = ErrorRed)
                        Text(error, color = ErrorRed, fontSize = 13.sp)
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.saveProduct(
                        id = if (isEditing) productId else null,
                        parteGeneral = parteGeneral,
                        tipo = tipo,
                        diseno = diseno,
                        tipoManga = tipoManga,
                        tipoCuello = tipoCuello,
                        color = color,
                        talla = talla,
                        precio = precio,
                        stock = stock,
                        ubicacion = ubicacion,
                        stockMinimo = stockMinimo,
                        onSuccess = onBack
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                shape = RoundedCornerShape(16.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.height(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(if (isEditing) Icons.Filled.Save else Icons.Filled.Add, null)
                    Spacer(modifier = Modifier.height(0.dp))
                    Text(
                        if (isEditing) "Guardar cambios" else "Crear producto",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LightMint),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.QrCode, null, tint = MintGreenDark)
                    Text("El QR usara el codigo generado para que el escaneo sea estable.", color = MintGreenDark, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = TextSecondary) },
        modifier = modifier,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MintGreen,
            unfocusedBorderColor = Color(0xFFE6E8EC),
            focusedLabelColor = MintGreen,
            cursorColor = MintGreen
        )
    )
}
