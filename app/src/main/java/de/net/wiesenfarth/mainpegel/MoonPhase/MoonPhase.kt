package de.net.wiesenfarth.mainpegel.MoonPhase

import de.net.wiesenfarth.mainpegel.R
import java.time.OffsetDateTime
import java.util.*

/*******************************************************
 * Objekt:     MoonPhase
 *
 * Beschreibung:
 * -----------------------------------------------------
 * Diese Klasse stellt Funktionen zur Berechnung und
 * Darstellung von Mondphasen bereit.
 *
 * Grundlage:
 * -----------------------------------------------------
 * Die Berechnung basiert auf einer vereinfachten
 * astronomischen Näherung des Mondzyklus
 * (~29.53 Tage).
 *
 * Verwendung:
 * -----------------------------------------------------
 * 1. Übergabe eines ISO-8601 Zeitstempels
 * 2. Berechnung der Mondphase (0–7)
 * 3. Umwandlung in String-Ressource für UI-Anzeige
 *
 * Beispiel:
 * -----------------------------------------------------
 * val phase = MoonPhase.getMoonPhase("2026-03-27T13:30+01:00")
 * val text  = getString(MoonPhase.getMoonPhaseStringRes(phase))
 *
 * @Autor:     Bollogg
 * @Datum:     2026-03-31
 *******************************************************/
object MoonPhase {

	/*******************************************************
	 * Funktion:   getMoonPhase
	 *
	 * Beschreibung:
	 * -----------------------------------------------------
	 * Berechnet die Mondphase anhand eines ISO-Zeitstempels.
	 *
	 * Ablauf:
	 * -----------------------------------------------------
	 * 1. Parsen des ISO-Strings in OffsetDateTime
	 * 2. Umwandlung in java.util.Date (Legacy API)
	 * 3. Extraktion von Jahr, Monat und Tag über Calendar
	 * 4. Berechnung der vergangenen Tage seit Referenzdatum
	 * 5. Normierung auf Mondzyklus (~29.53 Tage)
	 * 6. Umrechnung auf 8 Mondphasen (0–7)
	 *
	 * Hinweis:
	 * -----------------------------------------------------
	 * Die Berechnung ist eine Näherung und für UI-Zwecke
	 * ausreichend genau.
	 *
	 * Rückgabewerte:
	 * -----------------------------------------------------
	 * 0 = Neumond
	 * 1 = Zunehmende Sichel
	 * 2 = Erstes Viertel
	 * 3 = Zunehmender Mond
	 * 4 = Vollmond
	 * 5 = Abnehmender Mond
	 * 6 = Letztes Viertel
	 * 7 = Abnehmende Sichel
	 *
	 * @param isoTimestamp ISO-8601 Datum
	 * @return Mondphase als Integer (0–7)
	 *******************************************************/
	fun getMoonPhase(isoTimestamp: String): Int {

		// ISO → OffsetDateTime
		val odt = OffsetDateTime.parse(isoTimestamp)

		// OffsetDateTime → Date (für Calendar-Berechnung)
		val date = Date.from(odt.toInstant())

		// Calendar initialisieren
		val cal = Calendar.getInstance()
		cal.time = date

		// Datum extrahieren
		var year = cal.get(Calendar.YEAR)
		var month = cal.get(Calendar.MONTH) + 1
		val day = cal.get(Calendar.DAY_OF_MONTH)

		// Januar/Februar als Monate 13/14 des Vorjahres behandeln
		if (month < 3) {
			year--
			month += 12
		}

		month++

		// Astronomische Näherungsformel
		val c = 365.25 * year
		val e = 30.6 * month

		// Tage seit Referenzdatum
		val jd = c + e + day - 694039.09

		// Normierung auf Mondzyklus
		val jdNormalized = jd / 29.5305882

		// Bruchteil = aktuelle Phase
		val phase = jdNormalized - Math.floor(jdNormalized)

		// Umrechnung auf 8 Phasen (0–7)
		return (phase * 8).toInt() and 7
	}

	/*******************************************************
	 * Funktion:   getMoonPhaseStringRes
	 *
	 * Beschreibung:
	 * -----------------------------------------------------
	 * Liefert die passende String-Ressourcen-ID zur
	 * berechneten Mondphase.
	 *
	 * Verwendung:
	 * -----------------------------------------------------
	 * context.getString(
	 *     MoonPhase.getMoonPhaseStringRes(phase)
	 * )
	 *
	 * Vorteil:
	 * -----------------------------------------------------
	 * - Kein Context in dieser Klasse notwendig
	 * - Saubere Trennung von Logik und UI
	 *
	 * @param phase Mondphase (0–7)
	 * @return String-Ressourcen-ID
	 *******************************************************/
	fun getMoonPhaseStringRes(phase: Int): Int {
		return when (phase) {
			0 -> R.string.moon_0_new
			1 -> R.string.moon_1_waxing_crescent
			2 -> R.string.moon_2_first_quarter
			3 -> R.string.moon_3_waxing_gibbous
			4 -> R.string.moon_4_full
			5 -> R.string.moon_5_waning_gibbous
			6 -> R.string.moon_6_last_quarter
			7 -> R.string.moon_7_waning_crescent
			else -> R.string.moon_0_new
		}
	}
}