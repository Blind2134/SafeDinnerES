package com.example.safedinneres.utils

object Constants {
    // SharedPreferences
    const val PREF_NAME = "USER_PREFS"
    const val PREF_USER_ID = "uid_usuario"
    const val PREF_USER_NAME = "nombre_usuario"
    const val PREF_SELECTED_MONTH = "mes_seleccionado"

    // Firebase Collections
    const val COLLECTION_GASTOS = "gastos"
    const val COLLECTION_PRESUPUESTOS = "presupuestos"
    const val COLLECTION_USUARIOS = "usuarios"

    // Formatos de fecha
    const val DATE_FORMAT_DISPLAY = "dd/MM/yyyy"
    const val DATE_FORMAT_MONTH = "MMMM yyyy"

    // Categorías
    val CATEGORIAS = listOf(
        "Comida", "Transporte", "Educación",
        "Entretenimiento", "Otros"
    )

    // Métodos de pago
    val METODOS_PAGO = listOf(
        "Efectivo", "Tarjeta", "Yape",
        "Plin", "Transferencia"
    )
}