package com.example.safedinneres.models

data class GastoConDetalles(
    val gasto: Gasto,
    val nombreCategoria: String = "",
    val nombreCuenta: String = "",
    val nombreMetodoPago: String = ""
)