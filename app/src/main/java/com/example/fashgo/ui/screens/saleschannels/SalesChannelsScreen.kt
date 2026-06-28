package com.example.fashgo.ui.screens.saleschannels

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fashgo.ui.screens.home.WideMenuTile
import com.example.fashgo.ui.theme.BackgroundWhite
import com.example.fashgo.ui.theme.MintGreen
import com.example.fashgo.ui.theme.PrimaryBlue
import com.example.fashgo.ui.theme.TextPrimary
import com.example.fashgo.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesChannelsScreen(
    onBack: () -> Unit,
    onNavigateToChannelDetail: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Canales de Venta", fontWeight = FontWeight.Bold) },
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Marketplaces (Solo Categoría A)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            WideMenuTile(
                title = "Falabella",
                subtitle = "Ventas en marketplace Falabella",
                icon = Icons.Outlined.LocalMall,
                iconColor = Color(0xFF00B050), // Verde Falabella
                onClick = { onNavigateToChannelDetail("Falabella") }
            )
            
            WideMenuTile(
                title = "Mercado Libre",
                subtitle = "Ventas en marketplace Mercado Libre",
                icon = Icons.Outlined.LocalMall,
                iconColor = Color(0xFFFFD100), // Amarillo ML
                onClick = { onNavigateToChannelDetail("Mercado Libre") }
            )

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Directos (Todas las Categorías)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            WideMenuTile(
                title = "Página Web",
                subtitle = "Ventas en el e-commerce (A, B)",
                icon = Icons.Outlined.Language,
                iconColor = MintGreen,
                onClick = { onNavigateToChannelDetail("Página Web") }
            )

            WideMenuTile(
                title = "Ventas Presenciales",
                subtitle = "Ventas en tienda física (A, B, C)",
                icon = Icons.Outlined.Storefront,
                iconColor = PrimaryBlue,
                onClick = { onNavigateToChannelDetail("Ventas Presenciales") }
            )
        }
    }
}
