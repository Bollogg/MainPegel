package de.wiesenfarth.mainpegel;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/*******************************************************
 * Programm:  PegelEntry
 *
 * Beschreibung:
 *  Repräsentiert einen einzelnen gespeicherten Pegelwert
 *  in der lokalen Room-Datenbank.
 *
 *  Diese Klasse definiert den Aufbau der Tabelle
 *  "pegel_table" mit:
 *    - einer automatisch generierten ID
 *    - einem Zeitstempel (Unix-Millis)
 *    - dem Pegelwert in cm
 *
 *  Die Einträge werden genutzt, um
 *    • Pegelverläufe offline zu speichern
 *    • Daten der letzten 24h zu halten (Cleanup durch DAO)
 *    • Trends und Warnungen zu analysieren
 *
 * @Autor:     Bollog
 * @Datum:     2025-11-20
 *******************************************************/
@Entity(tableName = "pegel_table")
public class PegelEntry {

    // Primärschlüssel, automatisch fortlaufend generiert
    @PrimaryKey(autoGenerate = true)
    public int id;

    // Zeitstempel in Millisekunden (System.currentTimeMillis)
    public long timestamp;

    // Gemessener Pegelwert in Zentimetern
    public double value;

    /**
     * Konstruktor für einen neuen Pegel-Datensatz.
     *
     * @param timestamp Zeitpunkt der Messung (Millis)
     * @param value     Pegelstand in cm
     */
    public PegelEntry(long timestamp, double value) {
        this.timestamp = timestamp;
        this.value = value;
    }
}
