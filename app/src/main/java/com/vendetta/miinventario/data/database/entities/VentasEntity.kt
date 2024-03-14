package com.vendetta.miinventario.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "Ventas")
data class VentasEntity (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    val id: Int = 0,
    @ColumnInfo(name = "FK_Producto")
    val FK_Producto: String = "",
    @ColumnInfo(name = "Date")
    val Date : String = Date().toString(),
    @ColumnInfo(name = "Quantity")
    val Quantity : Int = 0 ,
    @ColumnInfo(name = "Price")
    val Price : Float = 0.0f,
    @ColumnInfo(name = "Profit")
    val Profit : Float = 0.0f,
    @ColumnInfo(name = "ID_Factura")
    val ID_Factura : Int = 0


)