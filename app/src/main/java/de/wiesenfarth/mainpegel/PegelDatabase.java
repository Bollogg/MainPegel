package de.wiesenfarth.mainpegel;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/*******************************************************
 * Programm:  PegelDatabase
 *
 * Beschreibung:
 *  Room-Datenbank für die Speicherung der Pegelwerte.
 *
 *  Diese Klasse definiert:
 *   - welche Entities (Tabellen) es gibt
 *   - welche Version die Datenbank hat
 *   - wie die Datenbank instanziiert wird (Singleton)
 *
 *  Die Datenbank speichert PegelEntry-Objekte lokal,
 *  sodass die App auch ohne Internetverbindung Daten
 *  der letzten 24 Stunden anzeigen kann.
 *
 * @Autor:     Bollog
 * @Datum:     2025-11-20
 *******************************************************/
@Database(entities = {PegelEntry.class}, version = 1, exportSchema = false)
public abstract class PegelDatabase extends RoomDatabase {

    // Singleton-Instanz der Datenbank
    // Verhindert mehrere parallele Datenbanken
    private static PegelDatabase instance;

    /**
     * Getter für das DAO, über das alle Datenbankzugriffe laufen.
     */
    public abstract PegelDao pegelDao();

    /**
     * Erstellt oder liefert die Singleton-Instanz der Datenbank.
     *
     * @param context Application-Context (nie Activity-Context!)
     * @return Datenbankinstanz
     */
    public static synchronized PegelDatabase getInstance(Context context) {

        // Nur eine Instanz erstellen
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),   // verhindert Memory-Leaks
                    PegelDatabase.class,               // welche DB-Klasse
                    "pegel_database"                   // Dateiname der DB
                )
                // Falls DB-Version sich ändert und keine Migrations definiert sind:
                // zerstört das Schema und erstellt neue DB (besser als Absturz)
                .fallbackToDestructiveMigration()
                .build();
        }

        return instance;
    }
}
