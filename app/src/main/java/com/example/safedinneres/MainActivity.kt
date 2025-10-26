package com.example.safedinneres

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.safedinneres.databinding.ActivityMainBinding
import com.example.safedinneres.models.Gasto
import com.example.safedinneres.models.GastoConDetalles
import com.example.safedinneres.models.Presupuesto
import com.example.safedinneres.repository.GastoRepository
import com.example.safedinneres.repository.PresupuestoRepository
import com.example.safedinneres.repository.CuentaRepository
import com.example.safedinneres.repository.CategoriaRepository
import com.example.safedinneres.repository.MetodoPagoRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : BaseActivityWithNav() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var gastoAdapter: GastoAdapter

    private val gastoRepository = GastoRepository()
    private val presupuestoRepo = PresupuestoRepository()
    private val cuentaRepo = CuentaRepository()
    private val categoriaRepo = CategoriaRepository()
    private val metodoRepo = MetodoPagoRepository()

    private var calendarioMes: Calendar = Calendar.getInstance()
    private var presupuestoActual: Presupuesto? = null
    private var gastosDelMes: List<GastoConDetalles> = emptyList()

    private val userId by lazy {
        getSharedPreferences("USER_PREFS", MODE_PRIVATE).getString("uid_usuario", "") ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔹 Configurar navegación inferior
        setupBottomNavigation(R.id.bottomNavigationView)
        supportActionBar?.hide()

        // 🔹 Mostrar nombre de usuario
        val prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE)
        val nombre = prefs.getString("nombre_usuario", "Usuario")
        binding.tvNombreUsuario.text = nombre

        // 🔹 Recuperar mes guardado
        val mesGuardado = prefs.getString("mes_seleccionado", null)
        if (mesGuardado != null) {
            val formato = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
            try {
                calendarioMes.time = formato.parse(mesGuardado) ?: Calendar.getInstance().time
            } catch (_: Exception) {}
        }

        // 🔹 Configurar RecyclerView
        gastoAdapter = GastoAdapter(emptyList()) { gastoConDetalles ->
            val intent = Intent(this, AgregarGastoActivity::class.java)
            intent.putExtra("gasto_id", gastoConDetalles.gasto.id)
            startActivity(intent)
        }

        binding.rvGastos.layoutManager = LinearLayoutManager(this)
        binding.rvGastos.adapter = gastoAdapter

        // 🔹 Botones para cambiar de mes
        binding.btnMesAnterior.setOnClickListener {
            calendarioMes.add(Calendar.MONTH, -1)
            actualizarMes()
        }
        binding.btnMesSiguiente.setOnClickListener {
            calendarioMes.add(Calendar.MONTH, 1)
            actualizarMes()
        }

        // 🔹 Cerrar sesión
        binding.btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }

        actualizarMes()
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE)
        val mesGuardado = prefs.getString("mes_seleccionado", null)
        if (mesGuardado != null) {
            val formato = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
            try {
                calendarioMes.time = formato.parse(mesGuardado) ?: Calendar.getInstance().time
            } catch (_: Exception) {}
        }
        actualizarMes()
    }

    private fun cerrarSesion() {
        val prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE)
        prefs.edit().clear().apply()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun actualizarMes() {
        val formatoMesFirestore = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
        val mesTexto = formatoMesFirestore.format(calendarioMes.time).replaceFirstChar { it.uppercase() }

        binding.tvMesActual.text = mesTexto

        val prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE)
        prefs.edit().putString("mes_seleccionado", mesTexto).apply()

        cargarGastosDelMes(mesTexto)
    }

    private fun cargarGastosDelMes(mes: String) {
        lifecycleScope.launch {
            val resultado = gastoRepository.listarGastos(mes)
            if (resultado.isSuccess) {
                val gastos = resultado.getOrNull() ?: emptyList()

                // 🔹 Obtener datos relacionados
                val categorias = categoriaRepo.obtenerCategoriasPorUsuario(userId).getOrNull() ?: emptyList()
                val cuentas = cuentaRepo.obtenerCuentasPorUsuario(userId).getOrNull() ?: emptyList()
                val metodos = metodoRepo.obtenerMetodosPorUsuario(userId).getOrNull() ?: emptyList()

                // 🔹 Combinar detalles con nombres legibles
                gastosDelMes = gastos.map { gasto ->
                    val categoriaNombre = categorias.find { it.id == gasto.categoriaId }?.nombre ?: "Sin categoría"
                    val cuentaNombre = cuentas.find { it.id == gasto.cuentaId }?.nombre ?: "Sin cuenta"
                    val metodoNombre = metodos.find { it.id == gasto.metodoPagoId }?.nombre ?: "Sin método"

                    GastoConDetalles(gasto, categoriaNombre, cuentaNombre, metodoNombre)
                }

                if (gastosDelMes.isEmpty()) {
                    binding.llSinGastos.visibility = View.VISIBLE
                    binding.rvGastos.visibility = View.GONE
                } else {
                    binding.llSinGastos.visibility = View.GONE
                    binding.rvGastos.visibility = View.VISIBLE
                    gastoAdapter.actualizarLista(gastosDelMes)
                }

                actualizarResumen(mes)

            } else {
                Toast.makeText(this@MainActivity, "Error al cargar gastos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actualizarResumen(mes: String) {
        lifecycleScope.launch {
            try {
                val totalGastado = gastosDelMes.sumOf { it.gasto.monto }
                val cantidadGastos = gastosDelMes.size

                binding.tvTotalGastado.text = String.format("S/. %.2f", totalGastado)
                binding.tvCantidadGastos.text = "$cantidadGastos gasto${if (cantidadGastos != 1) "s" else ""} este mes"

                val res = presupuestoRepo.obtenerPresupuesto(userId, mes)
                if (res.isSuccess) {
                    presupuestoActual = res.getOrNull()

                    if (presupuestoActual != null && presupuestoActual!!.montoTotal > 0) {
                        binding.progressBarPresupuesto.visibility = View.VISIBLE
                        binding.layoutTextosPresupuesto.visibility = View.VISIBLE

                        val porcentaje = ((totalGastado / presupuestoActual!!.montoTotal) * 100).toInt()
                        binding.progressBarPresupuesto.progress = porcentaje.coerceAtMost(100)

                        binding.tvPresupuestoGastado.text = String.format("Gastado: S/. %.2f", totalGastado)
                        binding.tvPresupuestoTotal.text = String.format("Límite: S/. %.2f", presupuestoActual!!.montoTotal)

                        if (totalGastado > presupuestoActual!!.montoTotal) {
                            binding.progressBarPresupuesto.progressTintList =
                                getColorStateList(android.R.color.holo_red_dark)
                            Toast.makeText(this@MainActivity, "¡Has excedido tu presupuesto!", Toast.LENGTH_SHORT).show()
                        } else {
                            binding.progressBarPresupuesto.progressTintList =
                                getColorStateList(android.R.color.holo_blue_dark)
                        }
                    } else {
                        binding.progressBarPresupuesto.visibility = View.GONE
                        binding.layoutTextosPresupuesto.visibility = View.GONE
                    }
                } else {
                    binding.progressBarPresupuesto.visibility = View.GONE
                    binding.layoutTextosPresupuesto.visibility = View.GONE
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error al actualizar resumen: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
