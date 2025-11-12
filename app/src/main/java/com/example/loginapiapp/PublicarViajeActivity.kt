package com.example.loginapiapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import android.widget.AutoCompleteTextView
import java.util.Calendar
import android.app.DatePickerDialog
import android.app.TimePickerDialog


class PublicarViajeActivity : AppCompatActivity() {

    private lateinit var txtOrigen: AutoCompleteTextView
    private lateinit var txtDestino: AutoCompleteTextView
    private lateinit var txtFechaHora: EditText
    private lateinit var txtLugares: EditText
    private lateinit var txtPrecio: EditText
    private lateinit var chkEncomiendas: CheckBox
    private lateinit var txtDetalles: EditText
    private lateinit var spnTipoVehiculo: Spinner
    private lateinit var btnPublicar: Button
    private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publicar_viaje)
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

                    R.id.menu_buscar -> {
                        val intent = Intent(this, BuscarViajeActivity::class.java)
                        startActivity(intent)
                        true
                    }

                    R.id.menu_publicar -> {
                        // Ya estás en publicar viaje
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


        // Inicializar Places
        placesClient = com.google.android.libraries.places.api.Places.createClient(this)

        // Referencias a vistas
        txtOrigen = findViewById(R.id.txtOrigen)
        txtDestino = findViewById(R.id.txtDestino)
        txtFechaHora = findViewById(R.id.txtFechaHora)  // Inicialización primero
        txtFechaHora.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)

                TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                    val fechaHora = String.format(
                        "%04d-%02d-%02d %02d:%02d:00",
                        selectedYear, selectedMonth + 1, selectedDay,
                        selectedHour, selectedMinute
                    )
                    txtFechaHora.setText(fechaHora)
                }, hour, minute, true).show()

            }, year, month, day).show()
        }

        txtLugares = findViewById(R.id.txtLugares)
        txtPrecio = findViewById(R.id.txtPrecio)
        chkEncomiendas = findViewById(R.id.chkEncomiendas)
        txtDetalles = findViewById(R.id.txtDetalles)
        spnTipoVehiculo = findViewById(R.id.spnTipoVehiculo)
        btnPublicar = findViewById(R.id.btnPublicar)

        // Autocomplete para ciudades argentinas
        setupAutocomplete(txtOrigen)
        setupAutocomplete(txtDestino)

        // Spinner de tipo de vehículo
        val tipos = arrayOf("Auto", "Camioneta", "Tráfic", "Colectivo", "Moto")
        spnTipoVehiculo.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tipos)

        btnPublicar.setOnClickListener { publicarViaje() }
    }

    private fun setupAutocomplete(textView: AutoCompleteTextView) {
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line)
        textView.setAdapter(adapter)

        textView.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrBlank() && s.length > 2) {
                    val request = FindAutocompletePredictionsRequest.builder()
                        .setQuery(s.toString())
                        .setCountries("AR")
                        .setTypeFilter(TypeFilter.CITIES)
                        .build()

                    placesClient.findAutocompletePredictions(request)
                        .addOnSuccessListener { response ->
                            adapter.clear()
                            for (prediction in response.autocompletePredictions) {
                                adapter.add(prediction.getFullText(null).toString())
                            }
                            adapter.notifyDataSetChanged()
                        }
                }
            }
        })
    }

    private fun publicarViaje() {
        val origen = txtOrigen.text.toString().trim()
        val destino = txtDestino.text.toString().trim()
        val fechaHora = txtFechaHora.text.toString().trim()
        val lugares = txtLugares.text.toString().trim()
        val precio = txtPrecio.text.toString().trim()
        val permite = if (chkEncomiendas.isChecked) 1 else 0
        val detalles = txtDetalles.text.toString().trim()
        val tipoVehiculo = spnTipoVehiculo.selectedItem.toString()

        if (origen.isEmpty() || destino.isEmpty() || fechaHora.isEmpty() ||
            lugares.isEmpty() || precio.isEmpty() || tipoVehiculo.isEmpty()
        ) {
            Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // 🔹 Recuperar token guardado en SharedPreferences
        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val token = prefs.getString("token", null)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        // 🔹 Crear JSON para enviar
        val json = JSONObject().apply {
            put("origen", origen)
            put("destino", destino)
            put("fecha_hora_salida", fechaHora)
            put("lugares", lugares.toInt())
            put("precio", precio.toFloat())
            put("permite_encomiendas", permite)
            put("detalles", detalles)
            put("tipoVehiculo", tipoVehiculo)
        }

        val url = "http://192.168.1.106/subete/backend/api/viajes/crear-viajes.php"

        val request = object : JsonObjectRequest(Method.POST, url, json,
            { response ->
                if (response.optBoolean("ok")) {
                    Toast.makeText(this, "Viaje publicado con éxito!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val errorMsg = response.optString("error", "Error desconocido")
                    Toast.makeText(this, "Error: $errorMsg", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error de conexión: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf("Authorization" to "Bearer $token")
            }
        }

        Volley.newRequestQueue(this).add(request)
    }
}
