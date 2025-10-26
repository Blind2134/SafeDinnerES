package com.example.safedinneres.repository

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
            collection.document(id).set(cuenta.copy(id = id)).await()
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Obtener todas las cuentas de un usuario
    suspend fun obtenerCuentasPorUsuario(userId: String): Result<List<Cuenta>> {
        return try {
            val snapshot = collection.whereEqualTo("userId", userId).get().await()
            val cuentas = snapshot.toObjects(Cuenta::class.java)
            Result.success(cuentas)
        } catch (e: Exception) {
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
            collection.document(id).delete().await()
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Actualizar saldo (por ejemplo, cuando haces un gasto)
    suspend fun actualizarSaldoCuenta(id: String, nuevoSaldo: Double): Result<Void?> {
        return try {
            val saldoSeguro = if (nuevoSaldo < 0) 0.0 else nuevoSaldo
            collection.document(id).update("saldo", saldoSeguro).await()
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Actualizar deuda (para cuentas de crédito)
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