package com.example.fashgo.data.remote.model


data class ProductoDerivado(    val id: String = "",
    val tipoPrenda: String,
    val subcategoria: String,
    val color: String,
    val talla: String = "Estandar",
    val cantidad: Int,
    val precioUnitario: Double,
    val rotacion: String = "Baja",
    val decisionSugerida: String = "Derivado",
    val ubicacion: String = "Zona Auxiliar",
    val nivel: String = "",
    val zonaAuxiliar: Int? = null,
    val origen: String = "",
    val notas: String = "",
    val fechaRegistro: Long = System.currentTimeMillis()
) {
    val nombreCompleto: String
        get() = listOf(tipoPrenda, subcategoria, color, talla)
            .filter { it.isNotBlank() }
            .joinToString(" - ")

    val precioTotal: Double
        get() = cantidad * precioUnitario
}
