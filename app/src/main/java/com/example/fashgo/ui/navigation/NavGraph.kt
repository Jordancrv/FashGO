package com.example.fashgo.ui.navigation
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.fashgo.data.cloud.SupabaseClient
import com.example.fashgo.data.remote.RemoteProductRepository
import com.example.fashgo.data.remote.RemoteSalesRepository
import com.example.fashgo.ui.screens.derivados.ProductoDerivadoScreen
import com.example.fashgo.ui.screens.home.HomeScreen
import com.example.fashgo.ui.screens.inventory.InventoryScreen
import com.example.fashgo.ui.screens.login.*
import com.example.fashgo.ui.screens.materiaprima.MateriaPrimaScreen
import com.example.fashgo.ui.screens.products.*
import com.example.fashgo.ui.screens.reports.*
import com.example.fashgo.ui.screens.sales.*
import com.example.fashgo.ui.screens.saleschannels.*
import com.example.fashgo.ui.screens.settings.SettingsScreen
import com.example.fashgo.ui.screens.profile.ProfileScreen
import com.example.fashgo.ui.screens.audit.AuditScreen

@Composable fun NavGraph(navController:NavHostController,startDestination:String=Screen.Login.route){
 val login:LoginViewModel=viewModel();var user by remember{mutableStateOf(SupabaseClient.currentUser.value)}
 NavHost(navController,startDestination=if(user==null)startDestination else Screen.Home.route){
  composable(Screen.Login.route){LoginScreen(login){u->user=u;navController.navigate(Screen.Home.route){popUpTo(Screen.Login.route){inclusive=true}}}}
  composable(Screen.Home.route){val u=user?:return@composable;HomeScreen(u.nombre,u.rol,onNavigateToProducts={navController.navigate(Screen.ProductList.route)},onNavigateToNewSale={navController.navigate(Screen.NewSale.route)},onNavigateToSaleHistory={navController.navigate(Screen.SaleHistory.route)},onNavigateToReports={navController.navigate(Screen.Reports.route)},onNavigateToAbcVentas={navController.navigate(Screen.AbcVentas.route)},onNavigateToSettings={navController.navigate(Screen.Settings.route)},onNavigateToLowStock={navController.navigate(Screen.LowStock.route)},onNavigateToMateriaPrima={navController.navigate(Screen.MateriaPrima.route)},onNavigateToProductosDerivados={navController.navigate(Screen.ProductosDerivados.route)},onNavigateToProfile={navController.navigate(Screen.Profile.route)},onNavigateToAudit={navController.navigate(Screen.Audit.route)},onLogout={user=null;login.logout();RemoteProductRepository.clear();RemoteSalesRepository.clear();navController.navigate(Screen.Login.route){popUpTo(0){inclusive=true}}},onNavigateToProductDetail={navController.navigate(Screen.ProductDetail.createRoute(it))})}
  composable(Screen.ProductList.route){InventoryScreen(onBack={navController.popBackStack()},onProductDetail={navController.navigate(Screen.ProductDetail.createRoute(it))},onAddProduct={navController.navigate(Screen.ProductForm.createRoute())})}
  composable(Screen.ProductDetail.route,listOf(navArgument("productId"){type=NavType.StringType})){e->val id=e.arguments?.getString("productId")?:return@composable;ProductDetailScreen(id,onBack={navController.popBackStack()},onEdit={navController.navigate(Screen.ProductForm.createRoute(it))},onAddToSale={navController.navigate(Screen.NewSale.route)})}
  composable(Screen.ProductForm.route,listOf(navArgument("productId"){type=NavType.StringType;nullable=true;defaultValue=null})){e->ProductFormScreen(e.arguments?.getString("productId"),onBack={navController.popBackStack()})}
  composable(Screen.NewSale.route){NewSaleScreen(onBack={navController.popBackStack()},onSaleCompleted={navController.popBackStack()})}
  composable(Screen.SaleHistory.route){SaleHistoryScreen(onBack={navController.popBackStack()},onSaleDetail={navController.navigate(Screen.SaleDetail.createRoute(it))})}
  composable(Screen.SaleDetail.route,listOf(navArgument("saleId"){type=NavType.StringType})){e->SaleDetailScreen(e.arguments?.getString("saleId")?:return@composable,onBack={navController.popBackStack()})}
  composable(Screen.Inventory.route){InventoryScreen(onBack={navController.popBackStack()},onProductDetail={navController.navigate(Screen.ProductDetail.createRoute(it))},onAddProduct={navController.navigate(Screen.ProductForm.createRoute())})};composable(Screen.LowStock.route){LowStockScreen(onBack={navController.popBackStack()},onProductDetail={navController.navigate(Screen.ProductDetail.createRoute(it))})}
  composable(Screen.Reports.route){ReportsScreen(onBack={navController.popBackStack()})};composable(Screen.AbcVentas.route){AbcVentasScreen(onBack={navController.popBackStack()})};composable(Screen.Settings.route){SettingsScreen(onBack={navController.popBackStack()})}
  composable(Screen.MateriaPrima.route){MateriaPrimaScreen(onBack={navController.popBackStack()})};composable(Screen.ProductosDerivados.route){ProductoDerivadoScreen(onBack={navController.popBackStack()})}
  composable(Screen.Profile.route){ProfileScreen(onBack={navController.popBackStack()})};composable(Screen.Audit.route){AuditScreen(onBack={navController.popBackStack()})}
 }
}
