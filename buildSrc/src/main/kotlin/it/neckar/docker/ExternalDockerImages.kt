package it.neckar.docker

import GradleContext
import it.neckar.docker.ExternalDockerImages.variableName
import it.neckar.projects.common.DockerImageDescriptor
import it.neckar.projects.common.DockerRegistry
import org.gradle.api.internal.TaskInputsInternal
import java.io.File
import java.util.Properties

/**
 * Contains the version numbers for all (external) docker containers
 *
 * Call `gradle printExternalDockerImages` to print all variables`
 */
object ExternalDockerImages {
  /**
   * Contains the version numbers from nameComponent to tag/version
   */
  internal val versionNumbersMap = loadVersionPropertiesFile()

  val MongoDb: DockerImageDescriptor = create("mongo")
  val Postgres: DockerImageDescriptor = create("postgres")
  val Traefik: DockerImageDescriptor = create("traefik")

  /**
   * This image (including registry) is suggested by the official keycloak guide: https://www.keycloak.org/getting-started/getting-started-dock
   */
  val Keycloak: DockerImageDescriptor = create(DockerRegistry.Quay, "keycloak/keycloak")

  val Gatus: DockerImageDescriptor = create(nameComponent = "twinproduction/gatus")
  val Prometheus: DockerImageDescriptor = create(nameComponent = "prom/prometheus")
  val PrometheusNodeExporter: DockerImageDescriptor = create(registry = DockerRegistry.Quay, nameComponent = "prometheus/node-exporter")
  val PromPushgateway: DockerImageDescriptor = create(nameComponent = "prom/pushgateway")
  val PromAlertmanager: DockerImageDescriptor = create(nameComponent = "prom/alertmanager")
  val GrafanaLoki: DockerImageDescriptor = create(nameComponent = "grafana/loki")
  val Grafana: DockerImageDescriptor = create(nameComponent = "grafana/grafana-oss")
  val Umami: DockerImageDescriptor = create(registry = DockerRegistry.Ghcr, nameComponent = "umami-software/umami")
  val Watchtower: DockerImageDescriptor = create(nameComponent = "containrrr/watchtower")
  val Vaultwarden: DockerImageDescriptor = create(nameComponent = "vaultwarden/server")
  val Rclone: DockerImageDescriptor = create(nameComponent = "rclone/rclone")

  /**
   * Returns the name of the variable that is used to reference the image
   */
  fun variableName(descriptor: DockerImageDescriptor): String {
    return "$VariableNamePrefix${descriptor.nameComponent}"
  }

  /**
   * Contains all entries
   */
  val entries: List<DockerImageDescriptor> = listOf(
    MongoDb,
    Postgres,
    Traefik,
    Keycloak,
    Gatus,
    Prometheus,
    PrometheusNodeExporter,
    PromPushgateway,
    PromAlertmanager,
    GrafanaLoki,
    Grafana,
    Umami,
    Vaultwarden,
    Watchtower,
    Rclone,
  )

  internal fun create(nameComponent: String): DockerImageDescriptor {
    return create(DockerRegistry.DockerHub, nameComponent)
  }

  internal fun create(registry: DockerRegistry, nameComponent: String): DockerImageDescriptor {
    val version = versionNumbersMap[nameComponent] ?: throw IllegalArgumentException(
      "No version number / tag found for [$nameComponent].\n" +
        "Please add an entry to the version.docker.properties file:\n" +
        "   $nameComponent=1.2.3"
    )
    return DockerImageDescriptor.create(registry, nameComponent, version)
  }

  /**
   * Returns the image for the given name component
   */
  fun get(nameComponent: String): DockerImageDescriptor {
    return entries.firstOrNull { it.nameComponent == nameComponent } ?: throw IllegalArgumentException("No docker image found for [$nameComponent]")
  }

  const val VariableNamePrefix: String = "docker-image::"

  /**
   * Loads the version properties file
   */
  private fun loadVersionPropertiesFile(): Map<String, String> {
    //Fallback to load the file directly - for unit tests
    val propertiesFile = GradleContext.rootProjectOrNull()?.file("version.docker.properties") ?: File("../version.docker.properties")

    require(propertiesFile.exists()) { "File not found: ${propertiesFile.absolutePath}" }

    val properties = propertiesFile.inputStream().use { input ->
      Properties().apply {
        load(input)
      }
    }

    return properties.entries.associate { it.key.toString() to it.value.toString() }
  }
}

/**
 * Adds all external docker images as properties to the task
 */
fun TaskInputsInternal.externalDockerImages() {
  properties(ExternalDockerImages.entries)
}

/**
 * Adds all external docker images as properties to the task
 */
fun TaskInputsInternal.properties(values: List<DockerImageDescriptor>) {
  properties(values.associate {
    variableName(it) to it.fqName
  })
}
