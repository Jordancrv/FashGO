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
import com.example.fashgo.data.remote.model.Venta
import com.example.fashgo.ui.theme.*
import com.example.fashgo.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleHistoryScreen(
    viewModel: SaleViewModel = viewModel(),
    onBack: () -> Unit,
    onSaleDetail: (String) -> Unit
) {
    val allVentas by viewModel.allVentas.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Ventas", fontWeight = FontWeight.Bold) },
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
        if (allVentas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
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
                                Icons.Outlined.Receipt,
                                contentDescription = null,
                                tint = MintGreen,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Sin ventas registradas", fontWeight = FontWeight.Medium, color = TextSecondary)
                    Text("Las ventas aparecerán aquí", color = TextTertiary, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Group by date
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val grouped = allVentas.groupBy { dateFormat.format(Date(it.fecha)) }

                grouped.forEach { (date, ventas) ->
                    item {
                        Text(
                            text = date,
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                        )
                    }
                    items(ventas, key = { it.id }) { venta ->
                        SaleCard(venta = venta, onClick = { onSaleDetail(venta.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun SaleCard(venta: Venta, onClick: () -> Unit) {
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MintGreen.copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.Receipt,
                        contentDescription = null,
                        tint = MintGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Venta #${venta.id}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Text(
                    timeFormat.format(Date(venta.fecha)),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            Text(
                FormatUtils.formatPrice(venta.total),
                fontWeight = FontWeight.Bold,
                color = MintGreenDark,
                fontSize = 15.sp
            )
        }
    }
}

