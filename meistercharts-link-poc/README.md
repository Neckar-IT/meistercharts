# meistercharts-link-poc

```bash
./gradlew :internal:open:meistercharts:meistercharts-link-poc:build
```

`meistercharts-link-poc` ist das Referenzprojekt für ein pnpm-Projekt, das das Bundle von `meistercharts-easy-api` aus dem aktuellen Stand verwendet. `meistercharts-react` und `meistercharts.com` lesen `@meistercharts/meistercharts` in der `catalog:`-Version aus der npm-Registry.

Ein pnpm-Projekt verwendet das Bundle mit diesen Deklarationen:

1. `package.json` enthält `"@meistercharts/meistercharts": "link:../meistercharts-api/meistercharts-easy-api/build/npm-work"`. `pnpm install` läuft ohne den Ordner `internal/open/meistercharts/meistercharts-api/meistercharts-easy-api/build/npm-work` durch und legt einen Symlink an. Der Symlink löst auf, sobald dieser Ordner existiert.
2. `build.gradle.kts` ruft `declaresNpmBundle` an `pnpmRunBuild` auf. `pnpmRunBuild` führt damit vorher `meistercharts-easy-api:npmBundle` aus. Ein `pnpmRunBuild` mit `dist`-Output läuft erneut, sobald sich `internal/open/meistercharts/meistercharts-api/meistercharts-easy-api/build/npm-work` ändert. `pnpmRunBuild` von `meistercharts-link-poc` hat keinen Output und läuft bei jedem Build.

`:pnpmInstall` baut kein Kotlin/JS-Modul.

`src/linkedBundle.test.ts` schlägt fehl, sobald die Dependency auf die Registry auflöst. Das Bundle legt beim Laden einen Canvas-Context an. `jsdom` implementiert keinen Canvas, deshalb ersetzt `src/canvasStub.ts` den Canvas-Context.
