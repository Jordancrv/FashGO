package com.example.fashgo.ui.screens.reports

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fashgo.data.database.FashGoDatabase
import com.example.fashgo.data.entity.DetalleVenta
import com.example.fashgo.data.entity.ProductSalesStat
import com.example.fashgo.data.entity.Producto
import com.example.fashgo.data.entity.Venta
import com.example.fashgo.data.repository.ProductoRepository
import com.example.fashgo.data.repository.VentaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

data class AbcSalesRow(
    val productId: Long,
    val nombreProducto: String,
    val codigoProducto: String,
    val unidadesVendidas: Int,
    val clasificacion: String
)

data class ReportsUiState(
    val ventasHoy: List<Venta> = emptyList(),
    val totalHoy: Double = 0.0,
    val countHoy: Int = 0,
    val topProducts: List<DetalleVenta> = emptyList(),
    val topChannels: List<com.example.fashgo.data.entity.TopChannel> = emptyList(),
    val ventasRango: List<Venta> = emptyList(),
    val totalRango: Double = 0.0
)

class ReportsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FashGoDatabase.getDatabase(application)
    private val ventaRepo = VentaRepository(db.ventaDao())
    private val productoRepo = ProductoRepository(db.productoDao())

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState

    private val fifteenDaySalesStats = ventaRepo.getProductSalesStats(
        System.currentTimeMillis() - TimeUnit.DAYS.toMillis(15),
        System.currentTimeMillis()
    )

    val abcSalesRows: StateFlow<List<AbcSalesRow>> = combine(
        productoRepo.getAll(),
        fifteenDaySalesStats
    ) { products, stats ->
        buildAbcSalesRows(products, stats)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        loadDailyReport()
        loadTopProducts()
        loadTopChannels()
    }

    private fun getStartOfDay(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun getEndOfDay(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    private fun loadDailyReport() {
        viewModelScope.launch {
            ventaRepo.getVentasDelDia(getStartOfDay(), getEndOfDay()).collect { ventas ->
                _uiState.value = _uiState.value.copy(
                    ventasHoy = ventas,
                    totalHoy = ventas.sumOf { it.total },
                    countHoy = ventas.size
                )
            }
        }
    }

    private fun loadTopProducts() {
        viewModelScope.launch {
            val end = System.currentTimeMillis()
            val start = end - TimeUnit.DAYS.toMillis(30)
            val top = ventaRepo.getTopProducts(start, end, 5)
            _uiState.value = _uiState.value.copy(topProducts = top)
        }
    }

    private fun loadTopChannels() {
        viewModelScope.launch {
            val end = System.currentTimeMillis()
            val start = end - TimeUnit.DAYS.toMillis(30)
            val top = ventaRepo.getTopChannels(start, end, 5)
            _uiState.value = _uiState.value.copy(topChannels = top)
        }
    }

    fun loadRangeReport(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            ventaRepo.getByDateRange(startDate, endDate).collect { ventas ->
                _uiState.value = _uiState.value.copy(
                    ventasRango = ventas,
                    totalRango = ventas.sumOf { it.total }
                )
            }
        }
    }

    private fun buildAbcSalesRows(
        products: List<Producto>,
        stats: List<ProductSalesStat>
    ): List<AbcSalesRow> {
        if (stats.isEmpty()) return emptyList()

        val soldByProductId = stats.associate { it.productoId to it.unidadesVendidas }
        val productsWithSales = products.filter { (soldByProductId[it.id] ?: 0) > 0 }
            .sortedByDescending { soldByProductId[it.id] ?: 0 }

        if (productsWithSales.isEmpty()) return emptyList()

        val topCut = ceil(productsWithSales.size / 3.0).toInt().coerceAtLeast(1)
        val aProductIds = productsWithSales.take(topCut).map { it.id }.toSet()

        return products
            .map { product ->
                val sold = soldByProductId[product.id] ?: 0
                val classification = when {
                    sold == 0 -> "C"
                    product.id in aProductIds -> "A"
                    else -> "B"
                }
                AbcSalesRow(
                    productId = product.id,
                    nombreProducto = product.nombre,
                    codigoProducto = product.codigoProducto,
                    unidadesVendidas = sold,
                    clasificacion = classification
                )
            }
            .sortedWith(
                compareBy<AbcSalesRow> { abcOrder(it.clasificacion) }
                    .thenByDescending { it.unidadesVendidas }
                    .thenBy { it.nombreProducto }
            )
    }

    private fun abcOrder(value: String): Int = when (value) {
        "A" -> 0
        "B" -> 1
        else -> 2
    }
}
