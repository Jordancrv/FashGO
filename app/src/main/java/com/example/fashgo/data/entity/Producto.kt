package com.example.fashgo.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "productos",
    indices = [
        Index(value = ["codigoProducto"], unique = true),
        Index(value = ["qrCode"], unique = true)
    ]
)
data class Producto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
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
    val qrCode: String = "",
    val stockMinimo: Int = 5,
    val clasificacionAbc: String = "A" // A, B, C
) {
    val semaforoEstado: String
        get() = when {
            stock <= stockMinimo -> "ROJO"
            stock <= stockMinimo * 2 -> "AMARILLO"
            else -> "VERDE"
        }

    val atributosResumen: String
        get() = listOf(diseno, tipoCuello, tipoManga, color, talla)
            .filter { it.isNotBlank() }
            .joinToString(" | ")
}
