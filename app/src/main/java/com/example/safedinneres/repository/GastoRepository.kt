package com.example.safedinneres.repository

import com.example.safedinneres.models.Gasto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class GastoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("gastos")

    // 🔹 Guardar o actualizar un gasto
    suspend fun guardarGasto(gasto: Gasto): Result<Void?> {
        return try {
            val id = gasto.id ?: collection.document().id
            collection.document(id).set(gasto.copy(id = id)).await()
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Obtener un gasto por su ID
    suspend fun obtenerGastoPorId(id: String): Result<Gasto?> {
        return try {
            val snapshot = collection.document(id).get().await()
            val gasto = snapshot.toObject(Gasto::class.java)
            Result.success(gasto)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Obtener todos los gastos del usuario
    suspend fun obtenerGastosPorUsuario(userId: String): Result<List<Gasto>> {
        return try {
            val snapshot = collection
                .whereEqualTo("userId", userId)
                .orderBy("fecha")
                .get()
                .await()
            val gastos = snapshot.toObjects(Gasto::class.java)
            Result.success(gastos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Obtener gastos por categoría
    suspend fun obtenerGastosPorCategoria(userId: String, categoriaId: String): Result<List<Gasto>> {
        return try {
            val snapshot = collection
                .whereEqualTo("userId", userId)
                .whereEqualTo("categoriaId", categoriaId)
                .get()
                .await()
            val gastos = snapshot.toObjects(Gasto::class.java)
            Result.success(gastos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Obtener gastos por cuenta
    suspend fun obtenerGastosPorCuenta(userId: String, cuentaId: String): Result<List<Gasto>> {
        return try {
            val snapshot = collection
                .whereEqualTo("userId", userId)
                .whereEqualTo("cuentaId", cuentaId)
                .get()
                .await()
            val gastos = snapshot.toObjects(Gasto::class.java)
            Result.success(gastos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Obtener gastos por método de pago
    suspend fun obtenerGastosPorMetodoPago(userId: String, metodoPagoId: String): Result<List<Gasto>> {
        return try {
            val snapshot = collection
                .whereEqualTo("userId", userId)
                .whereEqualTo("metodoPagoId", metodoPagoId)
                .get()
                .await()
            val gastos = snapshot.toObjects(Gasto::class.java)
            Result.success(gastos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Eliminar gasto
    suspend fun eliminarGasto(id: String): Result<Void?> {
        return try {
            collection.document(id).delete().await()
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Listar gastos por mes (para MainActivity)
    suspend fun listarGastos(mesTexto: String): Result<List<Gasto>> {
        return try {
            val prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(
                com.google.firebase.FirebaseApp.getInstance().applicationContext
            )
            val userId = prefs.getString("uid_usuario", null) ?: return Result.success(emptyList())

            val formato = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
            val mesActual = formato.parse(mesTexto)

            val calendario = Calendar.getInstance()
            calendario.time = mesActual ?: Date()
            calendario.set(Calendar.DAY_OF_MONTH, 1)
            val inicioMes = calendario.timeInMillis

            calendario.add(Calendar.MONTH, 1)
            val finMes = calendario.timeInMillis

            val snapshot = collection
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("fecha", inicioMes)
                .whereLessThan("fecha", finMes)
                .orderBy("fecha")
                .get()
                .await()

            val gastos = snapshot.toObjects(Gasto::class.java)
            Result.success(gastos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
