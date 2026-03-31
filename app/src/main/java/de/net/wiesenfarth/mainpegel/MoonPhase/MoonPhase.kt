package de.net.wiesenfarth.mainpegel.MoonPhaseCalc

// Wir geben der Library-Klasse einen eindeutigen Namen (Alias)
import org.shredzone.commons.suncalc.MoonPhase as SunCalcMoonPhase
import java.time.ZonedDateTime
import org.shredzone.commons.suncalc.MoonPhase

import java.util.*


class MoonPhaseHelper {


	fun getMoonPhase(date: Date = Date()): Int {
		val cal = Calendar.getInstance()
		cal.time = date

		var year = cal.get(Calendar.YEAR)
		var month = cal.get(Calendar.MONTH) + 1
		val day = cal.get(Calendar.DAY_OF_MONTH)

		if (month < 3) {
			year--
			month += 12
		}

		month++

		val c = 365.25 * year
		val e = 30.6 * month
		val jd = c + e + day - 694039.09  // Tage seit Referenz
		val jdNormalized = jd / 29.5305882  // Mondzyklus
		val phase = jdNormalized - Math.floor(jdNormalized)

		return (phase * 8).toInt() and 7
	}

	fun getMoonPhaseName(phase: Int): String {
		return when (phase) {
			0 -> "🌑"
			1 -> "🌒"
			2 -> "🌓"
			3 -> "🌔"
			4 -> "🌕"
			5 -> "🌖"
			6 -> "🌗"
			7 -> "🌘"
			else -> "🌑"
		}
	}
}
