package com.example.safedinneres

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.safedinneres.databinding.ActivityAgregarGastoBinding
import com.example.safedinneres.models.Gasto
import com.example.safedinneres.repository.GastoRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AgregarGastoActivity : BaseActivityWithNav() {

    private lateinit var binding: ActivityAgregarGastoBinding
    private lateinit var gastoRepository: GastoRepository
    private var fechaSeleccionadaTimestamp: Long = System.currentTimeMillis()
    private var gastoId: String? = null  // null si es nuevo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarGastoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gastoRepository = GastoRepository()


        val prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE)
        val mesSeleccionado = prefs.getString("mes_seleccionado", null)

        if (mesSeleccionado != null) {
            try {
                val formato = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
                val fecha = formato.parse(mesSeleccionado)
                if (fecha != null) {

                    fechaSeleccionadaTimestamp = fecha.time
                    val formatoMostrar = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    binding.tvFechaValue.text = formatoMostrar.format(fecha)
                }
            } catch (e: Exception) {
                val formatoMostrar = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.tvFechaValue.text = formatoMostrar.format(Date(fechaSeleccionadaTimestamp))
            }
        } else {
            val formatoMostrar = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            binding.tvFechaValue.text = formatoMostrar.format(Date(fechaSeleccionadaTimestamp))
        }


        configurarSpinnerCategoria()
        configurarSpinnerMetodoPago()

        // Selección de fecha
        binding.tvFechaValue.setOnClickListener { mostrarSelectorFecha() }

        // Botones
        binding.btnGuardar.setOnClickListener { guardarGasto() }
        binding.btnEliminar.setOnClickListener { eliminarGasto() }
        binding.btnCancelar.setOnClickListener { finish() }

        // Revisar si viene un gasto para editar
        gastoId = intent.getStringExtra("gasto_id")
        if (gastoId != null) {
            binding.btnEliminar.visibility = View.VISIBLE
            cargarGasto()
        } else {
            binding.btnEliminar.visibility = View.GONE
        }
    }

    private fun configurarSpinnerCategoria() {
        val categorias = listOf("Comida", "Transporte", "Educación", "Entretenimiento", "Otros")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategoria.adapter = adapter
    }

    private fun configurarSpinnerMetodoPago() {
        val metodos = listOf("Efectivo", "Tarjeta", "Yape", "Plin", "Transferencia")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, metodos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMetodoPago.adapter = adapter
    }

    private fun mostrarSelectorFecha() {
        val calendario = Calendar.getInstance()
        calendario.timeInMillis = fechaSeleccionadaTimestamp

        val anio = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH)
        val dia = calendario.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            this,
            { _, anioSel, mesSel, diaSel ->
                val fechaTexto = "%02d/%02d/%d".format(diaSel, mesSel + 1, anioSel)
                binding.tvFechaValue.text = fechaTexto

                val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                fechaSeleccionadaTimestamp =
                    formato.parse(fechaTexto)?.time ?: System.currentTimeMillis()
            },
            anio, mes, dia
        )
        datePicker.show()
    }

    private fun cargarGasto() {
        lifecycleScope.launch {
            val resultado = gastoRepository.obtenerGastoPorId(gastoId!!)
            if (resultado.isSuccess) {
                val gasto = resultado.getOrNull()
                gasto?.let {
                    binding.etDescripcion.setText(it.descripcion)
                    binding.etMonto.setText(it.monto.toString())
                    binding.tvFechaValue.text =
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it.fecha))
                    fechaSeleccionadaTimestamp = it.fecha

                    binding.spinnerCategoria.setSelection(
                        (binding.spinnerCategoria.adapter as ArrayAdapter<String>).getPosition(it.categoria)
                    )
                    binding.spinnerMetodoPago.setSelection(
                        (binding.spinnerMetodoPago.adapter as ArrayAdapter<String>).getPosition(it.metodoPago)
                    )
                }
            } else {
                Toast.makeText(this@AgregarGastoActivity, "Error al cargar gasto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun guardarGasto() {
        val descripcion = binding.etDescripcion.text.toString().trim()
        val montoTexto = binding.etMonto.text.toString().trim()
        val categoria = binding.spinnerCategoria.selectedItem.toString()
        val metodoPago = binding.spinnerMetodoPago.selectedItem.toString()

        if (descripcion.isEmpty() || montoTexto.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val monto = montoTexto.toDoubleOrNull()
        if (monto == null || monto <= 0) {
            Toast.makeText(this, "Monto inválido", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            // Obtener el gasto original si estamos editando
            val gastoOriginal = gastoId?.let {
                val res = gastoRepository.obtenerGastoPorId(it)
                if (res.isSuccess) res.getOrNull() else null
            }

            val gasto = Gasto(
                id = gastoId,
                descripcion = descripcion,
                monto = monto,
                categoria = categoria,
                metodoPago = metodoPago,
                fecha = fechaSeleccionadaTimestamp,
                userId = gastoOriginal?.userId ?: FirebaseAuth.getInstance().currentUser?.uid ?: "",
                mes = obtenerMesDesdeTimestamp(fechaSeleccionadaTimestamp)
            )

            val resultado = if (gastoId == null) {
                gastoRepository.agregarGasto(gasto)
            } else {
                gastoRepository.actualizarGasto(gasto)
            }

            if (resultado.isSuccess) {
                val mensaje = if (gastoId == null) "Gasto guardado" else "Gasto actualizado"
                Toast.makeText(this@AgregarGastoActivity, mensaje, Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@AgregarGastoActivity, "Error al guardar gasto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun eliminarGasto() {
        gastoId?.let { id ->
            lifecycleScope.launch {
                val resultado = gastoRepository.eliminarGasto(id)
                if (resultado.isSuccess) {
                    Toast.makeText(this@AgregarGastoActivity, "Gasto eliminado", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AgregarGastoActivity, "Error al eliminar gasto", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun obtenerMesDesdeTimestamp(timestamp: Long): String {
        val formatoMes = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
        return formatoMes.format(Date(timestamp)).replaceFirstChar { it.uppercase() }
    }
}
