# How to check boxing for providers

## Hintergrund

Wir müssen vermeiden, dass die Inline Classes versehentlich geboxt werden.
Das ist für die Performance dramatisch - der Speicherverbrauch + GC belasten das System ansonsten enorm.

## Grund-Idee

Wir haben Tests erstellt, diese werden kompiliert.
Danach kann der generierte Source-Code analysiert werden.

## Tests

Es gibt einige Tests im Package `it.neckar.open.provider.boxing` welche für das Boxing genutzt werden können.
Die Tests gibt es jeweils für JS und JVM - in den entsprechenden Source Folders.

- it.neckar.open.provider.boxing.IndexProviderBoxingTest
- it.neckar.open.provider.boxing.ProviderBoxingTest

## Vorgehen JS (Linux)

Script `show-js-boxing-files.sh` aufrufen

### Anleitung Code-Analyse

Entscheidend sind die `valueAt`-Methoden.
In den Tests enthält die `valueAt`-Methode jeweils ein `println`.
Damit kann man die Code-Stelle schnell finden (Ctrl-F)

> [!WARNING]
> Der Code enthält alle Provider aus allen Tests.
Bitte genau darauf achten, dass man wirklich die richtige Stelle mit dem richtigen String erwischt hat.

## Vorgehen JVM (IntelliJ IDEA)

- Test öffnen
- Tools - Kotlin - Show Kotlin Bytecode
- In the tool window press "Decompile"
- try to figure out if the value has been boxed
