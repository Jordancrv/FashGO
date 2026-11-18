package com.example.fashgo.data.remote.model


data class MateriaPrima(    val id: String = "",
    val nombre: String,
    val descripcion: String = "",
    val cantidad: Double,
    val unidadMedida: String = "kg",
    val proveedor: String = ""
)
