package de.wiesenfarth.mainpegel;

/*******************************************************
 * Klasse:      PegelResponse
 * Beschreibung:
 *   Datenmodell für die JSON-Antwort der Pegel-API.
 *******************************************************/
public class PegelResponse {

  // ISO-Zeitstempel, z.B. "2025-11-21T08:45:00+01:00"
  private String timestamp;

  // Pegelwert in Zentimetern
  private int value;

  /**
   * Leerer Konstruktor (erforderlich für Gson/Retrofit)
   */
  public PegelResponse() {
  }

  /**
   * Bequemer Konstruktor für manuelle Erstellung
   */
  public PegelResponse(String timestamp, int value) {
    this.timestamp = timestamp;
    this.value = value;
  }

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
