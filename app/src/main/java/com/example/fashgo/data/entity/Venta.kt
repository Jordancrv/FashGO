package com.example.fashgo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ventas")
data class Venta(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fecha: Long = System.currentTimeMillis(),
    val total: Double = 0.0,
    val canal: String = "Ventas Presenciales",
    val estado: String = "Enviado", // Enviado, Por enviar
    val cliente: String = "Cliente General",
    val direccion: String = "Tienda",
    val codigoOrden: String = ""
)
