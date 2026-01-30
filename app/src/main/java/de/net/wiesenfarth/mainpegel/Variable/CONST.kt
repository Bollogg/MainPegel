package de.net.wiesenfarth.mainpegel.Variable

/*******************************************************
 * Programm:  CONST
 *
 * Beschreibung:
 * Sammlung aller zentralen Konstanten für Pegel-Messstellen,
 * Schwellwerte und graphische Einstellungen.
 *
 * Autor:     Bollogg
 * Datum:     2025-11-17
 *******************************************************/
object CONST {
	// ----------------------------------------------------
  // Versions Code
  // ----------------------------------------------------
  const val APP_VERSION_NAME: String = "Oettinger V2025.12.04"
  const val APP_VERSION_CODE: Int = 20251204

  // ----------------------------------------------------
  // GUIDs der Pegelmessstellen (fest definierte IDs)
  // ----------------------------------------------------
  const val RAUNHEIM: String = "db1684c1-7ffc-4e8a-b8cf-8240a0d03519"
  const val FRANKFURT_OSTHAFEN: String = "66ff3eb4-513b-478b-abd2-2f5126ea66fd"
  const val HANAU_BRUECKE_DFH: String = "07374faf-2039-4430-ae6d-adc0e0784c4b"
  const val AUHEIM_BRUECKE_DFH: String = "da453ad0-5f1d-417c-baa3-74ae297f0b7a"
  const val KROTZENBURG: String = "27eed51b-c0a4-417e-926b-bb4194bfb341"
  const val MAINFLINGEN: String = "4627475d-ccda-4d53-8f13-28527c49eaf5"
  const val KLEINOSTHEIM_WUK: String = "3ef81fc0-33dc-4f67-8bb8-3f66975292d5"
  const val OBERNAU: String = "3c7cfb10-c866-404b-b11c-0d79986f865a"
  const val KLEINHEUBACH: String = "355b02d2-c578-46d9-a56b-8046d470cb95"
  const val FAULBACH: String = "a919f57f-8378-42d8-82f8-b87eaf008641"
  const val WERTHEIM: String = "0e065a22-9a0b-4f1d-b813-22fe6321bb1a"
  const val STEINBACH: String = "1ed983c3-114c-4fcc-a1db-61d336cf045f"
  const val WUERZBURG: String = "915d76e1-3bf9-4e37-9a9a-4d144cd771cc"
  const val ASTHEIM: String = "3de69bf8-dcbb-4afb-a15b-a8683a6a689c"
  const val SCHWEINFURT_NEUER_HAFEN: String = "42ecae60-eeb3-4b41-9721-46b3f12d04b8"
  const val TRUNSTADT: String = "a77aad00-caa0-44a2-95cb-8afd9c4ff00c"

  // Main-Donau-Kanal
  const val BAMBERG: String = "ff02f181-491c-4925-ad13-07edd2ddb3f1"
  const val RIEDENBURG_UP: String = "4a69e82e-97a3-4573-8aeb-b695c1eaa0b1"

  // ----------------------------------------------------
  // Maximal zulässige Anzahl Stellen für Schwellwert-Eingabe
  // (z.B. 4 → erlaubt 0000 bis 9999)
  // ----------------------------------------------------
  const val WAVE_THERESHOLD_MAX: Int = 4

  // ----------------------------------------------------
  // Stundenbereiche für die Darstellung im Pegelgraphen.
  // Jede Konstante repräsentiert die Anzahl Stunden.
  // (1 bis 29 Stunden auswählbar)
  // ----------------------------------------------------
  const val HOURS_1: Int = 1
  const val HOURS_2: Int = 2
  const val HOURS_3: Int = 3
  const val HOURS_4: Int = 4
  const val HOURS_5: Int = 5
  const val HOURS_6: Int = 6
  const val HOURS_7: Int = 7
  const val HOURS_8: Int = 8
  const val HOURS_9: Int = 9
  const val HOURS_10: Int = 10
  const val HOURS_11: Int = 11
  const val HOURS_12: Int = 12
  const val HOURS_13: Int = 13
  const val HOURS_14: Int = 14
  const val HOURS_15: Int = 15
  const val HOURS_16: Int = 16
  const val HOURS_17: Int = 17
  const val HOURS_18: Int = 18
  const val HOURS_19: Int = 19
  const val HOURS_20: Int = 20
  const val HOURS_21: Int = 21
  const val HOURS_22: Int = 22
  const val HOURS_23: Int = 23
  const val HOURS_24: Int = 24
  const val HOURS_25: Int = 25
  const val HOURS_26: Int = 26
  const val HOURS_27: Int = 27
  const val HOURS_28: Int = 28
  const val HOURS_29: Int = 29
}