package com.example.safedinneres.repository

import android.util.Log
import com.example.safedinneres.models.Cuenta
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CuentaRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("cuentas")

    // 🔹 Guardar o actualizar una cuenta
    suspend fun guardarCuenta(cuenta: Cuenta): Result<Void?> {
        return try {
            val id = cuenta.id ?: collection.document().id
            Log.d("CuentaRepository", "Guardando cuenta con ID: $id")
            Log.d("CuentaRepository", "Datos: $cuenta")

            collection.document(id).set(cuenta.copy(id = id)).await()

            Log.d("CuentaRepository", "Cuenta guardada exitosamente")
            Result.success(null)
        } catch (e: Exception) {
            Log.e("CuentaRepository", "Error al guardar cuenta", e)
            Result.failure(e)
        }
    }

    // 🔹 Obtener todas las cuentas de un usuario
    suspend fun obtenerCuentasPorUsuario(userId: String): Result<List<Cuenta>> {
        return try {
            Log.d("CuentaRepository", "Obteniendo cuentas para userId: $userId")
            val snapshot = collection.whereEqualTo("userId", userId).get().await()
            val cuentas = snapshot.toObjects(Cuenta::class.java)
            Log.d("CuentaRepository", "Cuentas obtenidas: ${cuentas.size}")
            Result.success(cuentas)
        } catch (e: Exception) {
            Log.e("CuentaRepository", "Error al obtener cuentas", e)
            Result.failure(e)
        }
    }

    // 🔹 Obtener una cuenta específica
    suspend fun obtenerCuentaPorId(id: String): Result<Cuenta?> {
        return try {
            val doc = collection.document(id).get().await()
            val cuenta = doc.toObject(Cuenta::class.java)
            Result.success(cuenta)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Eliminar una cuenta
    suspend fun eliminarCuenta(id: String): Result<Void?> {
        return try {
            Log.d("CuentaRepository", "Eliminando cuenta con ID: $id")
            collection.document(id).delete().await()
            Result.success(null)
        } catch (e: Exception) {
            Log.e("CuentaRepository", "Error al eliminar cuenta", e)
            Result.failure(e)
        }
    }

    // 🔹 Actualizar saldo
    suspend fun actualizarSaldoCuenta(id: String, nuevoSaldo: Double): Result<Void?> {
        return try {
            val saldoSeguro = if (nuevoSaldo < 0) 0.0 else nuevoSaldo
            collection.document(id).update("saldo", saldoSeguro).await()
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Actualizar deuda
    suspend fun actualizarDeudaCuenta(id: String, nuevaDeuda: Double): Result<Void?> {
        return try {
            collection.document(id).update("deudaActual", nuevaDeuda).await()
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pagarDeudaCuenta(id: String, montoPago: Double): Result<Void?> {
        return try {
            val cuentaResult = obtenerCuentaPorId(id)
            if (cuentaResult.isSuccess) {
                val cuenta = cuentaResult.getOrNull()
                cuenta?.let {
                    val nuevaDeuda = (it.deudaActual - montoPago).coerceAtLeast(0.0)
                    collection.document(id).update("deudaActual", nuevaDeuda).await()
                    Result.success(null)
                } ?: Result.failure(Exception("Cuenta no encontrada"))
            } else {
                Result.failure(Exception("Error al obtener cuenta"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}