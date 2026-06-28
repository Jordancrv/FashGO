package com.example.fashgo.ui.screens.sales

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fashgo.data.database.FashGoDatabase
import com.example.fashgo.data.entity.DetalleVenta
import com.example.fashgo.data.entity.Producto
import com.example.fashgo.data.entity.Venta
import com.example.fashgo.data.repository.ProductoRepository
import com.example.fashgo.data.repository.VentaRepository
import com.example.fashgo.util.InventoryAnalytics
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class CartItem(
    val producto: Producto,
    val cantidad: Int = 1,
    val precioUnitario: Double = producto.precio
) {
    val subtotal: Double get() = cantidad * precioUnitario
}

data class SaleUiState(
    val cart: List<CartItem> = emptyList(),
    val total: Double = 0.0,
    val montoRecibido: String = "",
    val cambio: Double = 0.0,
    val canal: String = "Ventas Presenciales",
    val cliente: String = "",
    val direccion: String = "",
    val error: String? = null,
    val saleCompleted: Boolean = false,
    val isProcessing: Boolean = false,
    val ventaDetalles: List<DetalleVenta> = emptyList()
)

class SaleViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FashGoDatabase.getDatabase(application)
    private val productoRepo = ProductoRepository(db.productoDao())
    private val ventaRepo = VentaRepository(db.ventaDao())

    private val _uiState = MutableStateFlow(SaleUiState())
    val uiState: StateFlow<SaleUiState> = _uiState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<Producto>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
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

    private val salesStats = ventaRepo.getProductSalesStats(
        System.currentTimeMillis() - TimeUnit.DAYS.toMillis(15),
        System.currentTimeMillis()
    )

    val abcByProductId: StateFlow<Map<Long, String>> = combine(
        productoRepo.getAll(),
        salesStats
    ) { products, stats ->
        InventoryAnalytics.classifyAbcBySales(products, stats)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

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

    val ventasDelDia: StateFlow<List<Venta>> = ventaRepo.getVentasDelDia(getStartOfDay(), getEndOfDay())
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allVentas: StateFlow<List<Venta>> = ventaRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun onSearchChange(query: String) {
        _searchQuery.value = query
    }

    fun addToCart(producto: Producto) {
        val currentCart = _uiState.value.cart
        val existing = currentCart.find { it.producto.id == producto.id }
        if (existing != null) {
            if (existing.cantidad < producto.stock) {
                updateCart(
                    currentCart.map { item ->
                        if (item.producto.id == producto.id) item.copy(cantidad = item.cantidad + 1) else item
                    }
                )
            } else {
                _uiState.value = _uiState.value.copy(error = "Stock insuficiente para ${producto.nombre}")
                return
            }
        } else {
            if (producto.stock <= 0) {
                _uiState.value = _uiState.value.copy(error = "Producto sin stock")
                return
            }
            updateCart(currentCart + CartItem(producto = producto))
        }
        _searchQuery.value = ""
    }

    fun addToCartByQrCode(qrCode: String) {
        viewModelScope.launch {
            val producto = productoRepo.getByQrCode(qrCode)
                ?: qrCode.substringAfter("FASHGO-", "").toLongOrNull()?.let { productoRepo.getById(it) }
            if (producto != null) {
                addToCart(producto)
            } else {
                _uiState.value = _uiState.value.copy(error = "No se encontro el producto escaneado")
            }
        }
    }

    fun updateQuantity(productId: Long, newQuantity: Int) {
        val currentCart = _uiState.value.cart
        val item = currentCart.find { it.producto.id == productId } ?: return
        when {
            newQuantity <= 0 -> updateCart(currentCart.filterNot { it.producto.id == productId })
            newQuantity <= item.producto.stock -> {
                updateCart(
                    currentCart.map { cartItem ->
                        if (cartItem.producto.id == productId) cartItem.copy(cantidad = newQuantity) else cartItem
                    }
                )
            }
            else -> {
                _uiState.value = _uiState.value.copy(error = "Stock maximo: ${item.producto.stock}")
            }
        }
    }

    fun removeFromCart(productId: Long) {
        updateCart(_uiState.value.cart.filterNot { it.producto.id == productId })
    }

    fun onMontoRecibidoChange(monto: String) {
        val montoVal = monto.toDoubleOrNull() ?: 0.0
        val cambio = montoVal - _uiState.value.total
        _uiState.value = _uiState.value.copy(
            montoRecibido = monto,
            cambio = if (cambio >= 0) cambio else 0.0
        )
    }

    fun onCanalChange(canal: String) {
        _uiState.value = _uiState.value.copy(canal = canal)
    }

    fun onClienteChange(cliente: String) {
        _uiState.value = _uiState.value.copy(cliente = cliente)
    }

    fun onDireccionChange(direccion: String) {
        _uiState.value = _uiState.value.copy(direccion = direccion)
    }

    private fun updateCart(cart: List<CartItem>) {
        val total = cart.sumOf { it.subtotal }
        val montoVal = _uiState.value.montoRecibido.toDoubleOrNull() ?: 0.0
        _uiState.value = _uiState.value.copy(
            cart = cart,
            total = total,
            cambio = if (montoVal >= total) montoVal - total else 0.0,
            error = null
        )
    }

    fun completeSale(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.cart.isEmpty()) {
            _uiState.value = state.copy(error = "El carrito esta vacio")
            return
        }
        if (state.canal == "Ventas Presenciales") {
            val montoRecibido = state.montoRecibido.toDoubleOrNull() ?: 0.0
            if (montoRecibido < state.total) {
                _uiState.value = state.copy(error = "El monto recibido es insuficiente")
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isProcessing = true, error = null)
            try {
                state.cart.forEach { item ->
                    val latestProduct = productoRepo.getById(item.producto.id)
                        ?: throw IllegalStateException("Producto no encontrado: ${item.producto.nombre}")
                    if (latestProduct.stock < item.cantidad) {
                        throw IllegalStateException("Stock insuficiente para ${item.producto.nombre}. Disponible: ${latestProduct.stock}")
                    }
                }

                val codigoOrden = if (state.canal != "Ventas Presenciales") "OC #${(100000..999999).random()}" else ""
                val venta = Venta(
                    total = state.total,
                    canal = state.canal,
                    cliente = state.cliente.ifBlank { "Cliente General" },
                    direccion = state.direccion,
                    codigoOrden = codigoOrden,
                    estado = if (state.canal == "Ventas Presenciales") "Enviado" else "Por enviar"
                )
                val detalles = state.cart.map { item ->
                    DetalleVenta(
                        ventaId = 0,
                        productoId = item.producto.id,
                        nombreProducto = item.producto.nombre,
                        codigoProducto = item.producto.codigoProducto,
                        cantidad = item.cantidad,
                        precio = item.precioUnitario
                    )
                }
                ventaRepo.insertVentaConDetalles(venta, detalles)

                state.cart.forEach { item ->
                    val rows = productoRepo.decrementStock(item.producto.id, item.cantidad)
                    if (rows == 0) {
                        throw IllegalStateException("No se pudo descontar stock de ${item.producto.nombre}")
                    }
                }

                _uiState.value = SaleUiState(saleCompleted = true)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isProcessing = false, error = "Error: ${e.message}")
            }
        }
    }

    fun resetSale() {
        _uiState.value = SaleUiState()
        _searchQuery.value = ""
    }

    fun loadSaleDetails(ventaId: Long) {
        viewModelScope.launch {
            val detalles = ventaRepo.getDetallesByVentaId(ventaId)
            _uiState.value = _uiState.value.copy(ventaDetalles = detalles)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
