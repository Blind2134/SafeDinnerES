package com.example.safedinneres.models

data class Gasto(
    val id: String? = null,
    val descripcion: String = "",
    val monto: Double = 0.0,
    val categoriaId: String = "",
    val cuentaId: String = "",
    val metodoPagoId: String = "",
    val fecha: Long = System.currentTimeMillis(),
    val userId: String = ""
)