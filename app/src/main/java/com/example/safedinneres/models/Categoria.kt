package com.example.safedinneres.models

data class Categoria(
    val id: String? = null,
    val nombre: String = "",
    val icono: String? = null, // Opcional (para mostrar en UI)
    val color: String? = null, // Opcional (para personalizar)
    val userId: String = ""
)
