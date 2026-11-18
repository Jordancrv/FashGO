package com.example.fashgo.data.remote.model

data class Venta(val id: String, val fecha: Long, val total: Double, val canal: String, val estado: String, val cliente: String, val direccion: String, val codigoOrden: String, val estadoContable: String = "confirmada")
data class DetalleVenta(val id: String, val ventaId: String, val productoId: String, val nombreProducto: String, val codigoProducto: String, val cantidad: Int, val precio: Double, val ubicacion: String = "")
