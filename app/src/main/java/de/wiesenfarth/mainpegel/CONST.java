package de.wiesenfarth.mainpegel;

/*******************************************************
 * Programm:  CONST
 *
 * Beschreibung:
 *  Sammlung aller zentralen Konstanten für Pegel-Messstellen,
 *  Schwellwerte und graphische Einstellungen.
 *
 * Autor:     Bollog
 * Datum:     2025-11-17
 *******************************************************/
public class CONST {

    // ----------------------------------------------------
    // GUIDs der Pegelmessstellen (fest definierte IDs)
    // ----------------------------------------------------
    public static final String RAUNHEIM                = "db1684c1-7ffc-4e8a-b8cf-8240a0d03519";
    public static final String FRANKFURT_OSTHAFEN      = "66ff3eb4-513b-478b-abd2-2f5126ea66fd";
    public static final String HANAU_BRUECKE_DFH       = "07374faf-2039-4430-ae6d-adc0e0784c4b";
    public static final String AUHEIM_BRUECKE_DFH      = "da453ad0-5f1d-417c-baa3-74ae297f0b7a";
    public static final String KROTZENBURG             = "27eed51b-c0a4-417e-926b-bb4194bfb341";
    public static final String MAINFLINGEN             = "4627475d-ccda-4d53-8f13-28527c49eaf5";
    public static final String KLEINOSTHEIM_WUK        = "3ef81fc0-33dc-4f67-8bb8-3f66975292d5";
    public static final String OBERNAU                 = "3c7cfb10-c866-404b-b11c-0d79986f865a";
    public static final String KLEINHEUBACH            = "355b02d2-c578-46d9-a56b-8046d470cb95";
    public static final String FAULBACH                = "a919f57f-8378-42d8-82f8-b87eaf008641";
    public static final String WERTHEIM                = "0e065a22-9a0b-4f1d-b813-22fe6321bb1a";
    public static final String STEINBACH               = "1ed983c3-114c-4fcc-a1db-61d336cf045f";
    public static final String WUERZBURG               = "915d76e1-3bf9-4e37-9a9a-4d144cd771cc";
    public static final String ASTHEIM                 = "3de69bf8-dcbb-4afb-a15b-a8683a6a689c";
    public static final String SCHWEINFURT_NEUER_HAFEN = "42ecae60-eeb3-4b41-9721-46b3f12d04b8";
    public static final String TRUNSTADT               = "a77aad00-caa0-44a2-95cb-8afd9c4ff00c";

    // Main-Donau-Kanal
    public static final String BAMBERG                 = "ff02f181-491c-4925-ad13-07edd2ddb3f1";
    public static final String RIEDENBURG_UP           = "4a69e82e-97a3-4573-8aeb-b695c1eaa0b1";

    // ----------------------------------------------------
    // Maximal zulässige Anzahl Stellen für Schwellwert-Eingabe
    // (z.B. 4 → erlaubt 0000 bis 9999)
    // ----------------------------------------------------
    public static final int WAVE_THERESHOLD_MAX        = 4;

    // ----------------------------------------------------
    // Stundenbereiche für die Darstellung im Pegelgraphen.
    // Jede Konstante repräsentiert die Anzahl Stunden.
    // (1 bis 29 Stunden auswählbar)
    // ----------------------------------------------------
    public static final int HOURS_1  = 1;
    public static final int HOURS_2  = 2;
    public static final int HOURS_3  = 3;
    public static final int HOURS_4  = 4;
    public static final int HOURS_5  = 5;
    public static final int HOURS_6  = 6;
    public static final int HOURS_7  = 7;
    public static final int HOURS_8  = 8;
    public static final int HOURS_9  = 9;
    public static final int HOURS_10 = 10;
    public static final int HOURS_11 = 11;
    public static final int HOURS_12 = 12;
    public static final int HOURS_13 = 13;
    public static final int HOURS_14 = 14;
    public static final int HOURS_15 = 15;
    public static final int HOURS_16 = 16;
    public static final int HOURS_17 = 17;
    public static final int HOURS_18 = 18;
    public static final int HOURS_19 = 19;
    public static final int HOURS_20 = 20;
    public static final int HOURS_21 = 21;
    public static final int HOURS_22 = 22;
    public static final int HOURS_23 = 23;
    public static final int HOURS_24 = 24;
    public static final int HOURS_25 = 25;
    public static final int HOURS_26 = 26;
    public static final int HOURS_27 = 27;
    public static final int HOURS_28 = 28;
    public static final int HOURS_29 = 29;

}
