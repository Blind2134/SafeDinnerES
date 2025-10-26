package com.example.safedinneres.models

data class Cuenta(
    val id: String? = null,
    val nombre: String = "",      // Ej: "BCP", "BBVA", "Interbank"
    val tipo: String = "",        // Ej: "Banco", "Billetera", "Efectivo"
    val saldo: Double = 0.0,
    val moneda: String = "PEN",
    val userId: String = "",
    val fechaCreacion: Long = System.currentTimeMillis()
)