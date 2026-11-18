package com.example.fashgo.ui.screens.sales

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fashgo.ui.theme.*
import com.example.fashgo.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleDetailScreen(
    saleId: String,
    viewModel: SaleViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val allVentas by viewModel.allVentas.collectAsState()
    val venta = allVentas.find { it.id == saleId }
    var returnDetail by remember { mutableStateOf<com.example.fashgo.data.remote.model.DetalleVenta?>(null) }

    LaunchedEffect(saleId) {
        viewModel.loadSaleDetails(saleId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle Venta #$saleId", fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 16.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SuccessGreen.copy(alpha = 0.1f),
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Venta Completada", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            FormatUtils.formatPrice(venta?.total ?: 0.0),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MintGreen
                        )
                        if (venta != null) {
                            val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy  hh:mm a", java.util.Locale.getDefault())
                            Text(
                                dateFormat.format(java.util.Date(venta.fecha)),
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Products header
            item {
                Text(
                    "Productos (${uiState.ventaDetalles.size})",
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Product details
            items(uiState.ventaDetalles) { detalle ->
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
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = LightMint,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Outlined.ShoppingBag,
                                    contentDescription = null,
                                    tint = MintGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                detalle.nombreProducto,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Text(
                                "${detalle.cantidad}x  ${FormatUtils.formatPrice(detalle.precio)}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        Text(
                            FormatUtils.formatPrice(detalle.cantidad * detalle.precio),
                            fontWeight = FontWeight.Bold,
                            color = MintGreenDark,
                            fontSize = 14.sp
                        )
                    }
                    val returned=uiState.returnedByDetail[detalle.id]?:0
                    val remaining=detalle.cantidad-returned
                    if(returned>0) Text("Devuelto: $returned",modifier=Modifier.padding(horizontal=14.dp),fontSize=12.sp,color=WarningOrange)
                    if(remaining>0&&venta?.estadoContable=="confirmada") TextButton(onClick={returnDetail=detalle},modifier=Modifier.align(Alignment.End).padding(horizontal=8.dp)){Text("Registrar devolución ($remaining disponible)",color=PrimaryBlue)}
                }
            }
        }
    }
    returnDetail?.let{detail->var quantity by remember(detail.id){mutableStateOf("")};var reason by remember(detail.id){mutableStateOf("")};val max=detail.cantidad-(uiState.returnedByDetail[detail.id]?:0)
        AlertDialog(onDismissRequest={if(!uiState.isProcessing)returnDetail=null},title={Text("Devolver producto")},text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)){Text(detail.nombreProducto,color=TextSecondary);OutlinedTextField(quantity,{quantity=it.filter(Char::isDigit).take(4)},label={Text("Cantidad (máximo $max)")},singleLine=true);OutlinedTextField(reason,{reason=it},label={Text("Motivo")});uiState.error?.let{Text(it,color=ErrorRed,fontSize=12.sp)}}},confirmButton={Button(enabled=!uiState.isProcessing,onClick={viewModel.returnItem(saleId,detail.id,quantity.toIntOrNull()?:0,reason){returnDetail=null}}){Text(if(uiState.isProcessing)"Procesando…" else "Confirmar devolución")}},dismissButton={TextButton(onClick={returnDetail=null}){Text("Cancelar")}})
    }
}

