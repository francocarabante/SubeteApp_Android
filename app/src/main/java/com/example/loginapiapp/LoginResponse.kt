package com.example.loginapiapp

data class LoginResponse(
    val mensaje: String?,
    val token: String?,
    val usuario: Usuario?,
    val error: String? = null
)

data class Usuario(
    val id: Int,
    val nombre: String,
    val email: String,
    val rol: String
)
