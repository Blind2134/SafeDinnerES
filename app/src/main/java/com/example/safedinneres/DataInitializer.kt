package com.example.safedinneres

import com.example.safedinneres.models.Categoria
import com.example.safedinneres.models.Cuenta
import com.example.safedinneres.models.MetodoPago
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DataInitializer {

    private val db = FirebaseFirestore.getInstance()

    // 🔹 Categorías predeterminadas
    suspend fun crearCategoriasIniciales(userId: String) {
        val categorias = listOf(
            Categoria(nombre = "Comida", icono = "🍔", color = "#FF9800", userId = userId),
            Categoria(nombre = "Transporte", icono = "🚌", color = "#03A9F4", userId = userId),
            Categoria(nombre = "Educación", icono = "📚", color = "#8BC34A", userId = userId),
            Categoria(nombre = "Entretenimiento", icono = "🎮", color = "#E91E63", userId = userId),
            Categoria(nombre = "Otros", icono = "💼", color = "#9E9E9E", userId = userId)
        )
        categorias.forEach {
            val docRef = db.collection("categorias").document()
            docRef.set(it.copy(id = docRef.id)).await()
        }
    }

    // 🔹 Cuentas predeterminadas
    suspend fun crearCuentasIniciales(userId: String) {
        val cuentas = listOf(
            Cuenta(nombre = "Efectivo", saldo = 0.0, userId = userId),
            Cuenta(nombre = "Banco", saldo = 0.0, userId = userId)
        )
        cuentas.forEach {
            val docRef = db.collection("cuentas").document()
            docRef.set(it.copy(id = docRef.id)).await()
        }
    }

    // 🔹 Métodos de pago predeterminados
    suspend fun crearMetodosPagoIniciales(userId: String) {
        val metodos = listOf(
            MetodoPago(nombre = "Efectivo", userId = userId),
            MetodoPago(nombre = "Tarjeta de Crédito", userId = userId),
            MetodoPago(nombre = "Tarjeta de Débito", userId = userId)
        )
        metodos.forEach {
            val docRef = db.collection("metodos_pago").document()
            docRef.set(it.copy(id = docRef.id)).await()
        }
    }
}
