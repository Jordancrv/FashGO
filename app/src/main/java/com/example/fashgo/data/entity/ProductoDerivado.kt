package com.example.fashgo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos_derivados")
data class ProductoDerivado(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
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
