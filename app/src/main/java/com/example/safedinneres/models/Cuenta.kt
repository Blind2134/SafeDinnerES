package com.example.safedinneres.models

data class Cuenta(
    val id: String? = null,
    val nombre: String = "",            // Ej: "BCP", "BBVA", "Yape"
    val tipo: String = "DEBITO",        // "DEBITO" o "CREDITO"
    val saldo: Double = 0.0,            // Disponible (para débito)
    val deudaActual: Double = 0.0,      // Solo aplica si es crédito
    val limiteCredito: Double = 0.0,    // Solo aplica si es crédito
    val moneda: String = "PEN",
    val userId: String = "",
    val fechaCreacion: Long = System.currentTimeMillis()
)