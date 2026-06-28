package com.example.fashgo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "materia_prima")
data class MateriaPrima(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val descripcion: String = "",
    val cantidad: Double,
    val unidadMedida: String = "kg",
    val proveedor: String = ""
)
