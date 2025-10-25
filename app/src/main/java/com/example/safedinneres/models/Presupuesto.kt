package com.example.safedinneres.models


data class Presupuesto(
    val id: String = "",
    val montoTotal: Double = 0.0,
    val gastado: Double = 0.0,
    val mes: String = "",
    val userId: String = ""
)