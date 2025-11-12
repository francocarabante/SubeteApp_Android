package com.example.loginapiapp

import com.google.gson.annotations.SerializedName

data class LastTripsResponse(
    @SerializedName("ok") val ok: Boolean,
    @SerializedName("results") val results: List<Trip>
)
