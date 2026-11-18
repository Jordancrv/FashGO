package com.example.fashgo.ui.screens.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fashgo.ui.theme.*
import com.example.fashgo.util.FormatUtils
import com.example.fashgo.data.offline.OfflineStore
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@Composable
fun HomeScreen(
    userName: String,
    userRole: String,
    viewModel: HomeViewModel = viewModel(),
    onNavigateToProducts: () -> Unit,
    onNavigateToNewSale: () -> Unit,
    onNavigateToSaleHistory: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToAbcVentas: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLowStock: () -> Unit,
    onNavigateToMateriaPrima: () -> Unit,
    onNavigateToProductosDerivados: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAudit: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit
) {
    val totalVentas by viewModel.totalVentasDelDia.collectAsState(initial = 0.0)
    val countVentas by viewModel.countVentasDelDia.collectAsState(initial = 0)
    val lowStockCount by viewModel.lowStockCount.collectAsState(initial = 0)
    val totalProducts by viewModel.totalProducts.collectAsState(initial = 0)
    val totalUnits by viewModel.totalUnits.collectAsState(initial = 0)
    val pendingSync by OfflineStore.pending.collectAsState()

    // Supabase almacena los roles en minúsculas (dueno/vendedor). La app local
    // utilizaba mayúsculas, por lo que el panel administrativo quedaba oculto.
    val isAdmin = userRole.equals("dueno", ignoreCase = true) ||
        userRole.equals("admin", ignoreCase = true)

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            viewModel.resolveProductIdByQrCode(result.contents) { id ->
                id?.let(onNavigateToProductDetail)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val options = ScanOptions()
                    options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                    options.setPrompt("Escanea un producto para ver detalles")
                    options.setBeepEnabled(true)
                    options.setOrientationLocked(false)
                    scanLauncher.launch(options)
                },
                containerColor = MintGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = "Escanear QR")
            }
        },
        containerColor = BackgroundWhite
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Limpio
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Bienvenido",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (lowStockCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = ErrorRed) {
                                    Text("$lowStockCount", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        ) {
                            IconButton(
                                onClick = onNavigateToLowStock,
                                modifier = Modifier
                                    .background(WarningOrange.copy(alpha = 0.1f), CircleShape)
                                    .size(44.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Notifications,
                                    contentDescription = "Alertas",
                                    tint = WarningOrange
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .background(Color(0xFFF0F2F5), CircleShape)
                            .size(44.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Logout,
                            contentDescription = "Cerrar sesión",
                            tint = TextSecondary
                        )
                    }
                }
            }

            // Tarjeta de Resumen Financiero
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MintGreen),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ventas de Hoy", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            FormatUtils.formatPrice(totalVentas ?: 0.0),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(Color.White.copy(alpha = 0.3f))
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Transacciones", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "$countVentas",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            if(pendingSync>0){Card(modifier=Modifier.fillMaxWidth().padding(horizontal=24.dp),colors=CardDefaults.cardColors(containerColor=WarningOrange.copy(alpha=.1f)),shape=RoundedCornerShape(14.dp)){Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){Icon(Icons.Filled.CloudOff,null,tint=WarningOrange);Text("$pendingSync operación(es) pendientes de sincronizar",color=WarningOrange,fontWeight=FontWeight.Medium)}};Spacer(Modifier.height(16.dp))}

            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardMetric("Productos", "$totalProducts", LightBlue, PrimaryBlue, Modifier.weight(1f))
                DashboardMetric("Unidades", "$totalUnits", LightMint, MintGreenDark, Modifier.weight(1f))
                DashboardMetric("Alertas", "$lowStockCount", WarningOrange.copy(alpha = 0.12f), WarningOrange, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Acciones Principales
            Text(
                text = "Acciones Rápidas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Nueva Venta",
                    icon = Icons.Filled.ShoppingCart,
                    gradient = listOf(MintGreen, MintGreenDark),
                    onClick = onNavigateToNewSale
                )
                ActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Productos e inventario",
                    icon = Icons.Filled.Inventory2,
                    gradient = listOf(PrimaryBlue, Color(0xFF0047CC)),
                    onClick = onNavigateToProducts
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Alerta de Stock
            if (lowStockCount > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clickable { onNavigateToLowStock() },
                    colors = CardDefaults.cardColors(containerColor = WarningOrange.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = WarningOrange.copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = WarningOrange,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Alertas de inventario",
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                            Text(
                                "$lowStockCount producto(s) requieren revisión",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = WarningOrange
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Menú de Gestión
            Text(
                text = "Gestión del Negocio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
            )

            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WideMenuTile(
                    title = "Mi perfil",
                    subtitle = "Datos personales y rol de la cuenta",
                    icon = Icons.Outlined.Person,
                    iconColor = MintGreen,
                    onClick = onNavigateToProfile
                )
                WideMenuTile(
                    title = "Historial de Ventas",
                    subtitle = "Revisa todas las ventas realizadas",
                    icon = Icons.Outlined.Receipt,
                    iconColor = MintGreen,
                    onClick = onNavigateToSaleHistory
                )
                if (isAdmin) {
                    WideMenuTile(
                        title = "Movimientos",
                        subtitle = "Auditoría de productos, materias e inventario",
                        icon = Icons.Outlined.History,
                        iconColor = Color(0xFF6A5ACD),
                        onClick = onNavigateToAudit
                    )
                    WideMenuTile(
                        title = "Materia Prima",
                        subtitle = "Control de insumos y materiales",
                        icon = Icons.Outlined.Inventory,
                        iconColor = Color(0xFF8B4513), // Marrón para Materia Prima
                        onClick = onNavigateToMateriaPrima
                    )
                    WideMenuTile(
                        title = "Productos Derivados",
                        subtitle = "Registro y control en zonas auxiliares",
                        icon = Icons.Outlined.Inventory2,
                        iconColor = PrimaryBlue,
                        onClick = onNavigateToProductosDerivados
                    )
                    WideMenuTile(
                        title = "ABC Ventas",
                        subtitle = "Clasificacion automatica cada 15 dias",
                        icon = Icons.Outlined.BarChart,
                        iconColor = Color(0xFFB7791F),
                        onClick = onNavigateToAbcVentas
                    )
                    WideMenuTile(
                        title = "Reportes",
                        subtitle = "Resumen general del rendimiento",
                        icon = Icons.Outlined.BarChart,
                        iconColor = WarningOrange,
                        onClick = onNavigateToReports
                    )

                    WideMenuTile(
                        title = "Ajustes del Sistema",
                        subtitle = "Configuración y preferencias",
                        icon = Icons.Outlined.Settings,
                        iconColor = TextSecondary,
                        onClick = onNavigateToSettings
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // Espacio para el FAB
        }
    }
}

@Composable
private fun DashboardMetric(label:String,value:String,background:Color,content:Color,modifier:Modifier=Modifier){
    Card(modifier=modifier,shape=RoundedCornerShape(16.dp),colors=CardDefaults.cardColors(containerColor=background)){
        Column(modifier=Modifier.padding(horizontal=12.dp,vertical=14.dp)){
            Text(value,fontSize=20.sp,fontWeight=FontWeight.Bold,color=content)
            Text(label,fontSize=11.sp,color=TextSecondary,maxLines=1)
        }
    }
}

@Composable
fun ActionCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = gradient[0].copy(alpha = 0.5f)),
        shape = RoundedCornerShape(20.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradient))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun WideMenuTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconColor.copy(alpha = 0.1f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(26.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, fontSize = 13.sp, color = TextSecondary)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextTertiary)
        }
    }
}

