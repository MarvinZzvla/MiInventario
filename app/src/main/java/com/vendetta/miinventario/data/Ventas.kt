package com.vendetta.miinventario.data

import java.util.ArrayList
import java.util.Date

data class Ventas (val fecha: Date,val factura : Int, val objetos : ArrayList<String>, val totalVenta : Float, val totalGanancia:Float)