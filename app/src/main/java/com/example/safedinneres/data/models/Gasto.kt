package com.example.safedinneres.data.models

data class Gasto(
    val id: String? = null,
    val descripcion: String = "",
    val monto: Double = 0.0,
    val categoria: String = "",
    val metodoPago: String = "",
    val fecha: Long = 0L   ,
    val userId: String = "",
    val mes: String = ""
)