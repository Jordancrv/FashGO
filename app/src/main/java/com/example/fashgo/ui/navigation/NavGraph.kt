package com.example.fashgo.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.fashgo.ui.screens.derivados.ProductoDerivadoScreen
import com.example.fashgo.ui.screens.home.HomeScreen
import com.example.fashgo.ui.screens.inventory.InventoryScreen
import com.example.fashgo.ui.screens.login.LoginScreen
import com.example.fashgo.ui.screens.products.*
import com.example.fashgo.ui.screens.reports.AbcVentasScreen
import com.example.fashgo.ui.screens.reports.ReportsScreen
import com.example.fashgo.ui.screens.sales.*
import com.example.fashgo.ui.screens.saleschannels.*
import com.example.fashgo.ui.screens.settings.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    var userName by remember { mutableStateOf("") }
    var userRole by remember { mutableStateOf("") }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Login
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { name, role ->
                    userName = name
                    userRole = role
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Home
        composable(Screen.Home.route) {
            HomeScreen(
                userName = userName,
                userRole = userRole,
                onNavigateToProducts = { navController.navigate(Screen.ProductList.route) },
                onNavigateToNewSale = { navController.navigate(Screen.NewSale.route) },
                onNavigateToSaleHistory = { navController.navigate(Screen.SaleHistory.route) },
                onNavigateToInventory = { navController.navigate(Screen.Inventory.route) },
                onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                onNavigateToAbcVentas = { navController.navigate(Screen.AbcVentas.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToLowStock = { navController.navigate(Screen.LowStock.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToSalesChannels = {
                    navController.navigate(Screen.SalesChannels.route)
                },
                onNavigateToMateriaPrima = {
                    navController.navigate(Screen.MateriaPrima.route)
                },
                onNavigateToProductosDerivados = {
                    navController.navigate(Screen.ProductosDerivados.route)
                },
                onNavigateToProductDetail = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                }
            )
        }

        // Products
        composable(Screen.ProductList.route) {
            ProductListScreen(
                onNavigateToDetail = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                },
                onNavigateToForm = { productId ->
                    navController.navigate(Screen.ProductForm.createRoute(productId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.LongType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
            ProductDetailScreen(
                productId = productId,
                onBack = { navController.popBackStack() },
                onEdit = { id ->
                    navController.navigate(Screen.ProductForm.createRoute(id))
                },
                onAddToSale = { id ->
                    navController.navigate(Screen.NewSale.route)
                }
            )
        }

        composable(
            route = Screen.ProductForm.route,
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getLong("productId") ?: -1L
            ProductFormScreen(
                productId = if (productId > 0) productId else null,
                onBack = { navController.popBackStack() }
            )
        }

        // Sales
        composable(Screen.NewSale.route) {
            NewSaleScreen(
                onBack = { navController.popBackStack() },
                onSaleCompleted = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.SaleHistory.route) {
            SaleHistoryScreen(
                onBack = { navController.popBackStack() },
                onSaleDetail = { saleId ->
                    navController.navigate(Screen.SaleDetail.createRoute(saleId))
                }
            )
        }

        composable(
            route = Screen.SaleDetail.route,
            arguments = listOf(navArgument("saleId") { type = NavType.LongType })
        ) { backStackEntry ->
            val saleId = backStackEntry.arguments?.getLong("saleId") ?: 0L
            SaleDetailScreen(
                saleId = saleId,
                onBack = { navController.popBackStack() }
            )
        }

        // Inventory
        composable(Screen.Inventory.route) {
            InventoryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Reports
        composable(Screen.Reports.route) {
            ReportsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AbcVentas.route) {
            AbcVentasScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Low Stock
        composable(Screen.LowStock.route) {
            LowStockScreen(
                onBack = { navController.popBackStack() },
                onProductDetail = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                }
            )
        }

        // Materia Prima
        composable(Screen.MateriaPrima.route) {
            com.example.fashgo.ui.screens.materiaprima.MateriaPrimaScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ProductosDerivados.route) {
            ProductoDerivadoScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Sales Channels
        composable(Screen.SalesChannels.route) {
            SalesChannelsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToChannelDetail = { channelName ->
                    navController.navigate(Screen.ChannelDetail.createRoute(channelName))
                }
            )
        }

        composable(
            route = Screen.ChannelDetail.route,
            arguments = listOf(navArgument("channelName") { type = NavType.StringType })
        ) { backStackEntry ->
            val channelName = backStackEntry.arguments?.getString("channelName") ?: ""
            ChannelDetailScreen(
                channelName = channelName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
