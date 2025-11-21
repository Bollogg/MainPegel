package de.wiesenfarth.mainpegel;

/*******************************************************
 * Programm:  PegelResponse
 *
 * Beschreibung:
 *  API Daten-Struktur in Json Format
 *  Getter und Setter
 *
 *
 * @Autor:     Bollog
 * @Datum:     2025-11-21
 *******************************************************/

public class PegelResponse {
    private String timestamp;
    private int value;

    public PegelResponse() {}

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
