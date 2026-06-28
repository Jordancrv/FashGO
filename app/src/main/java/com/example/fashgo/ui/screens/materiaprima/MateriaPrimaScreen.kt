package com.example.fashgo.ui.screens.materiaprima

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fashgo.data.entity.MateriaPrima
import com.example.fashgo.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MateriaPrimaScreen(
    viewModel: MateriaPrimaViewModel = viewModel(),
    onBack: () -> Unit
) {
    val materiaPrimaList by viewModel.materiaPrimaList.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showForm by remember { mutableStateOf(false) }
    var showUseDialog by remember { mutableStateOf<MateriaPrima?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Materia Prima", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Volver")
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
                        viewModel.selectMateriaPrima(null)
                        showForm = true
                    },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Nuevo Registro")
                }
            }
        },
        containerColor = BackgroundWhite
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (showForm) {
                MateriaPrimaForm(
                    initialData = uiState.selectedMateriaPrima,
                    onSave = { nombre, desc, cant, unidad, prov ->
                        viewModel.saveMateriaPrima(nombre, desc, cant, unidad, prov)
                        showForm = false
                    },
                    onCancel = { showForm = false }
                )
            } else {
                if (materiaPrimaList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay materia prima registrada", color = TextSecondary)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(materiaPrimaList, key = { it.id }) { mp ->
                            MateriaPrimaCard(
                                materiaPrima = mp,
                                onEdit = {
                                    viewModel.selectMateriaPrima(mp)
                                    showForm = true
                                },
                                onDelete = { viewModel.deleteMateriaPrima(mp) },
                                onUse = { showUseDialog = mp }
                            )
                        }
                    }
                }
            }

            uiState.error?.let { err ->
                AlertDialog(
                    onDismissRequest = { viewModel.clearError() },
                    title = { Text("Aviso") },
                    text = { Text(err) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearError() }) { Text("OK") }
                    }
                )
            }

            showUseDialog?.let { mp ->
                var usoStr by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { showUseDialog = null },
                    title = { Text("Utilizar Materia Prima") },
                    text = {
                        Column {
                            Text("Ingresa la cantidad a descontar de ${mp.nombre} (${mp.unidadMedida}):")
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = usoStr,
                                onValueChange = { usoStr = it },
                                label = { Text("Cantidad") }
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            viewModel.usarMateriaPrima(mp.id, usoStr)
                            showUseDialog = null
                        }) {
                            Text("Descontar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showUseDialog = null }) { Text("Cancelar") }
                    }
                )
            }
        }
    }
}

@Composable
fun MateriaPrimaCard(
    materiaPrima: MateriaPrima,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUse: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Inventory, null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(materiaPrima.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (materiaPrima.proveedor.isNotBlank()) {
                        Text("Proveedor: ${materiaPrima.proveedor}", fontSize = 12.sp, color = TextSecondary)
                    }
                }
                Text(
                    "${materiaPrima.cantidad} ${materiaPrima.unidadMedida}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (materiaPrima.cantidad < 10) ErrorRed else MintGreenDark
                )
            }
            Spacer(Modifier.height(12.dp))
            if (materiaPrima.descripcion.isNotBlank()) {
                Text(materiaPrima.descripcion, fontSize = 13.sp, color = TextSecondary)
                Spacer(Modifier.height(12.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onUse, colors = ButtonDefaults.textButtonColors(contentColor = WarningOrange)) {
                    Icon(Icons.Outlined.RemoveCircleOutline, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Utilizar")
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, null, tint = MintGreen)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, null, tint = ErrorRed)
                }
            }
        }
    }
}

@Composable
fun MateriaPrimaForm(
    initialData: MateriaPrima?,
    onSave: (String, String, String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    var nombre by remember { mutableStateOf(initialData?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(initialData?.descripcion ?: "") }
    var cantidad by remember { mutableStateOf(initialData?.cantidad?.toString() ?: "") }
    var unidad by remember { mutableStateOf(initialData?.unidadMedida ?: "kg") }
    var proveedor by remember { mutableStateOf(initialData?.proveedor ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            if (initialData == null) "Registrar Materia Prima" else "Editar Materia Prima",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = cantidad,
                onValueChange = { cantidad = it },
                label = { Text("Cantidad") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = unidad,
                onValueChange = { unidad = it },
                label = { Text("Unidad (ej. kg, m, u)") },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = proveedor,
            onValueChange = { proveedor = it },
            label = { Text("Proveedor") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onCancel) { Text("Cancelar") }
            Button(
                onClick = { onSave(nombre, descripcion, cantidad, unidad, proveedor) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Guardar")
            }
        }
    }
}
