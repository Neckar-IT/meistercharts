# Meistercharts - How to build

Kurzanleitung um Meistercharts zu bauen

## Prerequisites ==

- Install a JDK 17 or newer to run Gradle; the build downloads its JDK 25 toolchain

## Build ==

- execute `gradlew build` to build the project

Gradle will download all dependencies and build the project.

## Artifacts ==

### Easy API ===

The project
`meistercharts-api/meistercharts-easy-api`
contains the TypeScript API for Meistercharts.

#### NPM Bundle

The NPM bundle can be found under:
`meistercharts-api/meistercharts-easy-api/build/npm/@meistercharts-easy-api.tar.gz`

The directory `meistercharts-api/meistercharts-easy-api/build/npm-work` holds the unpacked package.
The task `npmCopyBundleContent` copies `meistercharts-api/meistercharts-easy-api/package.json` and writes `moduleName` of the extension `npmBundle` and the Gradle property `meisterchartsVersion` into the copy.

##### Using the bundle of the current source

[`meistercharts-link-poc`](meistercharts-link-poc/README.md) is the reference for a pnpm project using this bundle instead of the registry version.

## Build as a build of its own

https://github.com/Neckar-IT/meistercharts holds this directory as a Gradle build of its own. It builds the library modules from their sources and resolves the `it.neckar.open` libraries from Maven Central in the version `neckarOpenVersion` of `gradle.properties`.
