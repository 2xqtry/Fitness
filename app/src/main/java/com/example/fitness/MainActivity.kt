package com.example.fitness

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


data class Trening(
    val typAktywnosci: String,
    val dystans: Double,
    val czas: Int,
    val kalorie: Int,
    val intensywnosc: Int
)


class TreningAdapter(
    private val treningi: List<Trening>,
    private val onItemClicked: (Trening) -> Unit
) : RecyclerView.Adapter<TreningAdapter.TreningViewHolder>() {

    inner class TreningViewHolder(itemView: android.view.View) :
        RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.NazwaAktywmnosci)
        val details: TextView = itemView.findViewById(R.id.ListaRecycler)

        fun bind(trening: Trening) {
            title.text = trening.typAktywnosci
            details.text = "Dystans: ${trening.dystans} km, Kalorie: ${trening.kalorie} kcal"
            itemView.setOnClickListener { onItemClicked(trening) }
        }
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): TreningViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.trening_item, parent, false)
        return TreningViewHolder(view)
    }

    override fun onBindViewHolder(holder: TreningViewHolder, position: Int) {
        holder.bind(treningi[position])
    }

    override fun getItemCount(): Int = treningi.size
}

// MainActivity
class MainActivity : AppCompatActivity() {

    private lateinit var treningi: MutableList<Trening>
    private lateinit var adapter: TreningAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicjalizacja danych
        treningi = loadData()

        // RecyclerView setup
        val recyclerView: RecyclerView = findViewById(R.id.ListaRecycler)
        adapter = TreningAdapter(treningi) { trening -> showDetailsDialog(trening) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Obsługa formularza
        val czasEditText: EditText = findViewById(R.id.CzasEditText)
        val dystansEditText: EditText = findViewById(R.id.DystansEditText)
        val kalorieEditText: EditText = findViewById(R.id.KalorieEditText)
        val intensywnoscSeek: SeekBar = findViewById(R.id.OcenaIntensSeek)
        val aktywnosciRadioGroup: RadioGroup = findViewById(R.id.AktywnosciRadioGroup)
        val dodajButton: Button = findViewById(R.id.DodajAktynoscButton)

        dodajButton.setOnClickListener {
            val typAktywnosci = getSelectedRadioButtonText(aktywnosciRadioGroup)
            val dystans = dystansEditText.text.toString().toDoubleOrNull() ?: 0.0
            val czas = czasEditText.text.toString().toIntOrNull() ?: 0
            val kalorie = kalorieEditText.text.toString().toIntOrNull() ?: 0
            val intensywnosc = intensywnoscSeek.progress

            if (typAktywnosci.isNotEmpty() && dystans > 0 && czas > 0 && kalorie > 0) {
                val trening = Trening(typAktywnosci, dystans, czas, kalorie, intensywnosc)
                treningi.add(trening)
                adapter.notifyDataSetChanged()
                saveData()
            } else {
                Toast.makeText(this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Metoda do pobrania zaznaczonego RadioButton
    private fun getSelectedRadioButtonText(radioGroup: RadioGroup): String {
        val selectedId = radioGroup.checkedRadioButtonId
        val selectedRadioButton: RadioButton = findViewById(selectedId)
        return selectedRadioButton.text.toString()
    }

    // Dialog z detalami
    private fun showDetailsDialog(trening: Trening) {
        AlertDialog.Builder(this)
            .setTitle(trening.typAktywnosci)
            .setMessage(
                "Dystans: ${trening.dystans} km\n" +
                        "Czas: ${trening.czas} min\n" +
                        "Kalorie: ${trening.kalorie} kcal\n" +
                        "Intensywność: ${getIntensywnoscLabel(trening.intensywnosc)}"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    // Etykiety intensywności
    private fun getIntensywnoscLabel(intensywnosc: Int): String {
        return when (intensywnosc) {
            0 -> "Niska"
            1 -> "Średnia"
            2 -> "Wysoka"
            else -> "Nieznana"
        }
    }

    // Zapis danych do SharedPreferences
    private fun saveData() {
        val gson = Gson()
        val json = gson.toJson(treningi)
        val preferences: SharedPreferences =
            getSharedPreferences("TreningApp", Context.MODE_PRIVATE)
        preferences.edit().putString("treningi", json).apply()
    }

    // Odczyt danych z SharedPreferences
    private fun loadData(): MutableList<Trening> {
        val preferences: SharedPreferences =
            getSharedPreferences("TreningApp", Context.MODE_PRIVATE)
        val json = preferences.getString("treningi", null)
        val type = object : TypeToken<List<Trening>>() {}.type
        return if (json != null) Gson().fromJson(json, type) else mutableListOf()
    }
}
