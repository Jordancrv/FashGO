package com.example.fashgo.ui.screens.products

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fashgo.data.database.FashGoDatabase
import com.example.fashgo.data.entity.Producto
import com.example.fashgo.data.repository.ProductoRepository
import com.example.fashgo.data.repository.VentaRepository
import com.example.fashgo.util.InventoryAnalytics
import com.example.fashgo.util.ProductCodeGenerator
import com.example.fashgo.util.ProductDraft
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
import kotlin.math.ceil
import kotlin.math.max

data class ProductUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null,
    val selectedProduct: Producto? = null,
    val suggestedCode: String = ""
)

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FashGoDatabase.getDatabase(application)
    private val repository = ProductoRepository(db.productoDao())
    private val ventaRepository = VentaRepository(db.ventaDao())

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    @OptIn(ExperimentalCoroutinesApi::class)
    val products: StateFlow<List<Producto>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.getAll()
            else {
                repository.getAll().map { list ->
                    val terms = query.lowercase().split(" ").filter { it.isNotBlank() }
                    list.filter { prod ->
                        val text = "${prod.nombre} ${prod.tipo} ${prod.color} ${prod.talla} ${prod.codigoProducto}".lowercase()
                        terms.all { text.contains(it) }
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val lowStockProducts: StateFlow<List<Producto>> = repository.getLowStock()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val fifteenDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(15)
    private val salesStats = ventaRepository.getProductSalesStats(fifteenDaysAgo, System.currentTimeMillis())

    val abcByProductId: StateFlow<Map<Long, String>> = combine(products, salesStats) { productList, stats ->
        InventoryAnalytics.classifyAbcBySales(productList, stats)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun loadProduct(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val product = repository.getById(id)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                selectedProduct = product,
                suggestedCode = product?.codigoProducto.orEmpty()
            )
        }
    }

    fun updateSuggestedCode(
        parteGeneral: String,
        tipo: String,
        diseno: String,
        tipoManga: String,
        tipoCuello: String,
        color: String,
        talla: String
    ) {
        val draft = ProductDraft(
            parteGeneral = parteGeneral,
            tipo = tipo,
            diseno = diseno,
            tipoManga = tipoManga,
            tipoCuello = tipoCuello,
            color = color,
            talla = talla
        )
        if (draft.parteGeneral.isBlank() || draft.tipo.isBlank() || draft.color.isBlank() || draft.talla.isBlank()) {
            _uiState.value = _uiState.value.copy(suggestedCode = "")
            return
        }

        viewModelScope.launch {
            val prefix = ProductCodeGenerator.buildPrefix(draft)
            val codes = repository.getCodesByPrefix(prefix)
            val code = ProductCodeGenerator.buildCode(draft, codes)
            _uiState.value = _uiState.value.copy(suggestedCode = code)
        }
    }

    fun saveProduct(
        id: Long?,
        parteGeneral: String,
        tipo: String,
        diseno: String,
        tipoManga: String,
        tipoCuello: String,
        color: String,
        talla: String,
        precio: String,
        stock: String,
        ubicacion: String,
        stockMinimo: String,
        onSuccess: () -> Unit
    ) {
        if (parteGeneral.isBlank() || tipo.isBlank() || color.isBlank() || talla.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Completa parte general, tipo, color y talla")
            return
        }

        val precioVal = precio.toDoubleOrNull()
        if (precioVal == null || precioVal <= 0) {
            _uiState.value = _uiState.value.copy(error = "El precio debe ser un numero positivo")
            return
        }

        val stockVal = stock.toIntOrNull()
        if (stockVal == null || stockVal < 0) {
            _uiState.value = _uiState.value.copy(error = "El stock debe ser un numero valido")
            return
        }

        val draft = ProductDraft(
            parteGeneral = parteGeneral,
            tipo = tipo,
            diseno = diseno,
            tipoManga = tipoManga,
            tipoCuello = tipoCuello,
            color = color,
            talla = talla
        )
        val nombre = ProductCodeGenerator.buildName(draft)
        val clasificacionActual = if (id != null && id > 0) "A" /* placeholder, will resolve in coroutine */ else "A"

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val existing = if (id != null && id > 0) repository.getById(id) else null
                val prefix = ProductCodeGenerator.buildPrefix(draft)
                val siblingCodes = repository.getCodesByPrefix(prefix).filter { it != existing?.codigoProducto }
                val generatedCode = when {
                    existing != null && existing.codigoProducto.startsWith("$prefix-") -> existing.codigoProducto
                    else -> ProductCodeGenerator.buildCode(draft, siblingCodes)
                }

                val product = Producto(
                    id = existing?.id ?: 0,
                    nombre = nombre,
                    parteGeneral = parteGeneral,
                    tipo = tipo,
                    diseno = diseno,
                    tipoManga = tipoManga,
                    tipoCuello = tipoCuello,
                    color = color,
                    talla = talla,
                    precio = precioVal,
                    stock = stockVal,
                    stockReferencia = existing?.stockReferencia ?: stockVal,
                    ubicacion = ubicacion.ifBlank { defaultLocation(parteGeneral) },
                    codigoProducto = generatedCode,
                    qrCode = generatedCode,
                    clasificacionAbc = existing?.clasificacionAbc ?: "A",
                    stockMinimo = stockMinimo.toIntOrNull() ?: if ((existing?.clasificacionAbc ?: "A") == "A") 50 else max(3, ceil(stockVal * 0.25).toInt())
                )

                if (existing != null) {
                    repository.update(
                        product.copy(
                            stockReferencia = max(existing.stockReferencia, stockVal)
                        )
                    )
                } else {
                    repository.insert(product)
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    success = "Producto guardado",
                    suggestedCode = generatedCode
                )
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Error: ${e.message}")
            }
        }
    }

    fun deleteProduct(producto: Producto, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.delete(producto)
            _uiState.value = _uiState.value.copy(selectedProduct = null)
            onSuccess()
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, success = null)
    }

    private fun defaultLocation(parteGeneral: String): String {
        return when (parteGeneral) {
            "Parte Superior" -> "Zona Superior"
            "Parte Inferior" -> "Zona Inferior"
            "Enterizo" -> "Zona Enterizos"
            else -> "Zona General"
        }
    }
}
