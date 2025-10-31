package com.example.safedinneres

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.safedinneres.databinding.ActivityAgregarGastoBinding
import com.example.safedinneres.models.*
import com.example.safedinneres.repository.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class AgregarGastoActivity : BaseActivityWithNav() {

    private lateinit var binding: ActivityAgregarGastoBinding
    private lateinit var gastoRepository: GastoRepository
    private lateinit var categoriaRepository: CategoriaRepository
    private lateinit var metodoPagoRepository: MetodoPagoRepository
    private lateinit var cuentaRepository: CuentaRepository

    private var fechaSeleccionadaTimestamp: Long = System.currentTimeMillis()
    private var gastoId: String? = null

    private var categorias: List<Categoria> = emptyList()
    private var metodosPago: List<MetodoPago> = emptyList()
    private var cuentas: List<Cuenta> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarGastoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gastoRepository = GastoRepository()
        categoriaRepository = CategoriaRepository()
        metodoPagoRepository = MetodoPagoRepository()
        cuentaRepository = CuentaRepository()

        inicializarFecha()
        cargarSpinners()

        binding.tvFechaValue.setOnClickListener { mostrarSelectorFecha() }
        binding.btnGuardar.setOnClickListener { guardarGasto() }
        binding.btnEliminar.setOnClickListener { eliminarGasto() }
        binding.btnCancelar.setOnClickListener { finish() }

        gastoId = intent.getStringExtra("gasto_id")
        if (gastoId != null) {
            binding.btnEliminar.visibility = View.VISIBLE
            cargarGasto()
        } else {
            binding.btnEliminar.visibility = View.GONE
        }
    }

    private fun inicializarFecha() {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.tvFechaValue.text = formato.format(Date(fechaSeleccionadaTimestamp))
    }

    private fun cargarSpinners() {
        lifecycleScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

                val categoriasResult = categoriaRepository.obtenerCategoriasPorUsuario(userId)
                val metodosResult = metodoPagoRepository.obtenerMetodosPorUsuario(userId)
                val cuentasResult = cuentaRepository.obtenerCuentasPorUsuario(userId)

                if (categoriasResult.isSuccess && metodosResult.isSuccess && cuentasResult.isSuccess) {
                    categorias = categoriasResult.getOrDefault(emptyList())
                    metodosPago = metodosResult.getOrDefault(emptyList())
                    cuentas = cuentasResult.getOrDefault(emptyList())

                    binding.spinnerCategoria.adapter = ArrayAdapter(
                        this@AgregarGastoActivity,
                        android.R.layout.simple_spinner_item,
                        categorias.map { it.nombre }
                    )

                    binding.spinnerMetodoPago.adapter = ArrayAdapter(
                        this@AgregarGastoActivity,
                        android.R.layout.simple_spinner_item,
                        metodosPago.map { it.nombre }
                    )

                    binding.spinnerCuenta.adapter = ArrayAdapter(
                        this@AgregarGastoActivity,
                        android.R.layout.simple_spinner_item,
                        cuentas.map { it.nombre }
                    )
                } else {
                    Toast.makeText(this@AgregarGastoActivity, "Error cargando listas", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@AgregarGastoActivity, "Error cargando datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
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

                    val posCategoria = categorias.indexOfFirst { c -> c.id == it.categoriaId }
                    val posMetodo = metodosPago.indexOfFirst { m -> m.id == it.metodoPagoId }
                    val posCuenta = cuentas.indexOfFirst { c -> c.id == it.cuentaId }

                    if (posCategoria >= 0) binding.spinnerCategoria.setSelection(posCategoria)
                    if (posMetodo >= 0) binding.spinnerMetodoPago.setSelection(posMetodo)
                    if (posCuenta >= 0) binding.spinnerCuenta.setSelection(posCuenta)
                }
            }
        }
    }

    private fun guardarGasto() {
        val descripcion = binding.etDescripcion.text.toString().trim()
        val monto = binding.etMonto.text.toString().toDoubleOrNull() ?: 0.0
        if (descripcion.isEmpty() || monto <= 0) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val categoriaSeleccionada = categorias.getOrNull(binding.spinnerCategoria.selectedItemPosition)
        val metodoSeleccionado = metodosPago.getOrNull(binding.spinnerMetodoPago.selectedItemPosition)
        val cuentaSeleccionada = cuentas.getOrNull(binding.spinnerCuenta.selectedItemPosition)

        if (categoriaSeleccionada == null || metodoSeleccionado == null || cuentaSeleccionada == null) {
            Toast.makeText(this, "Selecciona todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

            val gasto = Gasto(
                id = gastoId,
                descripcion = descripcion,
                monto = monto,
                categoriaId = categoriaSeleccionada.id ?: "",
                cuentaId = cuentaSeleccionada.id ?: "",
                metodoPagoId = metodoSeleccionado.id ?: "",
                fecha = fechaSeleccionadaTimestamp,
                userId = userId
            )

            val resultado = gastoRepository.guardarGasto(gasto)

            if (resultado.isSuccess) {
                // 🔹 Actualizar saldo o deuda de la cuenta
                if (cuentaSeleccionada.tipo == "DEBITO") {
                    val nuevoSaldo = cuentaSeleccionada.saldo - monto
                    cuentaRepository.actualizarSaldoCuenta(cuentaSeleccionada.id!!, nuevoSaldo)
                } else if (cuentaSeleccionada.tipo == "CREDITO") {
                    val nuevaDeuda = cuentaSeleccionada.deudaActual + monto
                    cuentaRepository.actualizarDeudaCuenta(cuentaSeleccionada.id!!, nuevaDeuda)
                }

                Toast.makeText(this@AgregarGastoActivity, "Gasto guardado correctamente", Toast.LENGTH_SHORT).show()
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
}
