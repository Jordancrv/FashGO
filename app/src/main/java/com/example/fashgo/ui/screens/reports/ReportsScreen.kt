package com.example.fashgo.ui.screens.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fashgo.ui.theme.BackgroundWhite
import com.example.fashgo.ui.theme.LightBlue
import com.example.fashgo.ui.theme.LightMint
import com.example.fashgo.ui.theme.MintGreen
import com.example.fashgo.ui.theme.PrimaryBlue
import com.example.fashgo.ui.theme.TextPrimary
import com.example.fashgo.ui.theme.TextSecondary
import com.example.fashgo.ui.theme.TextTertiary
import com.example.fashgo.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val abcSalesRows by viewModel.abcSalesRows.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes", fontWeight = FontWeight.Bold) },
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
        Column {
            TabRow(selectedTabIndex = selectedTab, containerColor = Color.White) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Resumen") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("ABC Ventas") })
            }

            when (selectedTab) {
                0 -> ReportsSummaryTab(uiState = uiState, padding = padding)
                1 -> ReportsAbcTab(rows = abcSalesRows, padding = padding)
            }
        }
    }
}

@Composable
private fun ReportsSummaryTab(
    uiState: ReportsUiState,
    padding: PaddingValues
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = padding.calculateTopPadding() + 16.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Resumen del Día", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReportStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Ventas Hoy",
                    value = FormatUtils.formatPrice(uiState.totalHoy),
                    icon = Icons.Filled.TrendingUp,
                    iconColor = MintGreen,
                    bgColor = LightMint
                )
                ReportStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Transacciones",
                    value = "${uiState.countHoy}",
                    icon = Icons.Filled.Receipt,
                    iconColor = PrimaryBlue,
                    bgColor = LightBlue
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Productos Más Vendidos", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Últimos 30 días", fontSize = 12.sp, color = TextSecondary)
        }

        if (uiState.topProducts.isEmpty()) {
            item { EmptyCard("Sin datos disponibles") }
        } else {
            itemsIndexed(uiState.topProducts) { index, item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = LightBlue,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("#${index + 1}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryBlue)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.nombreProducto, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary)
                            Text("${item.cantidad} unidades vendidas", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun ReportsAbcTab(
    rows: List<AbcSalesRow>,
    padding: PaddingValues
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = padding.calculateTopPadding() + 16.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("ABC Ventas", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        item {
            Text(
                "Ventana móvil de 15 días. Al cumplirse cada nuevo día, la clasificación se recalcula automáticamente.",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }

        if (rows.isEmpty()) {
            item {
                EmptyCard("Aún no hay ventas registradas en los últimos 15 días. El ABC comenzará a generarse cuando exista al menos una venta.")
            }
        } else {
            items(rows, key = { it.productId }) { row ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = when (row.clasificacion) {
                                "A" -> MintGreen.copy(alpha = 0.14f)
                                "B" -> Color(0xFFFFF3D6)
                                else -> Color(0xFFFFE1E1)
                            },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    row.clasificacion,
                                    color = when (row.clasificacion) {
                                        "A" -> MintGreen
                                        "B" -> Color(0xFFB7791F)
                                        else -> Color(0xFFC53030)
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(row.nombreProducto, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                            if (row.codigoProducto.isNotBlank()) {
                                Text(row.codigoProducto, fontSize = 11.sp, color = TextTertiary)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${row.unidadesVendidas}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                            Text("ventas", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.BarChart, null, tint = TextTertiary, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(message, color = TextSecondary, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun ReportStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    bgColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = bgColor,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontSize = 12.sp, color = TextSecondary)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
        }
    }
}
