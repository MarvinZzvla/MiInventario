package com.vendetta.miinventario.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "DataClient")
data class DataClientEntity (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    val id:Int = 0,
    @ColumnInfo(name = "Date_Compra")
    val Date:String ="02/01/2024",
    @ColumnInfo(name = "Date_Expired")
    val Expired:String ="01/01/2024",
    @ColumnInfo(name = "isActive")
    val isActive:Boolean = false
)