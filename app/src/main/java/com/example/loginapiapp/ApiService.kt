package com.example.loginapiapp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import com.example.loginapiapp.Trip

interface ApiService {

    @Headers("Content-Type: application/json")
    @POST("auth/login.php")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    // 🔹 Método para traer los últimos viajes
    @GET("viajes/buscar-viajes.php?limit=10")
    fun getLastTrips(): Call<LastTripsResponse>



}
