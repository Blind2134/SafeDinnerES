package com.example.safedinneres.models

data class Usuario(
    val id: String = "",
    val nombre: String = "",
    val email: String = "",
    val creadoEn: Long = System.currentTimeMillis()
)