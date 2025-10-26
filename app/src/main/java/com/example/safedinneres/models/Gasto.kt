package com.example.safedinneres.models

data class Gasto(
    val id: String? = null,
    val descripcion: String = "",
    val monto: Double = 0.0,
    val categoria: String = "",
    val metodoPago: String = "", // nombre textual (para mostrar)
    val metodoPagoId: String? = null, // referencia al objeto MetodoPago
    val cuentaId: String? = null, // referencia a la cuenta de donde sale el dinero
    val fecha: Long = 0L,
    val userId: String = "",
    val mes: String = ""
)
