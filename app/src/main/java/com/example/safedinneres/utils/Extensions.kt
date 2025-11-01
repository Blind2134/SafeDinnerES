package com.example.safedinneres.utils

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Muestra un Toast de forma más simple
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/**
 * Formatea un número a moneda peruana
 */
fun Double.toSoles(): String {
    return String.format("S/. %.2f", this)
}

/**
 * Extensión para observar Flows de forma más simple
 */
fun <T> LifecycleOwner.collectFlow(
    flow: Flow<T>,
    onCollect: (T) -> Unit
) {
    lifecycleScope.launch {
        flow.collectLatest { value ->
            onCollect(value)
        }
    }
}

/**
 * Extensión para validar email
 */
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}