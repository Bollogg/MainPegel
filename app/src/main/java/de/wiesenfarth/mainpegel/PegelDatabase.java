package de.wiesenfarth.mainpegel;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
/*******************************************************
 * Programm:  PegelDatabase
 *
 * Beschreibung:
 * Datenbank für Pegel
 *
 *
 * @Autor:     Bollog
 * @Datum:     2025-11-20
 *******************************************************/
@Database(entities = {PegelEntry.class}, version = 1, exportSchema = false)
public abstract class PegelDatabase extends RoomDatabase {
    private static PegelDatabase instance;

    public abstract PegelDao pegelDao();

    public static synchronized PegelDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            PegelDatabase.class, "pegel_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}