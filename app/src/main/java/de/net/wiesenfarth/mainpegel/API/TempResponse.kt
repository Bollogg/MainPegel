package de.net.wiesenfarth.mainpegel.API

/*******************************************************
 * Klasse:      PegelResponse
 *
 * Beschreibung:
 * Datenmodell (DTO) für einen einzelnen Messwert aus der
 * JSON-Antwort der Pegel-API.
 *
 * Jede Instanz repräsentiert genau einen Pegelstand zu
 * einem bestimmten Zeitpunkt.
 *
 * Wird von Retrofit + Gson automatisch aus der
 * API-Antwort befüllt.
 *
 * Beispiel JSON:
 * {
 *   "timestamp": "2025-11-21T08:45:00+01:00",
 *   "value": 312
 * }
 *
 * @Autor:     Bollogg
 * @Website:   wiesenfarth-net.de
 *******************************************************/
class TempResponse {

    /**
     * Zeitstempel der Messung im ISO-8601-Format.
     *
     * Beispiele:
     *  - "2025-11-21T08:45:00+01:00"
     *  - "2025-11-21T07:45:00Z"
     *
     * Nullable, da die API theoretisch auch
     * unvollständige Datensätze liefern kann.
     *
     * @JvmField:
     *  - verhindert Getter/Setter
     *  - Gson greift direkt auf das Feld zu
     */
    @JvmField
    var timestamp: String = "null"

    /**
     * Temperatur in °C.
     *
     * Default = 0.0F, falls der Wert in der
     * JSON-Antwort fehlt.
     */
    @JvmField
    var value: Float = 0.0F

    /**
     * Leerer Standard-Konstruktor.
     *
     * Erforderlich für:
     *  - Gson
     *  - Retrofit
     *
     * Die Bibliotheken erzeugen das Objekt zuerst leer
     * und setzen die Felder anschließend per Reflection.
     */
    constructor()

    /**
     * Komfort-Konstruktor zur manuellen Erstellung
     * (z. B. für Tests oder Debugging).
     *
     * @param timestamp Zeitstempel der Messung
     * @param value     Pegelwert in Zentimetern
     */
    constructor(timestamp: String, value: Float) {
        this.timestamp = timestamp
        this.value = value
    }
}