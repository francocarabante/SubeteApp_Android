package com.example.loginapiapp

data class Viaje(
    val ID_Viaje: Int,
    val ID_Usuario: Int,
    val Origen: String,
    val Destino: String,
    val Fecha_Hora_Salida: String,
    val Lugares_Disponibles: Int,
    val Precio: Float,
    val Permite_Encomiendas: Int,
    val Detalles: String,
    val Estado: String,
    val Conductor_Nombre: String?,
    val Conductor_Apellido: String?,
    val Conductor_Telefono: String?
)
