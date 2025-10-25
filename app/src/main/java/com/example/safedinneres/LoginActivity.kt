package com.example.safedinneres

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.safedinneres.databinding.ActivityLoginBinding
import com.example.safedinneres.repository.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val usuarioRepo = UsuarioRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            iniciarSesion()
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun iniciarSesion() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // ⭐ DESHABILITAR BOTÓN MIENTRAS PROCESA
        binding.btnLogin.isEnabled = false

        lifecycleScope.launch {
            val resultado = usuarioRepo.iniciarSesion(email, password)

            // ⭐ HABILITAR BOTÓN NUEVAMENTE
            binding.btnLogin.isEnabled = true

            if (resultado.isSuccess) {
                val usuario = resultado.getOrNull()
                usuario?.let {
                    val prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE)
                    prefs.edit()
                        .putString("nombre_usuario", it.nombre)
                        .putString("uid_usuario", it.id)
                        .apply()
                }

                Toast.makeText(this@LoginActivity, "Bienvenido 👋", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                val mensajeError = resultado.exceptionOrNull()?.message ?: "No se pudo iniciar sesión"

                // ⭐ NUEVO: Si el error es por email no verificado, ofrecer reenviar
                if (mensajeError.contains("verifica tu correo", ignoreCase = true)) {
                    mostrarDialogReenviarVerificacion(email, password)
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Error: $mensajeError",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // ⭐ NUEVA FUNCIÓN: Dialog para reenviar email de verificación
    private fun mostrarDialogReenviarVerificacion(email: String, password: String) {
        AlertDialog.Builder(this)
            .setTitle("Email no verificado")
            .setMessage("Tu correo electrónico aún no ha sido verificado.\n\n¿Deseas que te reenviemos el correo de verificación?")
            .setPositiveButton("Reenviar") { dialog, _ ->
                reenviarEmailVerificacion(email, password)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    // ⭐ NUEVA FUNCIÓN: Reenviar email de verificación
    private fun reenviarEmailVerificacion(email: String, password: String) {
        lifecycleScope.launch {
            try {
                // Primero hacer login temporal para poder enviar el email
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        // Enviar email de verificación
                        it.user?.sendEmailVerification()
                            ?.addOnSuccessListener {
                                // Cerrar sesión inmediatamente
                                FirebaseAuth.getInstance().signOut()

                                Toast.makeText(
                                    this@LoginActivity,
                                    "Correo de verificación reenviado. Por favor revisa tu bandeja de entrada",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            ?.addOnFailureListener { error ->
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Error al reenviar: ${error.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(
                            this@LoginActivity,
                            "Error: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            } catch (e: Exception) {
                Toast.makeText(
                    this@LoginActivity,
                    "Error al reenviar: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}