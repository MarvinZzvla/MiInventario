package com.vendetta.miinventario.data

data class Productos(
    val id: Int,
    val productoName: String,
    val cantidad: Int,
    val precio_costo: Float,
    val precio_venta: Float,
    val barCode: String
)