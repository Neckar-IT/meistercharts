package it.neckar.gradle

import java.net.URI

/**
 * The URLs for the repositories
 */
object Repos {
  val sonatype_releases: URI = URI("https://oss.sonatype.org/content/repositories/releases/")

  val ktorEap: URI = URI("https://maven.pkg.jetbrains.space/public/p/ktor/eap/")
}
