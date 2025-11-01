package com.example.safedinneres.data.models

data class Usuario(
    val id: String = "",
    val nombre: String = "",
    val email: String = "",
    val creadoEn: Long = System.currentTimeMillis()
)