package com.example.fashgo.data.remote.model

data class Existencia(val ubicacionId: String, val codigo: String, val cantidad: Int)

data class Producto(
    val id: String,
    val nombre: String,
    val parteGeneral: String = "",
    val tipo: String = "",
    val diseno: String = "",
    val tipoManga: String = "",
    val tipoCuello: String = "",
    val color: String = "",
    val talla: String = "",
    val precio: Double,
    val stock: Int,
    val stockReferencia: Int = stock,
    val ubicacion: String = "",
    val codigoProducto: String = "",
    val qrCode: String = codigoProducto,
    val stockMinimo: Int = 5,
    val clasificacionAbc: String = "C",
    val existencias: List<Existencia> = emptyList()
) {
    val semaforoEstado get() = when { stock <= stockMinimo -> "ROJO"; stock <= stockMinimo * 2 -> "AMARILLO"; else -> "VERDE" }
    val atributosResumen get() = listOf(diseno, tipoCuello, tipoManga, color, talla).filter { it.isNotBlank() }.joinToString(" | ")
}
