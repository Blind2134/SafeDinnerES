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

            result.user?.sendEmailVerification()?.await()


            val usuario = Usuario(
                id = uid,
                nombre = nombre,
                email = email
            )


            db.collection("usuarios").document(uid).set(usuario).await()

            auth.signOut()

            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun iniciarSesion(email: String, password: String): Result<Usuario> {
        return try {
            // Iniciar sesión
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("Error al obtener UID"))


            val user = result.user
            if (user?.isEmailVerified == false) {
                auth.signOut()
                return Result.failure(Exception("Por favor verifica tu correo electrónico antes de iniciar sesión"))
            }


            val snapshot = db.collection("usuarios").document(uid).get().await()
            val usuario = snapshot.toObject(Usuario::class.java)
                ?: return Result.failure(Exception("Usuario no encontrado"))

            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reenviarEmailVerificacion(): Result<Unit> {
        return try {
            val user = auth.currentUser
            if (user != null) {
                user.sendEmailVerification().await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("No hay usuario autenticado"))
            }
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

    suspend fun enviarEmailRecuperacion(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}