package com.example.loginapiapp
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ViajeAdapter(private val viajes: List<Viaje>) : RecyclerView.Adapter<ViajeAdapter.ViajeViewHolder>() {

    class ViajeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrigenDestino: TextView = itemView.findViewById(R.id.tvOrigenDestino)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        val tvPrecio: TextView = itemView.findViewById(R.id.tvPrecio)
        val tvDetalles: TextView = itemView.findViewById(R.id.tvDetalles)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViajeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_viaje, parent, false)
        return ViajeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViajeViewHolder, position: Int) {
        val viaje = viajes[position]
        holder.tvOrigenDestino.text = "${viaje.Origen} → ${viaje.Destino}"
        holder.tvFecha.text = viaje.Fecha_Hora_Salida
        holder.tvPrecio.text = "Precio: $${viaje.Precio}"
        holder.tvDetalles.text = viaje.Detalles
    }

    override fun getItemCount(): Int = viajes.size
}
