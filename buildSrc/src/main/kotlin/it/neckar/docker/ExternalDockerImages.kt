package it.neckar.docker

import it.neckar.docker.ExternalDockerImages.variableName
import it.neckar.projects.common.DockerImageDescriptor
import it.neckar.projects.common.DockerImageDescriptorWithoutTag
import it.neckar.projects.common.DockerRegistry
import org.gradle.api.internal.TaskInputsInternal

/**
 * Contains the version numbers for all (external) docker containers
 *
 * Call `gradle printExternalDockerImages` to print all variables`
 */
object ExternalDockerImages {
  val MongoDb: DockerImageDescriptorWithoutTag = create("mongo")
  val Postgres: DockerImageDescriptorWithoutTag = create("postgres")
  val Traefik: DockerImageDescriptorWithoutTag = create("traefik")

  /**
   * This image (including registry) is suggested by the official keycloak guide: https://www.keycloak.org/getting-started/getting-started-dock
   */
  val Keycloak: DockerImageDescriptorWithoutTag = create(DockerRegistry.Quay, "keycloak/keycloak")

  val Gatus: DockerImageDescriptorWithoutTag = create(nameComponent = "twinproduction/gatus")
  val Prometheus: DockerImageDescriptorWithoutTag = create(nameComponent = "prom/prometheus")
  val PrometheusNodeExporter: DockerImageDescriptorWithoutTag = create(registry = DockerRegistry.Quay, nameComponent = "prometheus/node-exporter")
  val PromPushgateway: DockerImageDescriptorWithoutTag = create(nameComponent = "prom/pushgateway")
  val PromAlertmanager: DockerImageDescriptorWithoutTag = create(nameComponent = "prom/alertmanager")
  val GrafanaLoki: DockerImageDescriptorWithoutTag = create(nameComponent = "grafana/loki")
  val Grafana: DockerImageDescriptorWithoutTag = create(nameComponent = "grafana/grafana-oss")
  val Umami: DockerImageDescriptorWithoutTag = create(registry = DockerRegistry.Ghcr, nameComponent = "umami-software/umami")
  val Watchtower: DockerImageDescriptorWithoutTag = create(nameComponent = "containrrr/watchtower")
  val Vaultwarden: DockerImageDescriptorWithoutTag = create(nameComponent = "vaultwarden/server")
  val Rclone: DockerImageDescriptorWithoutTag = create(nameComponent = "rclone/rclone")

  /**
   * Returns the name of the variable that is used to reference the image
   */
  fun variableName(descriptor: DockerImageDescriptorWithoutTag): String {
    return "$VariableNamePrefix${descriptor.nameComponent}"
  }

  /**
   * Contains all entries
   */
  val entries: List<DockerImageDescriptorWithoutTag> = listOf(
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

  internal fun create(nameComponent: String): DockerImageDescriptorWithoutTag {
    return create(DockerRegistry.DockerHub, nameComponent)
  }

  internal fun create(registry: DockerRegistry, nameComponent: String): DockerImageDescriptorWithoutTag {
    return DockerImageDescriptorWithoutTag.create(registry, nameComponent)
  }

  /**
   * Returns the image for the given name component
   */
  fun get(nameComponent: String): DockerImageDescriptorWithoutTag {
    return entries.firstOrNull { it.nameComponent == nameComponent } ?: throw IllegalArgumentException("No docker image found for [$nameComponent]")
  }

  /**
   * Creates a new list with all image descriptors including the provided tags
   */
  fun withTag(dockerImageTags: ExternalDockerImageTags): List<DockerImageDescriptor> {
    return entries.map { it.withTag(dockerImageTags) }
  }

  const val VariableNamePrefix: String = "docker-image::"
}

/**
 * Adds all external docker images as properties to the task
 */
fun TaskInputsInternal.externalDockerImages() {
  val tags = ExternalDockerImageTags.loadFromDockerVersionProperties()
  properties(ExternalDockerImages.entries, tags)
}


/**
 * Adds all external docker images as properties to the task
 */
fun TaskInputsInternal.properties(values: List<DockerImageDescriptorWithoutTag>, tags: ExternalDockerImageTags) {
  properties(values.associate {
    val withTag = it.withTag(tags)
    variableName(it) to withTag.fqName
  })
}
