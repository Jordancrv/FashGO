package com.example.fashgo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fashgo.data.dao.IngresoDao
import com.example.fashgo.data.dao.MateriaPrimaDao
import com.example.fashgo.data.dao.ProductoDerivadoDao
import com.example.fashgo.data.dao.ProductoDao
import com.example.fashgo.data.dao.UsuarioDao
import com.example.fashgo.data.dao.VentaDao
import com.example.fashgo.data.entity.DetalleVenta
import com.example.fashgo.data.entity.Ingreso
import com.example.fashgo.data.entity.MateriaPrima
import com.example.fashgo.data.entity.Producto
import com.example.fashgo.data.entity.ProductoDerivado
import com.example.fashgo.data.entity.Usuario
import com.example.fashgo.data.entity.Venta
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Producto::class, Venta::class, DetalleVenta::class, Ingreso::class, Usuario::class, MateriaPrima::class, ProductoDerivado::class],
    version = 11,
    exportSchema = false
)
abstract class FashGoDatabase : RoomDatabase() {
    abstract fun productoDao(): ProductoDao
    abstract fun ventaDao(): VentaDao
    abstract fun ingresoDao(): IngresoDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun materiaPrimaDao(): MateriaPrimaDao
    abstract fun productoDerivadoDao(): ProductoDerivadoDao

    companion object {
        @Volatile
        private var INSTANCE: FashGoDatabase? = null

        fun getDatabase(context: Context): FashGoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FashGoDatabase::class.java,
                    "fashgo_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                CoroutineScope(Dispatchers.IO).launch {
                    seedDatabase(instance, context.applicationContext)
                }
                instance
            }
        }

        private suspend fun seedDatabase(database: FashGoDatabase, context: Context) {
            if (database.usuarioDao().getCount() == 0) {
                database.usuarioDao().insert(
                    Usuario(
                        nombre = "Liz",
                        password = "12345",
                        rol = "DUENO"
                    )
                )
                database.usuarioDao().insert(
                    Usuario(
                        nombre = "Vendedora",
                        password = "1234",
                        rol = "VENDEDOR"
                    )
                )
            }

            if (database.productoDao().getCount() == 0) {
                database.productoDao().insertAll(SeedDataLoader.loadProducts())
            }

            if (database.materiaPrimaDao().getCount() == 0) {
                database.materiaPrimaDao().insertAll(SeedDataLoader.loadMateriaPrima())
            }

            if (database.productoDerivadoDao().getCount() == 0) {
                database.productoDerivadoDao().insertAll(SeedDataLoader.loadProductosDerivados())
            }
        }
    }
}
