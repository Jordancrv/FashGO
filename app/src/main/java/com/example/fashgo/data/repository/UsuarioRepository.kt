package com.example.fashgo.data.repository

import com.example.fashgo.data.dao.UsuarioDao
import com.example.fashgo.data.entity.Usuario

class UsuarioRepository(private val usuarioDao: UsuarioDao) {
    suspend fun login(nombre: String, password: String): Usuario? =
        usuarioDao.login(nombre, password)

    suspend fun getAll(): List<Usuario> = usuarioDao.getAll()
    suspend fun insert(usuario: Usuario): Long = usuarioDao.insert(usuario)
    suspend fun update(usuario: Usuario) = usuarioDao.update(usuario)
    suspend fun delete(usuario: Usuario) = usuarioDao.delete(usuario)
    suspend fun getCount(): Int = usuarioDao.getCount()
}
