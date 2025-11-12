package com.example.loginapiapp

import com.google.gson.annotations.SerializedName

data class Trip(
    @SerializedName("ID_Viaje") val id: Int,
    @SerializedName("Origen") val origen: String,
    @SerializedName("Destino") val destino: String,
    @SerializedName("Fecha_Hora_Salida") val fecha_hora_salida: String,
    @SerializedName("Lugares_Disponibles") val lugares_disponibles: Int,
    @SerializedName("Precio") val precio: Double
)
