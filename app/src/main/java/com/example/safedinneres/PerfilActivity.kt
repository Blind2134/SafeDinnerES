package com.example.safedinneres

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.safedinneres.databinding.ActivityPerfilBinding
import com.example.safedinneres.repository.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PerfilActivity : BaseActivityWithNav() {

    private lateinit var binding: ActivityPerfilBinding
    private val usuarioRepository = UsuarioRepository()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation(binding.bottomNavigationView.id)

        cargarDatosUsuario()

        binding.btnCerrarSesion.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun cargarDatosUsuario() {
        val usuarioFirebase = auth.currentUser ?: return

        CoroutineScope(Dispatchers.Main).launch {
            val result = usuarioRepository.obtenerUsuarioPorId(usuarioFirebase.uid)

            result.fold(
                onSuccess = { usuario ->
                    binding.tvNombre.text = usuario.nombre
                    binding.tvEmail.text = usuario.email


                    val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(Date(usuario.creadoEn))

                    binding.tvFechaRegistro.text = "Registrado: $fecha"
                },
                onFailure = {
                    Toast.makeText(
                        this@PerfilActivity,
                        "Error al cargar perfil",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }
}
