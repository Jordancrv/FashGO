package com.example.fashgo.ui.screens.derivados

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fashgo.data.entity.ProductoDerivado
import com.example.fashgo.ui.theme.BackgroundWhite
import com.example.fashgo.ui.theme.ErrorRed
import com.example.fashgo.ui.theme.MintGreen
import com.example.fashgo.ui.theme.PrimaryBlue
import com.example.fashgo.ui.theme.TextPrimary
import com.example.fashgo.ui.theme.TextSecondary
import com.example.fashgo.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoDerivadoScreen(
    viewModel: ProductoDerivadoViewModel = viewModel(),
    onBack: () -> Unit
) {
    val derivados by viewModel.derivados.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var showForm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productos Derivados", fontWeight = FontWeight.Bold) },
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
            if (!showForm) {
                ExtendedFloatingActionButton(
                    onClick = {
                        viewModel.select(null)
                        showForm = true
                    },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Registrar derivado")
                }
            }
        },
        containerColor = BackgroundWhite
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (showForm) {
                ProductoDerivadoForm(
                    initial = uiState.selected,
                    error = uiState.error,
                    onSave = { tipoPrenda, subcategoria, color, talla, cantidad, precio, ubicacion, nivel, zonaAuxiliar, origen, notas ->
                        if (viewModel.save(tipoPrenda, subcategoria, color, talla, cantidad, precio, ubicacion, nivel, zonaAuxiliar, origen, notas)) {
                            showForm = false
                        }
                    },
                    onCancel = {
                        viewModel.select(null)
                        viewModel.clearError()
                        showForm = false
                    }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(derivados, key = { it.id }) { item ->
                        ProductoDerivadoCard(
                            item = item,
                            onEdit = {
                                viewModel.select(item)
                                showForm = true
                            },
                            onDelete = { viewModel.delete(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductoDerivadoCard(
    item: ProductoDerivado,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.padding(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.nombreCompleto, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Origen: ${item.origen.ifBlank { "Inventario depurado" }}", fontSize = 12.sp, color = TextSecondary)
                }
                Text(FormatUtils.formatPrice(item.precioTotal), fontWeight = FontWeight.Bold, color = MintGreen)
            }
            Text("Cantidad: ${item.cantidad} · Unitario: ${FormatUtils.formatPrice(item.precioUnitario)}", color = TextSecondary, fontSize = 13.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = TextSecondary)
                Spacer(modifier = Modifier.padding(2.dp))
                Text(item.ubicacion, color = TextSecondary, fontSize = 13.sp)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Editar", tint = MintGreen)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Eliminar", tint = ErrorRed)
                }
            }
        }
    }
}

@Composable
private fun ProductoDerivadoForm(
    initial: ProductoDerivado?,
    error: String?,
    onSave: (String, String, String, String, String, String, String, String, String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    var tipoPrenda by remember(initial) { mutableStateOf(initial?.tipoPrenda ?: "") }
    var subcategoria by remember(initial) { mutableStateOf(initial?.subcategoria ?: "") }
    var color by remember(initial) { mutableStateOf(initial?.color ?: "") }
    var talla by remember(initial) { mutableStateOf(initial?.talla ?: "Estandar") }
    var cantidad by remember(initial) { mutableStateOf(initial?.cantidad?.toString() ?: "") }
    var precio by remember(initial) { mutableStateOf(initial?.precioUnitario?.toString() ?: "") }
    var ubicacion by remember(initial) { mutableStateOf(initial?.ubicacion ?: "") }
    var nivel by remember(initial) { mutableStateOf(initial?.nivel ?: "") }
    var zonaAuxiliar by remember(initial) { mutableStateOf(initial?.zonaAuxiliar?.toString() ?: "") }
    var origen by remember(initial) { mutableStateOf(initial?.origen ?: "") }
    var notas by remember(initial) { mutableStateOf(initial?.notas ?: "") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(if (initial == null) "Registrar producto derivado" else "Editar producto derivado", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        OutlinedTextField(tipoPrenda, { tipoPrenda = it }, label = { Text("Tipo de prenda") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(subcategoria, { subcategoria = it }, label = { Text("Subcategoria") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(color, { color = it }, label = { Text("Color") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(talla, { talla = it }, label = { Text("Talla") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(cantidad, { cantidad = it }, label = { Text("Cantidad") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(precio, { precio = it }, label = { Text("Precio unitario") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(ubicacion, { ubicacion = it }, label = { Text("Ubicacion") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(nivel, { nivel = it }, label = { Text("Nivel") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(zonaAuxiliar, { zonaAuxiliar = it }, label = { Text("Zona auxiliar 1 o 2") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(origen, { origen = it }, label = { Text("Origen") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(notas, { notas = it }, label = { Text("Notas") }, modifier = Modifier.fillMaxWidth())
        error?.let { Text(it, color = ErrorRed, fontSize = 13.sp) }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onCancel) { Text("Cancelar") }
            Button(onClick = { onSave(tipoPrenda, subcategoria, color, talla, cantidad, precio, ubicacion, nivel, zonaAuxiliar, origen, notas) }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                Text("Guardar")
            }
        }
    }
}
