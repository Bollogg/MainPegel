package de.wiesenfarth.mainpegel

/*******************************************************
 * Klasse:      PegelResponse
 * Beschreibung:
 * Datenmodell für die JSON-Antwort der Pegel-API.
 */
class PegelResponse {
    // ISO-Zeitstempel, z.B. "2025-11-21T08:45:00+01:00"
    var timestamp: String? = null

    // Pegelwert in Zentimetern
    var value: Int = 0

    /**
     * Leerer Konstruktor (erforderlich für Gson/Retrofit)
     */
    constructor()

    /**
     * Bequemer Konstruktor für manuelle Erstellung
     */
    constructor(timestamp: String?, value: Int) {
        this.timestamp = timestamp
        this.value = value
    }
}
