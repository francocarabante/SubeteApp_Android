package com.example.loginapiapp

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.PopupMenu
import android.widget.Button
import android.content.Intent



class HomeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tripAdapter: TripAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // 🔹 Configurar botón del menú del header
        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)
        btnMenu.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.menu_main, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_inicio -> {
                        // Ya estás en inicio
                        true
                    }

                    R.id.menu_buscar -> {
                        val intent = Intent(this, BuscarViajeActivity::class.java)
                        startActivity(intent)
                        true
                    }

                    R.id.menu_publicar -> {
                        val intent = Intent(this, PublicarViajeActivity::class.java)
                        startActivity(intent)
                        true
                    }

                    R.id.menu_mis_viajes -> {
                        Toast.makeText(this, "🔧 Sección en desarrollo", Toast.LENGTH_SHORT).show()
                        true
                    }

                    R.id.menu_salir -> {
                        // Cerrar sesión
                        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                        prefs.edit().remove("token").apply()

                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                        true
                    }

                    else -> false
                }
            }
            popup.show()
        }

        // 🔹 Referencias del layout
        recyclerView = findViewById(R.id.rvLastTrips)
        val btnBuscar = findViewById<Button>(R.id.btnBuscarViaje)
        val btnCrear = findViewById<Button>(R.id.btnCrearViaje)

        recyclerView.layoutManager = LinearLayoutManager(this)
        tripAdapter = TripAdapter(emptyList())
        recyclerView.adapter = tripAdapter

        loadLastTrips()

        btnBuscar.setOnClickListener {
            // Abrir la pantalla de Buscar Viajes
            val intent = Intent(this, BuscarViajeActivity::class.java)
            startActivity(intent)
        }


        btnCrear.setOnClickListener {
            val intent = Intent(this, PublicarViajeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadLastTrips() {
        RetrofitClient.instance.getLastTrips().enqueue(object : Callback<LastTripsResponse> {
            override fun onResponse(call: Call<LastTripsResponse>, response: Response<LastTripsResponse>) {
                if (response.isSuccessful) {
                    val trips = response.body()?.results?.takeLast(4)?.reversed() ?: emptyList()
                    tripAdapter = TripAdapter(trips)
                    recyclerView.adapter = tripAdapter
                } else {
                    Toast.makeText(this@HomeActivity, "Error al cargar viajes", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LastTripsResponse>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
