package de.wiesenfarth.mainpegel;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;
/*******************************************************
 * Programm:  PegelDao
 *
 * Beschreibung:
 *  ToDo.....?????
 *
 *
 * @Autor:     Bollog
 * @Datum:     2025-11-20
 *******************************************************/
@Dao
public interface PegelDao {
    @Insert
    void insert(PegelEntry pegelEntry);

    @Query("SELECT * FROM pegel_table WHERE timestamp >= :time24hAgo")
    List<PegelEntry> getLast24Hours(long time24hAgo);

    @Query("DELETE FROM pegel_table WHERE timestamp < :time24hAgo")
    void deleteOldEntries(long time24hAgo);
}