package de.net.wiesenfarth.mainpegel

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar



/*******************************************************
 * Activity:   InfoActivity
 *
 * Beschreibung:
 * Zeigt eine Informationsseite (z. B. App-Infos,
 * Versionsdaten oder rechtliche Hinweise) an.
 *
 * Die Activity verwendet eine eigene Toolbar mit
 * Zurück-Pfeil (Up-Navigation), um zur vorherigen
 * Activity zurückzukehren.
 *
 * @Autor:     Bollogg
 * @Datum:     2025-11-20
 *******************************************************/
class InfoActivity : AppCompatActivity() {

  /**
   * Wird beim Erstellen der Activity aufgerufen.
   *
   * Initialisiert:
   * - das Layout
   * - die Toolbar als ActionBar
   * - den Titel sowie den Zurück-Button
   */
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Layout für die Info-Seite setzen
    setContentView(R.layout.info_activity)

    // --------------------------------------------------------
    // Toolbar einrichten
    // --------------------------------------------------------

    // Toolbar aus dem Layout laden
    val toolbar = findViewById<Toolbar>(R.id.toolbar)

    // Versionsname schreiben
    val txtVersionName = findViewById<TextView>(R.id.txtVersionName)
    txtVersionName.text = "Version: ${de.net.wiesenfarth.mainpegel.BuildConfig.VERSION_NAME}"
    // Toolbar als ActionBar verwenden
    setSupportActionBar(toolbar)

    // ActionBar konfigurieren (Titel + Zurück-Pfeil)
    supportActionBar?.let {
      it.title = getString(R.string.menu_info)
      it.setDisplayHomeAsUpEnabled(true)
    }
  }

  /*
   * Alternative Möglichkeit (kommentiert):
   *
   * Moderne Back-Navigation über den
   * OnBackPressedDispatcher.
   *
   * Wird hier nicht benötigt, da die Standard-
   * Navigation per finish() ausreichend ist.
   */

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
