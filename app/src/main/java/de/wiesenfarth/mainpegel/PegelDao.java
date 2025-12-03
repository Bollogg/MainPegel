package de.wiesenfarth.mainpegel;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/*******************************************************
 * Programm:  PegelDao
 *
 * Beschreibung:
 *  Data Access Object (DAO) für die Pegel-Datenbank.
 *
 *  Diese Schnittstelle definiert alle Datenbankoperationen
 *  für die lokale Speicherung der Pegelwerte.
 *
 *  Funktionen:
 *   - Einfügen eines neuen Messwertes (insert)
 *   - Abrufen aller Messwerte der letzten 24 Stunden
 *   - Löschen alter Messwerte, die älter als 24 Stunden sind
 *
 *  Wird vom Room-Framework automatisch implementiert.
 *
 * @Autor:     Bollog
 * @Datum:     2025-11-20
 *******************************************************/
@Dao
public interface PegelDao {

    /**
     * Fügt einen neuen Pegel-Datensatz in die lokale Datenbank ein.
     *
     * @param pegelEntry Objekt mit Zeitstempel + Pegelwert
     */
    @Insert
    void insert(PegelEntry pegelEntry);

    /**
     * Holt alle Pegel-Einträge, deren Zeitstempel innerhalb der
     * letzten 24 Stunden liegt.
     *
     * @param time24hAgo Zeitgrenze in Millisekunden (System.currentTimeMillis - 24h)
     * @return Liste aller Pegel-Einträge ≥ time24hAgo
     */
    @Query("SELECT * FROM pegel_table WHERE timestamp >= :time24hAgo")
    List<PegelEntry> getLast24Hours(long time24hAgo);

    /**
     * Löscht alle Einträge, die älter als 24 Stunden sind.
     * Dadurch bleibt die Datenbank klein und performant.
     *
     * @param time24hAgo Zeitgrenze (alles darunter wird gelöscht)
     */
    @Query("DELETE FROM pegel_table WHERE timestamp < :time24hAgo")
    void deleteOldEntries(long time24hAgo);
}
