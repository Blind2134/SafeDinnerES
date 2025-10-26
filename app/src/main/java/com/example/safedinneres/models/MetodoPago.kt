package com.example.safedinneres.models

data class MetodoPago(
    val id: String? = null,
    val nombre: String = "",        // Ej: "Tarjeta BCP", "Yape", "Tarjeta BBVA"
    val tipo: String = "",          // Ej: "Tarjeta", "Efectivo", "Transferencia"
    val subtipo: String? = null,    // Ej: "Débito", "Crédito" (solo si aplica)
    val cuentaId: String? = null,   // 🔗 A qué cuenta pertenece (BCP, BBVA, etc.)
    val limiteCredito: Double? = null, // Solo si es tarjeta de crédito
    val deudaActual: Double? = null,   // También para crédito
    val userId: String = ""
)