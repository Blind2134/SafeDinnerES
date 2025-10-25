package com.example.safedinneres

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.safedinneres.databinding.ActivityLoginBinding
import com.example.safedinneres.repository.UsuarioRepository
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

        lifecycleScope.launch {
            val resultado = usuarioRepo.iniciarSesion(email, password)
            if (resultado.isSuccess) {
                val usuario = resultado.getOrNull()
                usuario?.let {
                    val prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE)
                    prefs.edit()
                        .putString("nombre_usuario", it.nombre)
                        .putString("uid_usuario", it.id) // opcional si quieres usar el uid luego
                        .apply()
                }

                Toast.makeText(this@LoginActivity, "Bienvenido 👋", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                Toast.makeText(
                    this@LoginActivity,
                    "Error: ${resultado.exceptionOrNull()?.message ?: "No se pudo iniciar sesión"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

