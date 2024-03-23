package com.vendetta.miinventario.data

import java.io.Serializable

data class Productos(
    val id: Int,
    val productoName: String,
    val cantidad: Int,
    val precio_costo: Float,
    val precio_venta: Float,
    val barCode: String
):Serializable