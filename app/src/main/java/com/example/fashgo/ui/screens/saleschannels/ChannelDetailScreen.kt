package com.example.fashgo.ui.screens.saleschannels

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fashgo.ui.theme.*
import com.example.fashgo.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelDetailScreen(
    channelName: String,
    viewModel: ChannelDetailViewModel = viewModel(),
    onBack: () -> Unit
) {
    val orders by viewModel.orders.collectAsState()

    LaunchedEffect(channelName) {
        viewModel.setCanal(channelName)
    }

    val enviadas = orders.filter { it.venta.estado == "Enviado" }
    val porEnviar = orders.filter { it.venta.estado == "Por enviar" }
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Por enviar", "Enviado")

    Scaffold(
        topBar = {
            Column {
            TopAppBar(
                title = { 
                    Column {
                        Text(channelName, fontWeight = FontWeight.Bold)
                        Text(
                            "Órdenes de Compra", 
                            fontSize = 12.sp, 
                            color = TextSecondary
                        )
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
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = MintGreen
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = FontWeight.Medium) },
                        selectedContentColor = MintGreen,
                        unselectedContentColor = TextSecondary
                    )
                }
            }
        }
        },
        containerColor = BackgroundWhite
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val currentList = if (selectedTabIndex == 0) porEnviar else enviadas

            if (currentList.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("No hay órdenes de compra en esta sección.", color = TextSecondary)
                    }
                }
            } else {
                items(currentList) { order ->
                    OrderCard(
                        order = order,
                        channelName = channelName,
                        onUpdateEstado = if (selectedTabIndex == 0) { { viewModel.updateEstado(order.venta.id, "Enviado") } } else null
                    )
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    order: OrderWithDetails,
    channelName: String,
    onUpdateEstado: (() -> Unit)?
) {
    var expanded by remember { mutableStateOf(false) }
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${channelName} - ${if (order.venta.codigoOrden.isNotEmpty()) order.venta.codigoOrden else "Venta #${order.venta.id}"}",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Cliente: ${order.venta.cliente}", fontSize = 13.sp, color = TextSecondary)
                    if (order.venta.direccion.isNotEmpty()) {
                        Text("Dirección: ${order.venta.direccion}", fontSize = 13.sp, color = TextSecondary)
                    }
                    Text("Fecha: ${formatter.format(Date(order.venta.fecha))}", fontSize = 12.sp, color = TextTertiary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Monto: ${FormatUtils.formatPrice(order.venta.total)}",
                        fontWeight = FontWeight.Bold,
                        color = MintGreenDark,
                        fontSize = 15.sp
                    )
                }
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    HorizontalDivider(color = Color(0xFFF0F2F5))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Detalle de productos:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    order.detalles.forEach { item ->
                        val color = item.producto?.color ?: "N/A"
                        val talla = item.producto?.talla ?: "N/A"
                        val classAbc = item.producto?.clasificacionAbc ?: "-"
                        val stock = item.producto?.stock ?: 0
                        val ubicacion = item.producto?.ubicacion ?: "Almacén"

                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Text(
                                "${item.detalle.nombreProducto} - $color - Talla $talla",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Cant: ${item.detalle.cantidad} | Precio: ${FormatUtils.formatPrice(item.detalle.precio)}", fontSize = 12.sp, color = TextSecondary)
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MintGreen.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        "$classAbc | Stock: $stock | L: $ubicacion",
                                        fontSize = 10.sp,
                                        color = MintGreenDark,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF0F2F5), modifier = Modifier.padding(top = 4.dp))
                    }

                    if (onUpdateEstado != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onUpdateEstado,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(Icons.Filled.LocalShipping, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Marcar como Enviado")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
