package com.example.fashgo.data.dao

import androidx.room.*
import com.example.fashgo.data.entity.Usuario

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios WHERE nombre = :nombre AND password = :password LIMIT 1")
    suspend fun login(nombre: String, password: String): Usuario?

    @Query("SELECT * FROM usuarios ORDER BY nombre ASC")
    suspend fun getAll(): List<Usuario>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usuario: Usuario): Long

    @Update
    suspend fun update(usuario: Usuario)

    @Delete
    suspend fun delete(usuario: Usuario)

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun getCount(): Int
}
