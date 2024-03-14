package com.vendetta.miinventario.data.database

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Database
import androidx.room.Room
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import com.vendetta.miinventario.data.database.dao.FinanzasDao
import com.vendetta.miinventario.data.database.dao.ProductosDao
import com.vendetta.miinventario.data.database.dao.UserDao
import com.vendetta.miinventario.data.database.dao.VentasDao
import com.vendetta.miinventario.data.database.entities.FinanzasEntity
import com.vendetta.miinventario.data.database.entities.ProductosEntity
import com.vendetta.miinventario.data.database.entities.UserEntity
import com.vendetta.miinventario.data.database.entities.VentasEntity
import java.time.chrono.HijrahChronology.INSTANCE

@Database(entities = [UserEntity::class,VentasEntity::class,ProductosEntity::class,FinanzasEntity::class], version = 1)
abstract class InventarioDatabase: RoomDatabase() {
abstract val userDao: UserDao
abstract val ventasDao: VentasDao
abstract val productosDao:ProductosDao
abstract val finanzasDao:FinanzasDao

companion object{
    const val DATABASE_NAME = "inventario_database"

    @Volatile
    private var INSTANCE: InventarioDatabase? = null
    fun getDatabase(context: Context): InventarioDatabase {
        val tempInstance = INSTANCE
        if (tempInstance != null) {
            return tempInstance
        }
        synchronized(this) {
            val instance = databaseBuilder(
                context.applicationContext,
                InventarioDatabase::class.java,
                DATABASE_NAME
            ).build()
            INSTANCE = instance
            return instance
        }
    }
}



    //abstract fun getQuoteDao():QuoteDao
}