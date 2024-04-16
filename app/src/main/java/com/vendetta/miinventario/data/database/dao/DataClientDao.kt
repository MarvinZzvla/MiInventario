package com.vendetta.miinventario.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.vendetta.miinventario.data.database.entities.DataClientEntity

@Dao
interface DataClientDao {
    @Insert
    suspend fun insert(dataclient: DataClientEntity)
    @Update
    suspend fun update(dataclient: DataClientEntity)
    @Delete
    suspend fun delete(dataclient: DataClientEntity)
    @Query(value = "SELECT * FROM DataClient")
    suspend fun getInfo(): List<DataClientEntity>
}