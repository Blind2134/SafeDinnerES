package com.example.safedinneres.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val formatoMostrar = SimpleDateFormat(
        Constants.DATE_FORMAT_DISPLAY,
        Locale.getDefault()
    )

    private val formatoMes = SimpleDateFormat(
        Constants.DATE_FORMAT_MONTH,
        Locale("es", "ES")
    )

    /**
     * Convierte un timestamp a formato dd/MM/yyyy
     */
    fun formatearFecha(timestamp: Long): String {
        return formatoMostrar.format(Date(timestamp))
    }

    /**
     * Obtiene el mes en formato "Enero 2025"
     */
    fun obtenerMesTexto(timestamp: Long): String {
        return formatoMes.format(Date(timestamp))
            .replaceFirstChar { it.uppercase() }
    }

    /**
     * Obtiene el mes actual
     */
    fun obtenerMesActual(): String {
        return obtenerMesTexto(System.currentTimeMillis())
    }

    /**
     * Parsea un mes en formato texto a Calendar
     */
    fun parsearMes(mesTexto: String): Calendar {
        val calendar = Calendar.getInstance()
        try {
            calendar.time = formatoMes.parse(mesTexto)
                ?: Calendar.getInstance().time
        } catch (e: Exception) {
            // Retorna mes actual si hay error
        }
        return calendar
    }
}