package com.vendetta.miinventario.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vendetta.miinventario.data.database.entities.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM Usuarios")
    suspend fun getAllUser():List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(user:UserEntity)
    @Update
    suspend fun update(user: UserEntity)
    @Delete
    suspend fun delete(user: UserEntity)

    @Query("DELETE FROM Usuarios")
    suspend fun deleteAll()
}