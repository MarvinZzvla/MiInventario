package com.vendetta.miinventario.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "Productos")
data class ProductosEntity (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("ID")
    val ID:Int = 0,
    @ColumnInfo(name = "Name")
    val Name:String = "",
    @ColumnInfo(name = "Available")
    val Available:Int = 0,
    @ColumnInfo(name = "Price")
    val Price: Float = 0.0f,
    @ColumnInfo(name = "Price_Sell")
    val Price_Sell :Float = 0.0f,
    @ColumnInfo(name = "BarCode")
    val BarCode: String = ""

)