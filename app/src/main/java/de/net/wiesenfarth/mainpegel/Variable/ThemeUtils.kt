/*******************************************************
 * Programm:  ThemeHelper
 *
 * Beschreibung:
 * Hilfsfunktion zum Auslesen von Farbwerten aus dem aktuell
 * gesetzten App-Theme. Ermöglicht den Zugriff auf Attribute
 * (z.B. ?attr/colorPrimary), unabhängig davon, ob diese direkt
 * als Farbwert oder als Ressourcenreferenz definiert sind.
 *
 * Verwendung:
 * val color = context.getThemeColor(R.attr.colorPrimary)
 *
 * Besonderheiten:
 * - Unterstützt direkte Farbwerte und Referenzen (@color/...)
 * - Wirft eine Exception, wenn das Attribut nicht existiert
 *
 * Autor:     Bollogg
 * Datum:     2026-03-25
 *******************************************************/
package de.net.wiesenfarth.mainpegel.Variable

import android.content.Context
import android.util.TypedValue

fun Context.getThemeColor(attr: Int): Int {
	val typedValue = TypedValue()
	val found = theme.resolveAttribute(attr, typedValue, true)

	if (!found) {
		throw IllegalArgumentException("Attr not found: $attr")
	}

	return if (typedValue.resourceId != 0) {
		getColor(typedValue.resourceId)
	} else {
		typedValue.data
	}
}


/*******************************************************
 * Funktion:  getThemeColor (mit Fallback)
 *
 * Beschreibung:
 * Erweiterte Variante der Farb-Abfrage. Falls das gewünschte
 * Attribut im Theme nicht vorhanden ist, wird ein definierter
 * Fallback-Wert zurückgegeben, anstatt eine Exception zu werfen.
 *
 * Verwendung:
 * val color = context.getThemeColor(R.attr.colorPrimary, Color.RED)
 *
 * Vorteile:
 * - Verhindert Abstürze bei fehlenden Theme-Attributen
 * - Ideal für Widgets oder optionale Themes
 *
 *******************************************************/
// ToDo: für Widget neu fallback
/*
fun Context.getThemeColor(attr: Int, fallback: Int): Int {
    val typedValue = TypedValue()
    val found = theme.resolveAttribute(attr, typedValue, true)

    if (!found) return fallback

    return if (typedValue.resourceId != 0) {
        getColor(typedValue.resourceId)
    } else {
        typedValue.data
    }
}
*/