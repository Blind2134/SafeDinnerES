package com.example.safedinneres

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.safedinneres.databinding.ActivityCuentasBinding
import com.example.safedinneres.databinding.DialogAgregarCuentaBinding
import com.example.safedinneres.models.Cuenta
import com.example.safedinneres.repository.CuentaRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class CuentasActivity : BaseActivityWithNav() {

    private lateinit var binding: ActivityCuentasBinding
    private lateinit var cuentaAdapter: CuentaAdapter
    private val cuentasList = mutableListOf<Cuenta>()

    private val cuentaRepository = CuentaRepository()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCuentasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation(binding.bottomNavigationView.id)

        // Configurar RecyclerView
        cuentaAdapter = CuentaAdapter(
            cuentas = cuentasList,
            onCuentaClick = { cuenta ->
                mostrarDetallesCuenta(cuenta)
            },
            onCuentaLongClick = { cuenta ->
                mostrarOpcionesCuenta(cuenta)
            }
        )
        binding.recyclerCuentas.apply {
            layoutManager = LinearLayoutManager(this@CuentasActivity)
            adapter = cuentaAdapter
        }

        // Configurar botón agregar
        binding.btnAgregarCuenta.setOnClickListener {
            mostrarDialogoAgregarCuenta()
        }

        // Cargar cuentas
        cargarCuentas()
    }

    private fun mostrarDetallesCuenta(cuenta: Cuenta) {
        val mensaje = if (cuenta.tipo == "CREDITO") {
            val disponible = cuenta.limiteCredito - cuenta.deudaActual
            """
            Cuenta de Crédito
            
            Nombre: ${cuenta.nombre}
            Deuda actual: S/. ${String.format("%.2f", cuenta.deudaActual)}
            Límite: S/. ${String.format("%.2f", cuenta.limiteCredito)}
            Disponible: S/. ${String.format("%.2f", disponible)}
            Moneda: ${cuenta.moneda}
            """.trimIndent()
        } else {
            """
            Cuenta de Débito
            
            Nombre: ${cuenta.nombre}
            Saldo disponible: S/. ${String.format("%.2f", cuenta.saldo)}
            Moneda: ${cuenta.moneda}
            """.trimIndent()
        }

        AlertDialog.Builder(this)
            .setTitle("Detalles de la Cuenta")
            .setMessage(mensaje)
            .setPositiveButton("Aceptar", null)
            .show()
    }

    private fun mostrarOpcionesCuenta(cuenta: Cuenta) {
        val opciones = arrayOf("Editar", "Eliminar")

        AlertDialog.Builder(this)
            .setTitle(cuenta.nombre)
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> mostrarDialogoEditarCuenta(cuenta)
                    1 -> mostrarDialogoEliminarCuenta(cuenta)
                }
            }
            .show()
    }

    private fun mostrarDialogoAgregarCuenta() {
        val dialogBinding = DialogAgregarCuentaBinding.inflate(LayoutInflater.from(this))

        // Manejar cambio de tipo de cuenta
        dialogBinding.rgTipoCuenta.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == dialogBinding.rbDebito.id) {
                dialogBinding.tilSaldoInicial.visibility = View.VISIBLE
                dialogBinding.tilLimiteCredito.visibility = View.GONE
            } else {
                dialogBinding.tilSaldoInicial.visibility = View.GONE
                dialogBinding.tilLimiteCredito.visibility = View.VISIBLE
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Nueva Cuenta")
            .setView(dialogBinding.root)
            .setPositiveButton("Agregar") { _, _ ->
                val nombre = dialogBinding.etNombreCuenta.text.toString().trim()
                val esCredito = dialogBinding.rbCredito.isChecked

                if (nombre.isEmpty()) {
                    Toast.makeText(this, "Ingresa un nombre para la cuenta", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (esCredito) {
                    val limiteText = dialogBinding.etLimiteCredito.text.toString().trim()
                    val limite = limiteText.toDoubleOrNull() ?: 0.0
                    agregarCuentaCredito(nombre, limite)
                } else {
                    val saldoText = dialogBinding.etSaldoInicial.text.toString().trim()
                    val saldo = saldoText.toDoubleOrNull() ?: 0.0
                    agregarCuentaDebito(nombre, saldo)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEditarCuenta(cuenta: Cuenta) {
        val dialogBinding = DialogAgregarCuentaBinding.inflate(LayoutInflater.from(this))

        // Pre-llenar datos
        dialogBinding.etNombreCuenta.setText(cuenta.nombre)

        if (cuenta.tipo == "CREDITO") {
            dialogBinding.rbCredito.isChecked = true
            dialogBinding.tilSaldoInicial.visibility = View.GONE
            dialogBinding.tilLimiteCredito.visibility = View.VISIBLE
            dialogBinding.etLimiteCredito.setText(cuenta.limiteCredito.toString())
        } else {
            dialogBinding.rbDebito.isChecked = true
            dialogBinding.tilSaldoInicial.visibility = View.VISIBLE
            dialogBinding.tilLimiteCredito.visibility = View.GONE
            dialogBinding.etSaldoInicial.setText(cuenta.saldo.toString())
        }

        // Manejar cambio de tipo
        dialogBinding.rgTipoCuenta.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == dialogBinding.rbDebito.id) {
                dialogBinding.tilSaldoInicial.visibility = View.VISIBLE
                dialogBinding.tilLimiteCredito.visibility = View.GONE
            } else {
                dialogBinding.tilSaldoInicial.visibility = View.GONE
                dialogBinding.tilLimiteCredito.visibility = View.VISIBLE
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Editar Cuenta")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = dialogBinding.etNombreCuenta.text.toString().trim()
                val esCredito = dialogBinding.rbCredito.isChecked

                if (nombre.isEmpty()) {
                    Toast.makeText(this, "Ingresa un nombre para la cuenta", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (esCredito) {
                    val limiteText = dialogBinding.etLimiteCredito.text.toString().trim()
                    val limite = limiteText.toDoubleOrNull() ?: 0.0
                    editarCuenta(cuenta.copy(
                        nombre = nombre,
                        tipo = "CREDITO",
                        limiteCredito = limite
                    ))
                } else {
                    val saldoText = dialogBinding.etSaldoInicial.text.toString().trim()
                    val saldo = saldoText.toDoubleOrNull() ?: 0.0
                    editarCuenta(cuenta.copy(
                        nombre = nombre,
                        tipo = "DEBITO",
                        saldo = saldo
                    ))
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEliminarCuenta(cuenta: Cuenta) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Cuenta")
            .setMessage("¿Estás seguro de eliminar la cuenta '${cuenta.nombre}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarCuenta(cuenta)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun agregarCuentaDebito(nombre: String, saldo: Double) {
        lifecycleScope.launch {
            val nuevaCuenta = Cuenta(
                nombre = nombre,
                tipo = "DEBITO",
                saldo = saldo,
                moneda = "PEN",
                userId = userId
            )

            val result = cuentaRepository.guardarCuenta(nuevaCuenta)

            if (result.isSuccess) {
                Toast.makeText(this@CuentasActivity, "Cuenta agregada correctamente", Toast.LENGTH_SHORT).show()
                cargarCuentas()
            } else {
                Toast.makeText(this@CuentasActivity, "Error al agregar cuenta: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun agregarCuentaCredito(nombre: String, limite: Double) {
        lifecycleScope.launch {
            val nuevaCuenta = Cuenta(
                nombre = nombre,
                tipo = "CREDITO",
                limiteCredito = limite,
                deudaActual = 0.0,
                moneda = "PEN",
                userId = userId
            )

            val result = cuentaRepository.guardarCuenta(nuevaCuenta)

            if (result.isSuccess) {
                Toast.makeText(this@CuentasActivity, "Cuenta agregada correctamente", Toast.LENGTH_SHORT).show()
                cargarCuentas()
            } else {
                Toast.makeText(this@CuentasActivity, "Error al agregar cuenta: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun editarCuenta(cuenta: Cuenta) {
        lifecycleScope.launch {
            val result = cuentaRepository.guardarCuenta(cuenta)

            if (result.isSuccess) {
                Toast.makeText(this@CuentasActivity, "Cuenta actualizada correctamente", Toast.LENGTH_SHORT).show()
                cargarCuentas()
            } else {
                Toast.makeText(this@CuentasActivity, "Error al actualizar cuenta: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun eliminarCuenta(cuenta: Cuenta) {
        lifecycleScope.launch {
            cuenta.id?.let { id ->
                val result = cuentaRepository.eliminarCuenta(id)

                if (result.isSuccess) {
                    Toast.makeText(this@CuentasActivity, "Cuenta eliminada correctamente", Toast.LENGTH_SHORT).show()
                    cargarCuentas()
                } else {
                    Toast.makeText(this@CuentasActivity, "Error al eliminar cuenta: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun cargarCuentas() {
        lifecycleScope.launch {
            val result = cuentaRepository.obtenerCuentasPorUsuario(userId)

            if (result.isSuccess) {
                cuentasList.clear()
                result.getOrNull()?.let { cuentas ->
                    cuentasList.addAll(cuentas)
                }
                cuentaAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this@CuentasActivity, "Error al cargar cuentas: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}