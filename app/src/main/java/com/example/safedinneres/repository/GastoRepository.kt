package com.example.safedinneres.repository

import com.example.safedinneres.models.Gasto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GastoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser


    suspend fun agregarGasto(gasto: Gasto): Result<Void?> {
        return try {
            val id = db.collection("gastos").document().id
            val nuevo = gasto.copy(id = id, userId = user?.uid ?: "")
            db.collection("gastos").document(id).set(nuevo).await()
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun listarGastos(mes: String): Result<List<Gasto>> {
        return try {
            val query = db.collection("gastos")
                .whereEqualTo("userId", user?.uid)
                .whereEqualTo("mes", mes)
                .get()
                .await()

            val lista = query.documents.mapNotNull { it.toObject(Gasto::class.java) }
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun actualizarGasto(gasto: Gasto): Result<Void?> {
        return try {
            if (gasto.id == null) throw Exception("ID del gasto no puede ser null")
            db.collection("gastos").document(gasto.id).set(gasto).await()
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun eliminarGasto(id: String): Result<Void?> {
        return try {
            db.collection("gastos").document(id).delete().await()
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerGastoPorId(id: String): Result<Gasto> {
        return try {
            val doc = db.collection("gastos").document(id).get().await()
            val gasto = doc.toObject(Gasto::class.java)
            if (gasto != null) Result.success(gasto) else Result.failure(Exception("Gasto no encontrado"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
