package com.example.loginapiapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// 👇 Este import es clave
import com.example.loginapiapp.Trip

class TripAdapter(private val tripList: List<Trip>) :
    RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trip, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = tripList[position]
        holder.tvRuta.text = "${trip.origen} → ${trip.destino}"
        holder.tvFecha.text = trip.fecha_hora_salida

    }

    override fun getItemCount(): Int = tripList.size

    class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRuta: TextView = itemView.findViewById(R.id.tvRuta)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
    }
}
