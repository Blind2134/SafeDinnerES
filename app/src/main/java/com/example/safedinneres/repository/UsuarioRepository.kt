package com.example.safedinneres.repository

import com.example.safedinneres.models.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UsuarioRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()


    suspend fun registrarUsuario(nombre: String, email: String, password: String): Result<Usuario> {
        return try {

            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("Error al obtener UID"))


            val usuario = Usuario(
                id = uid,
                nombre = nombre,
                email = email
            )


            db.collection("usuarios").document(uid).set(usuario).await()

            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun iniciarSesion(email: String, password: String): Result<Usuario> {
        return try {

            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("Error al obtener UID"))


            val snapshot = db.collection("usuarios").document(uid).get().await()
            val usuario = snapshot.toObject(Usuario::class.java)
                ?: return Result.failure(Exception("Usuario no encontrado"))

            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerUsuarioPorId(uid: String): Result<Usuario> {
        return try {
            val snapshot = db.collection("usuarios").document(uid).get().await()
            val usuario = snapshot.toObject(Usuario::class.java)

            if (usuario != null) {
                Result.success(usuario)
            } else {
                Result.failure(Exception("Usuario no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}