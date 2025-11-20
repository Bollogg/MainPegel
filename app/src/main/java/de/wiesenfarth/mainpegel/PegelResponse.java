package de.wiesenfarth.mainpegel;

/*******************************************************
 * Programm:  PegelResponse
 *
 * Beschreibung:
 *  getter von API Daten
 *
 *
 * @Autor:     Bollog
 * @Datum:     2025-11-20
 *******************************************************/

public class PegelResponse {
    private String timestamp;
    private double value;
    private String stateMnwMhw;
    private String stateNswHsw;

    public String getTimestamp() { return timestamp; }
    public double getValue() { return value; }
    public String getStateMnwMhw() { return stateMnwMhw; }
    public String getStateNswHsw() { return stateNswHsw; }

    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public void setValue(double value) { this.value = value; }
    public void setStateMnwMhw(String stateMnwMhw) { this.stateMnwMhw = stateMnwMhw; }
    public void setStateNswHsw(String stateNswHsw) { this.stateNswHsw = stateNswHsw; }
}
