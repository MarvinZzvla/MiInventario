package com.vendetta.miinventario.data

import java.util.ArrayList
import java.util.Date

data class Ventas (val fecha: Date,
                   var factura : Int, val objetos : String, val totalVenta : Float, val totalGanancia:Float)