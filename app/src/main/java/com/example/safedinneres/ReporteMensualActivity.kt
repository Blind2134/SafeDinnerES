package com.example.safedinneres

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.safedinneres.databinding.ActivityReporteMensualBinding
import com.example.safedinneres.models.Gasto
import com.example.safedinneres.models.Presupuesto
import com.example.safedinneres.repository.GastoRepository
import com.example.safedinneres.repository.PresupuestoRepository
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ReporteMensualActivity : BaseActivityWithNav() {

    private lateinit var binding: ActivityReporteMensualBinding
    private val gastoRepository = GastoRepository()
    private val presupuestoRepo = PresupuestoRepository()

    private var calendarioMes: Calendar = Calendar.getInstance()
    private var gastosDelMes: List<Gasto> = emptyList()
    private var gastosDelMesAnterior: List<Gasto> = emptyList()

    private val userId by lazy {
        getSharedPreferences("USER_PREFS", MODE_PRIVATE)
            .getString("uid_usuario", "") ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReporteMensualBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        setupBottomNavigation(R.id.bottomNavigationView)

        val formato = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
        val prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE)

        val mesGuardado = prefs.getString("mes_seleccionado", null)
        if (mesGuardado != null) {
            try {
                calendarioMes.time = formato.parse(mesGuardado) ?: Calendar.getInstance().time
            } catch (_: Exception) {
                calendarioMes = Calendar.getInstance()
            }
        }

        binding.btnMesAnterior.setOnClickListener {
            calendarioMes.add(Calendar.MONTH, -1)
            actualizarReporte()
        }

        binding.btnMesSiguiente.setOnClickListener {
            calendarioMes.add(Calendar.MONTH, 1)
            actualizarReporte()
        }

        actualizarReporte()
    }

    private fun actualizarReporte() {
        val formatoMes = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
        val mesTexto = formatoMes.format(calendarioMes.time).replaceFirstChar { it.uppercase() }
        binding.tvMesActual.text = mesTexto

        val prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE)
        prefs.edit().putString("mes_seleccionado", mesTexto).apply()

        lifecycleScope.launch {
            try {
                val resGastos = gastoRepository.obtenerGastosPorUsuario(userId)
                if (resGastos.isSuccess) {
                    val todosGastos = resGastos.getOrNull() ?: emptyList()

                    // Filtrar por mes actual y mes anterior
                    gastosDelMes = filtrarGastosPorMes(todosGastos, calendarioMes)

                    val calAnterior = calendarioMes.clone() as Calendar
                    calAnterior.add(Calendar.MONTH, -1)
                    gastosDelMesAnterior = filtrarGastosPorMes(todosGastos, calAnterior)
                }

                // Obtener presupuesto del mes
                val resPresupuesto = presupuestoRepo.obtenerPresupuesto(userId, mesTexto)
                val presupuesto = if (resPresupuesto.isSuccess) resPresupuesto.getOrNull() else null

                // Actualizar UI
                actualizarResumen(presupuesto)
                actualizarComparacion()
                actualizarGraficoPastel()
                actualizarTop3()

            } catch (e: Exception) {
                Toast.makeText(
                    this@ReporteMensualActivity,
                    "Error al cargar reporte: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun filtrarGastosPorMes(gastos: List<Gasto>, calendario: Calendar): List<Gasto> {
        val mes = calendario.get(Calendar.MONTH)
        val anio = calendario.get(Calendar.YEAR)

        val calTemp = Calendar.getInstance()

        return gastos.filter {
            calTemp.timeInMillis = it.fecha
            calTemp.get(Calendar.MONTH) == mes && calTemp.get(Calendar.YEAR) == anio
        }
    }

    private fun actualizarResumen(presupuesto: Presupuesto?) {
        val totalGastado = gastosDelMes.sumOf { it.monto }
        binding.tvTotalMes.text = String.format("Gastado: S/. %.2f", totalGastado)

        if (presupuesto != null && presupuesto.montoTotal > 0) {
            val porcentaje = ((totalGastado / presupuesto.montoTotal) * 100).toInt()
            binding.progressPresupuesto.progress = porcentaje.coerceAtMost(100)
            binding.tvPresupuestoInfo.text = "$porcentaje% del presupuesto usado"

            binding.progressPresupuesto.progressTintList =
                if (totalGastado > presupuesto.montoTotal)
                    getColorStateList(android.R.color.holo_red_dark)
                else
                    getColorStateList(android.R.color.holo_blue_dark)
        } else {
            binding.progressPresupuesto.progress = 0
            binding.tvPresupuestoInfo.text = "Sin presupuesto configurado"
        }
    }

    private fun actualizarComparacion() {
        val totalActual = gastosDelMes.sumOf { it.monto }
        val totalAnterior = gastosDelMesAnterior.sumOf { it.monto }

        val texto = if (totalAnterior == 0.0) {
            "No hay datos del mes anterior"
        } else {
            val diferencia = totalActual - totalAnterior
            val porcentaje = ((diferencia / totalAnterior) * 100)
            when {
                diferencia > 0 -> "Gastaste S/. %.2f más que el mes anterior (+%.1f%%)".format(
                    diferencia,
                    porcentaje
                )
                diferencia < 0 -> "Gastaste S/. %.2f menos que el mes anterior (%.1f%%)".format(
                    -diferencia,
                    porcentaje
                )
                else -> "Gastaste lo mismo que el mes anterior"
            }
        }

        binding.tvComparacion.text = texto
    }

    private fun actualizarGraficoPastel() {
        if (gastosDelMes.isEmpty()) {
            binding.pieChartCategorias.visibility = View.GONE
            return
        }

        binding.pieChartCategorias.visibility = View.VISIBLE

        val gastosPorCategoria = gastosDelMes.groupBy { it.categoriaId }
            .mapValues { entry -> entry.value.sumOf { it.monto }.toFloat() }

        val entries = gastosPorCategoria.map { PieEntry(it.value, it.key) }

        val colores = listOf(
            Color.parseColor("#FF6B6B"), // Rojo
            Color.parseColor("#4ECDC4"), // Turquesa
            Color.parseColor("#45B7D1"), // Azul
            Color.parseColor("#FFA07A"), // Salmón
            Color.parseColor("#98D8C8"), // Verde menta
            Color.parseColor("#F7DC6F"), // Amarillo
            Color.parseColor("#BB8FCE"), // Púrpura
            Color.parseColor("#85C1E2")  // Azul claro
        )

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colores
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.WHITE
        dataSet.sliceSpace = 3f

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(binding.pieChartCategorias))

        binding.pieChartCategorias.apply {
            this.data = data
            description.isEnabled = false
            legend.textSize = 12f
            setEntryLabelTextSize(11f)
            setEntryLabelColor(Color.BLACK)
            setUsePercentValues(true)
            setDrawEntryLabels(true)
            centerText = "Categorías"
            setCenterTextSize(14f)
            animateY(1000)
            invalidate()
        }
    }

    private fun actualizarTop3() {
        binding.llTop3.removeAllViews()

        if (gastosDelMes.isEmpty()) {
            val tvSinDatos = TextView(this).apply {
                text = "No hay gastos registrados este mes"
                textSize = 14f
                setPadding(0, 16, 0, 16)
            }
            binding.llTop3.addView(tvSinDatos)
            return
        }

        val formato = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
        val top3 = gastosDelMes.sortedByDescending { it.monto }.take(3)

        top3.forEachIndexed { index, gasto ->
            val itemView = layoutInflater.inflate(
                android.R.layout.simple_list_item_2,
                binding.llTop3,
                false
            )

            val tvTitulo = itemView.findViewById<TextView>(android.R.id.text1)
            val tvSubtitulo = itemView.findViewById<TextView>(android.R.id.text2)

            tvTitulo.text = "${index + 1}. ${gasto.descripcion}"
            tvSubtitulo.text =
                "${gasto.categoriaId} - S/. %.2f - ${formato.format(Date(gasto.fecha))}".format(gasto.monto)

            binding.llTop3.addView(itemView)
        }
    }
}
