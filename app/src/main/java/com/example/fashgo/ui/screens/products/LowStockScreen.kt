package com.example.fashgo.ui.screens.products

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LowStockScreen(
    viewModel: ProductViewModel = viewModel(),
    onBack: () -> Unit,
    onProductDetail: (Long) -> Unit
) {
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
    val abcByProductId by viewModel.abcByProductId.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stock Bajo", fontWeight = FontWeight.Bold) },
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
        if (lowStockProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(shape = CircleShape, color = SuccessGreen.copy(alpha = 0.1f), modifier = Modifier.size(80.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(36.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("¡Todo en orden!", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("No hay productos con stock bajo", color = TextSecondary, fontSize = 13.sp)
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
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = WarningOrange.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Filled.Warning, null, tint = WarningOrange, modifier = Modifier.size(22.dp))
                            Text(
                                "${lowStockProducts.size} producto(s) necesitan atención",
                                fontWeight = FontWeight.Medium,
                                color = WarningOrange,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                items(lowStockProducts, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        abc = abcByProductId[product.id] ?: "C",
                        onClick = { onProductDetail(product.id) }
                    )
                }
            }
        }
    }
}
