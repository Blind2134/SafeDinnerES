package com.example.safedinneres.ui.gastos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.safedinneres.databinding.ItemGastoBinding
import com.example.safedinneres.data.models.Gasto
import java.text.SimpleDateFormat
import java.util.*

class GastoAdapter(
    private var listaGastos: List<Gasto>,
    private val onItemClick: (Gasto) -> Unit
) : RecyclerView.Adapter<GastoAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemGastoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGastoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val gasto = listaGastos[position]

        with(holder.binding) {
            tvDescripcion.text = gasto.descripcion
            tvCategoria.text = gasto.categoria
            tvMonto.text = "S/ %.2f".format(gasto.monto)


            val emojiPago = when (gasto.metodoPago) {
                "Efectivo" -> "💵"
                "Tarjeta" -> "💳"
                "Yape" -> "📱"
                "Plin" -> "💠"
                "Transferencia" -> "🏦"
                else -> "💰"
            }
            tvMetodoPago.text = "$emojiPago ${gasto.metodoPago}"


            val (emojiCat, colorCat) = when (gasto.categoria) {
                "Comida" -> "🍔" to "#FF9800"
                "Transporte" -> "🚌" to "#03A9F4"
                "Educación" -> "📚" to "#8BC34A"
                "Entretenimiento" -> "🎮" to "#E91E63"
                "Otros" -> "💼" to "#9E9E9E"
                else -> "💸" to "#BDBDBD"
            }

            tvIconoCategoria.text = emojiCat
            viewColorCategoria.setBackgroundColor(colorCat.toColorInt())


            val formato = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            tvFecha.text = formato.format(Date(gasto.fecha))


            root.setOnClickListener {
                onItemClick(gasto)
            }
        }
    }

    override fun getItemCount() = listaGastos.size

    fun actualizarLista(nuevaLista: List<Gasto>) {
        listaGastos = nuevaLista
        notifyDataSetChanged()
    }
}
