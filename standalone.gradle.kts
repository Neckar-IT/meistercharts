// The root script of the export of internal/open/meistercharts, named by its settings.gradle.kts. Inside the monorepo the root script
// of :internal:open:meistercharts is build.gradle.kts.

import dev.detekt.gradle.extensions.DetektExtension

description = "MeisterCharts"

subprojects {
  // The rules MeisterCharts relaxes, on top of the configuration of the build platform, as inside the monorepo.
  pluginManager.withPlugin("dev.detekt") {
    extensions.configure<DetektExtension> {
      config.from(rootProject.file("config/detekt/detekt-meistercharts.yml"))
    }
  }
}
