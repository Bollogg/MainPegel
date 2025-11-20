package de.wiesenfarth.mainpegel;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pegel_table")
public class PegelEntry {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public long timestamp;  // Zeitstempel für den Wert
    public double value;    // Pegelstand in cm

    public PegelEntry(long timestamp, double value) {
        this.timestamp = timestamp;
        this.value = value;
    }
}