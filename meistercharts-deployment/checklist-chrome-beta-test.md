# Checklist Beta-Test Google Chrome

## Links

### Download-Link für Beta:

https://www.google.com/chrome/beta/

### Schedule

https://chromestatus.com/roadmap

### Versions-Historie:

https://en.wikipedia.org/wiki/Google_Chrome_version_history

## Installation Beta-Version

```
wget https://dl.google.com/linux/direct/google-chrome-beta_current_amd64.deb
    sudo gdebi google-chrome-beta_current_amd64.deb
```

> [!CAUTION]
> Unbedingt Version noch mal überprüfen!

## Tests in demosjs durchführen

`google-chrome-beta "https://demos.meistercharts.com/index.html?minQuality=Broken&minVisibility=DevOnly"`

Mit der Beta-Version öffnen und die Demos durchklicken.
ggf. vergleichen mit der Chrome Stable

## Tests des Lizergy-Konfigurators durchführen

Mit der Beta-Version https://spielwiese.pv.neckar.it/ öffnen und durchklicken.
ggf. vergleichen mit der Chrome Stable * insbesondere den Modul-Planner kurz ausprobieren

## Kalender-Eintrag erstellen

Im persönlichen Kalender einen Termin für den nächsten Beta-Test einstellen.
Dazu auf https://chromestatus.com/roadmap gehen und in der DEV-Spalte (ganz rechts) in der Zeile "Beta" das erste Datum plus 1 Tag verwenden.
Beispiel: bei `Beta between Oct 15 - Oct 22` sollte der Test am 16. Oktober durchgeführt werden.

## Report generieren

Der Report steht in [`meistercharts-docs/src/docs/asciidoc/meistercharts-chrome-compatibility-report.adoc`](../meistercharts-docs/src/docs/asciidoc/meistercharts-chrome-compatibility-report.adoc).

### `revdate` sowie `revnumber` setzen

Ganz oben im Header das aktuelle Datum sowie Chrome-Version setzen:

`:revdate: 2022-07-27`
`:revnumber: 109`

### Versionen aktualisieren

- Die (neuen) Versionen und Beta-/Release-Daten aktualisieren

### Kompatibilität testen

- Die Tabelle kopieren und oben für die neue Version einfügen
Achtung!
Genaue Versionsnummer für Tabellen-Titel eintragen, z. B. Version 85.0.4183.49 (Official Build) beta (64-bit)

// Machen wir aktuell nicht mehr:
//NOTE: Es gibt einen Zelle in welcher die Performance angegeben wird - bitte aktualisieren

## Report veröffentlichen

Der Report wird automatisch unter

https://docs.meistercharts.com/meistercharts-chrome-compatibility-report-small.pdf

veröffentlicht, sobald die Änderungen auf master gemergt sind.
