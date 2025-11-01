package com.example.safedinneres.data.repository

import com.example.safedinneres.data.models.Presupuesto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PresupuestoRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("presupuestos")


    suspend fun obtenerPresupuesto(userId: String, mes: String): Result<Presupuesto?> {
        return try {
            val snapshot = collection
                .whereEqualTo("userId", userId)
                .whereEqualTo("mes", mes)
                .get()
                .await()
            val presupuesto = snapshot.documents.firstOrNull()?.toObject(Presupuesto::class.java)
            Result.success(presupuesto)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun guardarPresupuesto(presupuesto: Presupuesto): Result<Void?> {
        return try {
            val docId = if (presupuesto.id.isEmpty()) collection.document().id else presupuesto.id
            collection.document(docId).set(presupuesto.copy(id = docId)).await()
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun eliminarPresupuesto(id: String): Result<Void?> {
        return try {
            collection.document(id).delete().await()
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
