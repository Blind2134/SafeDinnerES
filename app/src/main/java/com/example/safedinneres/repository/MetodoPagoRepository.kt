package com.example.safedinneres.repository

import com.example.safedinneres.models.MetodoPago
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MetodoPagoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("metodos_pago")

    // 🔹 Guardar o actualizar un método de pago
    suspend fun guardarMetodoPago(metodo: MetodoPago): Result<Void?> {
        return try {
            val id = metodo.id ?: collection.document().id
            collection.document(id).set(metodo.copy(id = id)).await()
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Obtener todos los métodos de pago de un usuario
    suspend fun obtenerMetodosPorUsuario(userId: String): Result<List<MetodoPago>> {
        return try {
            val snapshot = collection.whereEqualTo("userId", userId).get().await()
            val metodos = snapshot.toObjects(MetodoPago::class.java)
            Result.success(metodos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Obtener un método específico
    suspend fun obtenerMetodoPorId(id: String): Result<MetodoPago?> {
        return try {
            val doc = collection.document(id).get().await()
            val metodo = doc.toObject(MetodoPago::class.java)
            Result.success(metodo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Eliminar un método de pago
    suspend fun eliminarMetodoPago(id: String): Result<Void?> {
        return try {
            collection.document(id).delete().await()
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
