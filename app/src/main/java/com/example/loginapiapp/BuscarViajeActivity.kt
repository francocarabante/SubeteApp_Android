package com.example.loginapiapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.PopupMenu
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import java.util.Calendar

class BuscarViajeActivity : AppCompatActivity() {
    private lateinit var recyclerViewViajes: RecyclerView
    private lateinit var adapter: ViajeAdapter
    private lateinit var txtOrigen: AutoCompleteTextView
    private lateinit var txtDestino: AutoCompleteTextView
    private lateinit var txtFecha: EditText
    private lateinit var btnBuscar: Button
    private val listaViajes = mutableListOf<Viaje>()
    private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buscar_viajes) // usa el nombre real de tu layout
        // 🔹 Configurar botón del menú del header
        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)
        btnMenu.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.menu_main, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_inicio -> {
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        true
                    }

                    R.id.menu_publicar -> {
                        // Ya estás en buscar viaje
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


        // === Inicializar Google Places ===
        placesClient = com.google.android.libraries.places.api.Places.createClient(this)

        recyclerViewViajes = findViewById(R.id.recyclerViewViajes)
        txtOrigen = findViewById(R.id.txtOrigen)
        txtDestino = findViewById(R.id.txtDestino)
        txtFecha = findViewById(R.id.txtFecha)
        txtFecha.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                // Solo la fecha, sin hora
                val fecha = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                txtFecha.setText(fecha)
            }, year, month, day).show()
        }


        btnBuscar = findViewById(R.id.btnBuscar)

        recyclerViewViajes.layoutManager = LinearLayoutManager(this)
        adapter = ViajeAdapter(listaViajes)
        recyclerViewViajes.adapter = adapter

        // Activar autocompletado
        setupAutocomplete(txtOrigen)
        setupAutocomplete(txtDestino)

        // Cargar todos los viajes al iniciar
        cargarViajes()

        btnBuscar.setOnClickListener { buscarViajes() }
    }

    private fun setupAutocomplete(textView: AutoCompleteTextView) {
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line)
        textView.setAdapter(adapter)

        textView.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.length > 2) {
                    val request = FindAutocompletePredictionsRequest.builder()
                        .setQuery(s.toString())
                        .setCountries("AR")                // 🔹 Solo Argentina
                        .setTypeFilter(com.google.android.libraries.places.api.model.TypeFilter.CITIES) // 🔹 Solo ciudades
                        .build()

                    placesClient.findAutocompletePredictions(request)
                        .addOnSuccessListener { response ->
                            adapter.clear()
                            for (prediction in response.autocompletePredictions) {
                                adapter.add(prediction.getFullText(null).toString())
                            }
                            adapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener {
                            // log o Toast opcional
                        }
                }
            }
        })
    }


    private fun cargarViajes() {
        val url = "http://192.168.1.106/subete/backend/api/viajes/buscar-viajes.php?limit=10"
        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                listaViajes.clear()
                val results: JSONArray = response.optJSONArray("results") ?: JSONArray()
                for (i in 0 until results.length()) {
                    val v = results.getJSONObject(i)
                    val viaje = Viaje(
                        ID_Viaje = v.getInt("ID_Viaje"),
                        ID_Usuario = v.optInt("ID_Usuario"),
                        Origen = v.optString("Origen"),
                        Destino = v.optString("Destino"),
                        Fecha_Hora_Salida = v.optString("Fecha_Hora_Salida"),
                        Lugares_Disponibles = v.optString("Lugares_Disponibles").toIntOrNull() ?: 0,
                        Precio = v.optString("Precio").toFloatOrNull() ?: 0f,
                        Conductor_Nombre = v.optString("Conductor_Nombre"),
                        Conductor_Apellido = v.optString("Conductor_Apellido"),
                        Conductor_Telefono = v.optString("Conductor_Telefono"),
                        Permite_Encomiendas = v.optString("Permite_Encomiendas").toIntOrNull() ?: 0,
                        Detalles = v.optString("Detalles"),
                        Estado = v.optString("Estado")
                    )
                    listaViajes.add(viaje)
                }
                adapter.notifyDataSetChanged()
            },
            { error ->
                Toast.makeText(this, "Error al cargar viajes: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun buscarViajes() {
        val origen = txtOrigen.text.toString().trim()
        val destino = txtDestino.text.toString().trim()
        val fecha = txtFecha.text.toString().trim()

        val url = "http://192.168.1.106/subete/backend/api/viajes/buscar-viajes.php?origen=$origen&destino=$destino&fecha=$fecha"
        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                listaViajes.clear()
                val results: JSONArray = response.optJSONArray("results") ?: JSONArray()
                for (i in 0 until results.length()) {
                    val v = results.getJSONObject(i)
                    val viaje = Viaje(
                        ID_Viaje = v.getInt("ID_Viaje"),
                        ID_Usuario = v.optInt("ID_Usuario"),
                        Origen = v.optString("Origen"),
                        Destino = v.optString("Destino"),
                        Fecha_Hora_Salida = v.optString("Fecha_Hora_Salida"),
                        Lugares_Disponibles = v.optString("Lugares_Disponibles").toIntOrNull() ?: 0,
                        Precio = v.optString("Precio").toFloatOrNull() ?: 0f,
                        Conductor_Nombre = v.optString("Conductor_Nombre"),
                        Conductor_Apellido = v.optString("Conductor_Apellido"),
                        Conductor_Telefono = v.optString("Conductor_Telefono"),
                        Permite_Encomiendas = v.optString("Permite_Encomiendas").toIntOrNull() ?: 0,
                        Detalles = v.optString("Detalles"),
                        Estado = v.optString("Estado")
                    )
                    listaViajes.add(viaje)
                }
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Viajes encontrados: ${listaViajes.size}", Toast.LENGTH_SHORT).show()
            },
            { error ->
                Toast.makeText(this, "Error al buscar viajes: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }
}
