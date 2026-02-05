# MainPegel
Liest den Pegelstand von der angegebenen Messstelle aus.
Bei Überschreitung des Pegelstand um x cm wird ein Alarm ausgelöst

<p align="center">
  <img src="images/MainPegelApp.png" width="400">
</p>

## Installation
Wichtige Informationen!

### Erinnerungsfunktion
Damit der Pegel gelesen werden kann muss in der APP die Erinnerungsfunktion erlaubt werden!
<p align="center">
  <img src="images/MainPegelWeckerUndErinnerungen.png" width="400">
</p>

Es wird im Hintergrund ein Wecker gestellt um alle 15, 30, 45,... Minuten den Pegelwert
auszulesen.

### Benachritigungsalarme
Um die Pegelwarnung zu aktivieren ist es notwendig, dass die APP Benachrichtigungen senden darf
<p align="center">
  <img src="images/MainPegelBenachrichtigungen.png" width="400">
</p>

- Vibrationsalarm
- Benachrichtigungsalarm

## Einstellungen
Es können folgende Einstellungen geändert werden.

<p align="center">
  <img src="images/MainPegelEinstellungen.png" width="400">
</p>

- Pegelmessstelle (Ort)
- Lesen des Pegels alle x Minuten
- Wellenerkennung bei Steigendem Pegel in cm
    Bei Überschreitung wird der Alarm ausgelöst
- Anzeige des Messzeitraums als Grafik
- Alarme werden bei überschreiten der Welle ausgelöst
    - Vibrationsalarm
    - Benachrichtigungsalarm


## Widgets
Anzeige des Pegels als Grafik und aktueller Pegelmesswert mit der Uhrzeit des zuletzt gemessenen Pegels  

<p align="center">
  <img src="images/MainPegelWidgets.png" width="400">
</p>

## Pegeldaten
Die Pegeldaten werden von folgender API bezogen.

https://www.pegelonline.wsv.de/


## Android Version

- Minimum SDK 32
- Kotlin V2.3.0
- JavaVersion VERSION_17

## Lizenz

Apache License Version 2.0