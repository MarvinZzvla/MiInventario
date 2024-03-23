package com.vendetta.miinventario.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "Finanzas")
data class FinanzasEntity (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    val id:Int = 0,
    @ColumnInfo(name = "Date")
    val Date:String ="",
    @ColumnInfo(name = "Total")
    val Total:Float =0.0f,
    @ColumnInfo(name = "Total_Ganancias")
    val TotalGanancias:Float =0.0f ,
    @ColumnInfo(name = "FK_Venta")
    val FK_Venta:Int =0
)