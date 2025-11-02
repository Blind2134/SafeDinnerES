package com.example.safedinneres

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.safedinneres.databinding.ActivityRegisterBinding
import com.example.safedinneres.repository.UsuarioRepository
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val usuarioRepo = UsuarioRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            registrar()
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registrar() {
        val nombre = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmar = binding.etConfirmPassword.text.toString().trim()

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || confirmar.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmar) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(
                this,
                "La contraseña debe tener al menos 6 caracteres",
                Toast.LENGTH_SHORT
            ).show()
            return
        }


        binding.btnRegister.isEnabled = false

        lifecycleScope.launch {
            val resultado = usuarioRepo.registrarUsuario(nombre, email, password)


            binding.btnRegister.isEnabled = true

            if (resultado.isSuccess) {

                Toast.makeText(
                    this@RegisterActivity,
                    R.string.verifica_tu_correo,
                    Toast.LENGTH_LONG
                ).show()

                kotlinx.coroutines.delay(2500)


                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

            } else {
                val error = resultado.exceptionOrNull()
                error?.printStackTrace()
                Toast.makeText(
                    this@RegisterActivity,
                    "Error: ${error?.message ?: "Desconocido"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}