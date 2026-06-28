package com.example.fashgo.ui.screens.inventory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fashgo.data.database.FashGoDatabase
import com.example.fashgo.data.entity.Ingreso
import com.example.fashgo.data.entity.Producto
import com.example.fashgo.data.repository.IngresoRepository
import com.example.fashgo.data.repository.ProductoRepository
import com.example.fashgo.data.repository.VentaRepository
import com.example.fashgo.util.InventoryAnalytics
import com.example.fashgo.util.InventorySummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class InventoryUiState(
    val error: String? = null,
    val success: String? = null,
    val isLoading: Boolean = false
)

class InventoryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FashGoDatabase.getDatabase(application)
    private val productoRepo = ProductoRepository(db.productoDao())
    private val ingresoRepo = IngresoRepository(db.ingresoDao())
    private val ventaRepo = VentaRepository(db.ventaDao())

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    @OptIn(ExperimentalCoroutinesApi::class)
    val products: StateFlow<List<Producto>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) productoRepo.getAll()
            else {
                productoRepo.getAll().map { list ->
                    val terms = query.lowercase().split(" ").filter { it.isNotBlank() }
                    list.filter { prod ->
                        val text = "${prod.nombre} ${prod.tipo} ${prod.color} ${prod.talla} ${prod.codigoProducto}".lowercase()
                        terms.all { text.contains(it) }
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val ingresos: StateFlow<List<Ingreso>> = ingresoRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val summary: StateFlow<InventorySummary> = products
        .map(InventoryAnalytics::buildSummary)
        .stateIn(viewModelScope, SharingStarted.Lazily, InventorySummary())

    private val fifteenDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(15)
    private val salesStats = ventaRepo.getProductSalesStats(fifteenDaysAgo, System.currentTimeMillis())

    val abcByProductId: StateFlow<Map<Long, String>> = combine(products, salesStats) { productList, stats ->
        InventoryAnalytics.classifyAbcBySales(productList, stats)
    }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    fun onSearchChange(query: String) {
        _searchQuery.value = query
    }

    fun registrarIngreso(
        productoId: Long,
        nombreProducto: String,
        codigoProducto: String,
        cantidad: String,
        onSuccess: () -> Unit
    ) {
        val cantidadVal = cantidad.toIntOrNull()
        if (cantidadVal == null || cantidadVal <= 0) {
            _uiState.value = _uiState.value.copy(error = "Ingrese una cantidad valida")
            return
        }

        viewModelScope.launch {
            try {
                ingresoRepo.insert(
                    Ingreso(
                        productoId = productoId,
                        nombreProducto = nombreProducto,
                        codigoProducto = codigoProducto,
                        cantidad = cantidadVal
                    )
                )
                productoRepo.incrementStock(productoId, cantidadVal)
                _uiState.value = _uiState.value.copy(success = "Ingreso registrado: +$cantidadVal unidades")
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error: ${e.message}")
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, success = null)
    }

    fun getProductById(id: Long, onResult: (Producto?) -> Unit) {
        viewModelScope.launch {
            onResult(productoRepo.getById(id))
        }
    }

    fun getProductByQrCode(qrCode: String, onResult: (Producto?) -> Unit) {
        viewModelScope.launch {
            val product = productoRepo.getByQrCode(qrCode)
                ?: qrCode.substringAfter("FASHGO-", "").toLongOrNull()?.let { productoRepo.getById(it) }
            onResult(product)
        }
    }
}
