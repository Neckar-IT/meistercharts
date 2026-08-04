package it.neckar.gradle

import it.neckar.open.app.env.DotEnv
import it.neckar.open.app.env.EnvFileName
import it.neckar.open.app.env.SystemFileSystemAccess
import java.io.File
import kotlinx.io.files.Path
import org.gradle.api.Project

/**
 * The `.env` for build scripts, searched from [startDirectory] upwards to the git root.
 *
 * The lookup answers null because every build-script caller consults the environment itself, at its
 * own point in its own resolution order. Reads the file on every call, so a task resolves this in
 * `doFirst` rather than during configuration.
 */
fun dotEnvAt(startDirectory: File): DotEnv = DotEnv.load(Path(startDirectory.path), EnvFileName.Default, SystemFileSystemAccess) { null }

/** The root project's `.env`. */
val Project.dotEnv: DotEnv
  get() = dotEnvAt(rootProject.rootDir)
