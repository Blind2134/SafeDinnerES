package com.example.safedinneres

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.safedinneres.R
import com.example.safedinneres.databinding.ItemCuentaBinding
import com.example.safedinneres.models.Cuenta
import java.text.NumberFormat
import java.util.Locale

class CuentaAdapter(
    private val cuentas: List<Cuenta>,
    private val onCuentaClick: (Cuenta) -> Unit = {},
    private val onCuentaLongClick: (Cuenta) -> Unit = {}
) : RecyclerView.Adapter<CuentaAdapter.CuentaViewHolder>() {

    inner class CuentaViewHolder(val binding: ItemCuentaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CuentaViewHolder {
        val binding = ItemCuentaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CuentaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CuentaViewHolder, position: Int) {
        val cuenta = cuentas[position]
        val context = holder.itemView.context
        val esCredito = cuenta.tipo == "CREDITO"

        with(holder.binding) {
            // Nombre de la cuenta
            tvNombreCuenta.text = cuenta.nombre

            // Tipo de cuenta y moneda
            tvTipoCuenta.text = "${cuenta.tipo.capitalize()} • ${cuenta.moneda}"

            // Saldo o deuda
            if (esCredito) {
                // Si es cuenta de crédito, mostrar deuda
                tvSaldoCuenta.text = formatearMoneda(cuenta.deudaActual)
                tvSaldoCuenta.setTextColor(ContextCompat.getColor(context, R.color.error))

                // Estado de la deuda
                val porcentajeUsado = if (cuenta.limiteCredito > 0) {
                    (cuenta.deudaActual / cuenta.limiteCredito) * 100
                } else 0.0

                tvEstadoCuenta.text = when {
                    porcentajeUsado >= 90 -> "Límite cercano"
                    porcentajeUsado >= 50 -> "Uso moderado"
                    else -> "Disponible"
                }
                tvEstadoCuenta.setTextColor(ContextCompat.getColor(context,
                    when {
                        porcentajeUsado >= 90 -> R.color.error
                        porcentajeUsado >= 50 -> R.color.warning
                        else -> R.color.success
                    }
                ))
            } else {
                // Si es cuenta de débito, mostrar saldo disponible
                tvSaldoCuenta.text = formatearMoneda(cuenta.saldo)
                tvSaldoCuenta.setTextColor(ContextCompat.getColor(context, R.color.blue_dark))

                // Estado del saldo
                tvEstadoCuenta.text = when {
                    cuenta.saldo > 1000 -> "Bueno"
                    cuenta.saldo > 100 -> "Disponible"
                    cuenta.saldo > 0 -> "Bajo"
                    else -> "Sin fondos"
                }
                tvEstadoCuenta.setTextColor(ContextCompat.getColor(context,
                    when {
                        cuenta.saldo > 1000 -> R.color.success
                        cuenta.saldo > 100 -> R.color.blue_dark
                        cuenta.saldo > 0 -> R.color.warning
                        else -> R.color.error
                    }
                ))
            }

            // Ícono según tipo de cuenta
            val icono = when {
                esCredito -> R.drawable.ic_credit_card
                cuenta.nombre.contains("efectivo", ignoreCase = true) -> R.drawable.ic_cash
                cuenta.nombre.contains("yape", ignoreCase = true) ||
                        cuenta.nombre.contains("plin", ignoreCase = true) -> R.drawable.ic_cash
                cuenta.nombre.contains("banco", ignoreCase = true) ||
                        cuenta.nombre.contains("bcp", ignoreCase = true) ||
                        cuenta.nombre.contains("bbva", ignoreCase = true) ||
                        cuenta.nombre.contains("interbank", ignoreCase = true) -> R.drawable.ic_bank
                else -> R.drawable.ic_cuentas
            }
            ivIconoCuenta.setImageResource(icono)

            // Click normal para ver detalles
            root.setOnClickListener {
                onCuentaClick(cuenta)
            }

            // Long click para opciones (editar/eliminar)
            root.setOnLongClickListener {
                onCuentaLongClick(cuenta)
                true
            }
        }
    }

    override fun getItemCount(): Int = cuentas.size

    private fun formatearMoneda(monto: Double): String {
        val formato = NumberFormat.getCurrencyInstance(Locale("es", "PE"))
        return formato.format(monto)
    }
}