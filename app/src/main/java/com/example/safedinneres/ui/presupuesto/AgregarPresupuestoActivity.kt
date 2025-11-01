package com.example.safedinneres.ui.presupuesto

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.safedinneres.base.BaseActivityWithNav
import com.example.safedinneres.R
import com.example.safedinneres.databinding.ActivityAgregarPresupuestoBinding
import com.example.safedinneres.data.models.Presupuesto
import com.example.safedinneres.data.repository.PresupuestoRepository
import com.example.safedinneres.ui.main.MainActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AgregarPresupuestoActivity : BaseActivityWithNav() {

    private lateinit var binding: ActivityAgregarPresupuestoBinding
    private val presupuestoRepository = PresupuestoRepository()

    private var presupuestoExistente: Presupuesto? = null
    private var userId: String = ""
    private var mesActual: String = ""

    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarPresupuestoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE)
        userId = prefs.getString("uid_usuario", "") ?: ""


        val mesGuardado = prefs.getString("mes_seleccionado", null)
        mesActual = if (mesGuardado != null && mesGuardado.isNotEmpty()) {
            mesGuardado
        } else {
            SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
                .format(calendar.time)
                .replaceFirstChar { it.uppercase() }
        }

        binding.tvMonthValue.text = mesActual


        binding.tvMonthValue.setOnClickListener {
            mostrarSelectorMes()
        }


        cargarPresupuesto()


        binding.btnSave.setOnClickListener {
            guardarPresupuesto()
        }


        binding.btnCancel.setOnClickListener {
            volverAlInicio()
        }


        binding.btnDelete.setOnClickListener {
            eliminarPresupuesto()
        }
    }

    private fun mostrarSelectorMes() {
        val año = calendar.get(Calendar.YEAR)
        val mes = calendar.get(Calendar.MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, yearSelected, monthSelected, _ ->
                calendar.set(Calendar.YEAR, yearSelected)
                calendar.set(Calendar.MONTH, monthSelected)


                mesActual = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
                    .format(calendar.time)
                    .replaceFirstChar { it.uppercase() }

                binding.tvMonthValue.text = mesActual


                val prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE)
                prefs.edit().putString("mes_seleccionado", mesActual).apply()


                cargarPresupuesto()
            },
            año,
            mes,
            1
        )


        try {
            val daySpinner = datePickerDialog.datePicker
                .findViewById<CalendarView>(
                    resources.getIdentifier("day", "id", "android")
                )
            daySpinner?.visibility = View.GONE
        } catch (_: Exception) {}

        datePickerDialog.show()
    }

    private fun cargarPresupuesto() {
        lifecycleScope.launch {
            val resultado = presupuestoRepository.obtenerPresupuesto(userId, mesActual)
            if (resultado.isSuccess) {
                presupuestoExistente = resultado.getOrNull()
                presupuestoExistente?.let {
                    binding.etAmount.setText(it.montoTotal.toString())
                    binding.btnDelete.visibility = View.VISIBLE
                    binding.spaceDelete.visibility = View.VISIBLE
                } ?: run {
                    binding.etAmount.setText("")
                    binding.btnDelete.visibility = View.GONE
                    binding.spaceDelete.visibility = View.GONE
                }
            } else {
                Toast.makeText(
                    this@AgregarPresupuestoActivity,
                    "Error al cargar presupuesto",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun guardarPresupuesto() {
        val monto = binding.etAmount.text.toString().toDoubleOrNull()
        if (monto == null || monto <= 0) {
            Toast.makeText(this, "Ingresa un monto válido", Toast.LENGTH_SHORT).show()
            return
        }

        val presupuesto = Presupuesto(
            id = presupuestoExistente?.id ?: "",
            montoTotal = monto,
            gastado = 0.0,
            mes = mesActual,
            userId = userId
        )

        lifecycleScope.launch {
            val res = presupuestoRepository.guardarPresupuesto(presupuesto)
            if (res.isSuccess) {

                val prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE)
                prefs.edit().putString("mes_seleccionado", mesActual).apply()

                Toast.makeText(this@AgregarPresupuestoActivity, "Presupuesto guardado", Toast.LENGTH_SHORT).show()
                volverAlInicio()
            } else {
                Toast.makeText(this@AgregarPresupuestoActivity, "Error al guardar presupuesto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun eliminarPresupuesto() {
        presupuestoExistente?.let { pres ->
            lifecycleScope.launch {
                val res = presupuestoRepository.eliminarPresupuesto(pres.id)
                if (res.isSuccess) {
                    Toast.makeText(this@AgregarPresupuestoActivity, "Presupuesto eliminado", Toast.LENGTH_SHORT).show()
                    volverAlInicio()
                } else {
                    Toast.makeText(this@AgregarPresupuestoActivity, "Error al eliminar", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            Toast.makeText(this, "No hay presupuesto para eliminar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun volverAlInicio() {

        val prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE)
        prefs.edit().putString("mes_seleccionado", mesActual).apply()

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}