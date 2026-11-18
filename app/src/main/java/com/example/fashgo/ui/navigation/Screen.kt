package com.example.fashgo.ui.navigation
sealed class Screen(val route:String){
 data object Login:Screen("login"); data object Home:Screen("home"); data object ProductList:Screen("products")
 data object ProductDetail:Screen("product_detail/{productId}"){fun createRoute(id:String)="product_detail/$id"}
 data object ProductForm:Screen("product_form?productId={productId}"){fun createRoute(id:String?=null)=if(id==null)"product_form" else "product_form?productId=$id"}
 data object NewSale:Screen("new_sale"); data object SaleHistory:Screen("sale_history"); data object SaleDetail:Screen("sale_detail/{saleId}"){fun createRoute(id:String)="sale_detail/$id"}
 data object Inventory:Screen("inventory"); data object Reports:Screen("reports"); data object AbcVentas:Screen("abc_ventas"); data object Settings:Screen("settings"); data object LowStock:Screen("low_stock"); data object SalesChannels:Screen("sales_channels"); data object MateriaPrima:Screen("materia_prima"); data object ProductosDerivados:Screen("productos_derivados"); data object ChannelDetail:Screen("channel_detail/{channelName}"){fun createRoute(v:String)="channel_detail/${android.net.Uri.encode(v)}"}
 data object Profile:Screen("profile");data object Audit:Screen("audit")
}
