package de.wiesenfarth.mainpegel

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase

/*******************************************************
 * Programm:  PegelDatabase
 *
 * Beschreibung:
 * Room-Datenbank für die Speicherung der Pegelwerte.
 *
 * Diese Klasse definiert:
 * - welche Entities (Tabellen) es gibt
 * - welche Version die Datenbank hat
 * - wie die Datenbank instanziiert wird (Singleton)
 *
 * Die Datenbank speichert PegelEntry-Objekte lokal,
 * sodass die App auch ohne Internetverbindung Daten
 * der letzten 24 Stunden anzeigen kann.
 *
 * @Autor:     Bollog
 * @Datum:     2025-11-20
 */
@Database(entities = [PegelEntry::class], version = 1, exportSchema = false)
abstract class PegelDatabase : RoomDatabase() {
    /**
     * Getter für das DAO, über das alle Datenbankzugriffe laufen.
     */
    abstract fun pegelDao(): PegelDao

    companion object {
        // Singleton-Instanz der Datenbank
        // Verhindert mehrere parallele Datenbanken
        private var instance: PegelDatabase? = null

        /**
         * Erstellt oder liefert die Singleton-Instanz der Datenbank.
         *
         * @param context Application-Context (nie Activity-Context!)
         * @return Datenbankinstanz
         */
        @Synchronized
        fun getInstance(context: Context): PegelDatabase {
            // Nur eine Instanz erstellen

            if (instance == null) {
                instance = databaseBuilder<PegelDatabase>(
                    context.getApplicationContext(),  // verhindert Memory-Leaks
                    PegelDatabase::class.java,  // welche DB-Klasse
                    "pegel_database" // Dateiname der DB
                ) // Falls DB-Version sich ändert und keine Migrations definiert sind:
                    // zerstört das Schema und erstellt neue DB (besser als Absturz)
                    .fallbackToDestructiveMigration()
                    .build()
            }

            return instance!!
        }
    }
}
