package de.net.wiesenfarth.mainpegel

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import de.net.wiesenfarth.mainpegel.databinding.DatenschutzerklaerungActivityBinding

class DatenschutzerklaerungActivity : AppCompatActivity() {

	private lateinit var binding: DatenschutzerklaerungActivityBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = DatenschutzerklaerungActivityBinding.inflate(layoutInflater)
		setContentView(binding.root)

		setSupportActionBar(binding.toolbar)
		// Datenschutzerklärung
		binding.toolbar.title = getString(R.string.menu_privacy_policy)
		supportActionBar?.setDisplayHomeAsUpEnabled(true)

		val text = assets.open("datenschutzerklaerung.txt")
			.bufferedReader()
			.use { it.readText() }

		binding.privacyText.text = text
	}

	// --------------------------------------------------------
	// Reaktion auf Toolbar-Aktionen (z. B. Zurück-Pfeil)
	// --------------------------------------------------------
	override fun onOptionsItemSelected(item: MenuItem): Boolean {

		// when ist übersichtlicher als if/else
		return when (item.itemId) {

			// Klick auf den "Up"-Button (Pfeil links oben)
			android.R.id.home -> {
				finish() // Activity beenden → zurück zur vorherigen
				true     // Event wurde verarbeitet
			}

			// Alle anderen Menü-Events an die Basisklasse weiterreichen
			else -> super.onOptionsItemSelected(item)
		}
	}
}