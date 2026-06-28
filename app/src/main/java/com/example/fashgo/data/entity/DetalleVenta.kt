package com.example.fashgo.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "detalle_ventas",
    foreignKeys = [
        ForeignKey(
            entity = Venta::class,
            parentColumns = ["id"],
            childColumns = ["ventaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Producto::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["ventaId"]),
        Index(value = ["productoId"])
    ]
)
data class DetalleVenta(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ventaId: Long,
    val productoId: Long,
    val nombreProducto: String,
    val codigoProducto: String = "",
    val cantidad: Int,
    val precio: Double
)
