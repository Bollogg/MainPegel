package de.net.wiesenfarth.mainpegel

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.net.wiesenfarth.mainpegel.DataBase.RowData
import de.net.wiesenfarth.mainpegel.DataBase.WaterAdapter
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/*******************************************************
 * Klasse:     WaterLevelActivity
 *
 * Beschreibung:
 * -----------------------------------------------------
 * Diese Activity zeigt eine scrollbare Tabelle mit
 * Pegel- und Temperaturdaten an.
 *
 * Datenquelle:
 * -----------------------------------------------------
 * Die Daten werden NICHT direkt von der API geladen,
 * sondern aus dem lokalen Cache (SharedPreferences),
 * der von PegelLogic befüllt wird.
 *
 * Architektur:
 * -----------------------------------------------------
 * PegelLogic → SharedPreferences (Cache) → UI (RecyclerView)
 *
 * Darstellung:
 * -----------------------------------------------------
 * • RecyclerView für performante Anzeige
 * • Neuester Messwert steht oben (reverse)
 * • Anzeige enthält:
 *      - Datum + Uhrzeit
 *      - Pegelstand (cm)
 *      - Temperatur (°C)
 *
 * Funktionen:
 * -----------------------------------------------------
 * • Toolbar mit Zurück-Button
 * • Umwandlung von API-Zeitstempeln in lokales Format
 * • Robuste Fehlerbehandlung bei ungültigen Daten
 *
 * @Autor:     Bollogg
 * @Datum:     2026-03-27
 *******************************************************/
class WaterLevelActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Layout laden
		setContentView(R.layout.activity_water_level)

		// --------------------------------------------------------
		// Toolbar einrichten
		// --------------------------------------------------------
		val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
		setSupportActionBar(toolbar)

		supportActionBar?.title = "Wasserstände"
		supportActionBar?.setDisplayHomeAsUpEnabled(true)

		// --------------------------------------------------------
		// RecyclerView prüfen (Debug-Sicherheit)
		// --------------------------------------------------------
		val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

		if (recyclerView == null) {
			throw RuntimeException("RecyclerView nicht gefunden → Layout Problem!")
		}

		// Daten laden und anzeigen
		loadData()
	}

	// --------------------------------------------------------
	// Toolbar-Zurück-Button (Pfeil oben links)
	// --------------------------------------------------------
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			android.R.id.home -> {
				finish()
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}

	/**
	 * Formatiert einen API-Zeitstempel in ein lesbares Datum.
	 *
	 * Eingabe:
	 * ------------------------------------------------
	 * ISO-8601 Zeitstempel (z.B. 2026-03-27T12:30:00Z)
	 *
	 * Ausgabe:
	 * ------------------------------------------------
	 * Format: dd.MM.yyyy HH:mm (z.B. 27.03.2026 13:30)
	 *
	 * @param apiTime Zeitstempel von der API
	 * @return formatierter String oder "--" bei Fehler
	 */
	private fun formatDateTime(apiTime: String?): String {
		return try {
			val odt = OffsetDateTime.parse(apiTime)
			val zdt = odt.atZoneSameInstant(ZoneId.systemDefault())

			DateTimeFormatter
				.ofPattern("dd.MM.yyyy HH:mm", Locale.GERMANY)
				.format(zdt)

		} catch (e: Exception) {
			"--"
		}
	}

	/**
	 * Lädt Messdaten aus dem Cache und befüllt die Tabelle.
	 *
	 * Ablauf:
	 * ------------------------------------------------
	 * 1. Anzahl gespeicherter Werte lesen
	 * 2. Einzelwerte (Pegel, Zeit, Temperatur) auslesen
	 * 3. Zeitstempel formatieren
	 * 4. Liste umdrehen (neueste Werte zuerst)
	 * 5. RecyclerView mit Adapter befüllen
	 */
	private fun loadData() {

		val prefs = getSharedPreferences("pegel_cache", MODE_PRIVATE)

		val count = prefs.getInt("count", 0)

		val list = mutableListOf<RowData>()

		for (i in 0 until count) {

			val value = prefs.getInt("value_$i", 0)

			// Original-Zeitstempel aus Cache
			val timestamp = prefs.getString("timestamp_$i", null)

			// Formatierte Anzeige (Datum + Zeit)
			val dateTime = formatDateTime(timestamp)

			// Temperatur (optional)
			val temp = prefs.getFloat("temp_$i", Float.NaN)

			list.add(
				RowData(
					time = dateTime,
					value = value,
					temp = temp
				)
			)
		}

		// Neueste Werte zuerst anzeigen
		list.reverse()

		val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
		recyclerView.layoutManager = LinearLayoutManager(this)
		recyclerView.adapter = WaterAdapter(list)
	}
}