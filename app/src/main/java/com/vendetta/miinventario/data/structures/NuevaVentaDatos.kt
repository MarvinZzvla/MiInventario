package com.vendetta.miinventario.data.structures

import java.io.Serializable
import java.util.Date


data class NuevaVentaDatos (val id:Int,val name: String,val cantidad:Int,val precio_total:Float,val precio_sell_total:Float, val date: String):Serializable