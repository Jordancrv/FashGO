package com.example.fashgo.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object ProductList : Screen("products")
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: Long) = "product_detail/$productId"
    }
    object ProductForm : Screen("product_form?productId={productId}") {
        fun createRoute(productId: Long? = null) =
            if (productId != null) "product_form?productId=$productId" else "product_form"
    }
    object NewSale : Screen("new_sale")
    object SaleHistory : Screen("sale_history")
    object SaleDetail : Screen("sale_detail/{saleId}") {
        fun createRoute(saleId: Long) = "sale_detail/$saleId"
    }
    object Inventory : Screen("inventory")
    object Reports : Screen("reports")
    object AbcVentas : Screen("abc_ventas")
    object Settings : Screen("settings")
    object LowStock : Screen("low_stock")
    object SalesChannels : Screen("sales_channels")
    object MateriaPrima : Screen("materia_prima")
    object ProductosDerivados : Screen("productos_derivados")
    object ChannelDetail : Screen("channel_detail/{channelName}") {
        fun createRoute(channelName: String) = "channel_detail/$channelName"
    }
}
