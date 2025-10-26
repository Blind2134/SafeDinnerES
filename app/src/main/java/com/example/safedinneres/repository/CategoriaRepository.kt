package com.example.safedinneres.repository

import com.example.safedinneres.models.Categoria
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CategoriaRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("categorias")

    suspend fun guardarCategoria(categoria: Categoria): Result<Void?> {
        return try {
            val id = categoria.id ?: collection.document().id
            collection.document(id).set(categoria.copy(id = id)).await()
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerCategoriasPorUsuario(userId: String): Result<List<Categoria>> {
        return try {
            val snapshot = collection.whereEqualTo("userId", userId).get().await()
            val categorias = snapshot.toObjects(Categoria::class.java)
            Result.success(categorias)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarCategoria(id: String): Result<Void?> {
        return try {
            collection.document(id).delete().await()
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
