package it.neckar.docker

import it.neckar.docker.ExternalDockerImages.variableName
import it.neckar.projects.common.docker.DockerImageDescriptor
import it.neckar.projects.common.docker.DockerImageDescriptorWithoutTag
import it.neckar.projects.common.docker.DockerRegistry
import it.neckar.projects.common.docker.DockerRepository
import org.gradle.api.internal.TaskInputsInternal

/**
 * Contains the version numbers for all (external) docker containers
 *
 * Call `gradle printExternalDockerImages` to print all variables`
 */
object ExternalDockerImages {
  val MongoDb: DockerImageDescriptorWithoutTag = createDefault(repositoryAsString = "mongo")
  val Postgres: DockerImageDescriptorWithoutTag = createDefault(repositoryAsString = "postgres")
  val Traefik: DockerImageDescriptorWithoutTag = createDefault(repositoryAsString = "traefik")

  /**
   * This image (including registry) is suggested by the official keycloak guide: https://www.keycloak.org/getting-started/getting-started-dock
   */
  val Keycloak: DockerImageDescriptorWithoutTag = create(DockerRegistry.Quay, "keycloak/keycloak")

  val Gatus: DockerImageDescriptorWithoutTag = createDefault(repositoryAsString = "twinproduction/gatus")
  val Prometheus: DockerImageDescriptorWithoutTag = createDefault(repositoryAsString = "prom/prometheus")
  val PrometheusNodeExporter: DockerImageDescriptorWithoutTag = create(registry = DockerRegistry.Quay, repositoryAsString = "prometheus/node-exporter")
  val PromPushgateway: DockerImageDescriptorWithoutTag = createDefault(repositoryAsString = "prom/pushgateway")
  val PromAlertmanager: DockerImageDescriptorWithoutTag = createDefault(repositoryAsString = "prom/alertmanager")
  val GrafanaLoki: DockerImageDescriptorWithoutTag = createDefault(repositoryAsString = "grafana/loki")
  val Grafana: DockerImageDescriptorWithoutTag = createDefault(repositoryAsString = "grafana/grafana-oss")
  val Umami: DockerImageDescriptorWithoutTag = create(registry = DockerRegistry.Ghcr, repositoryAsString = "umami-software/umami")
  val Watchtower: DockerImageDescriptorWithoutTag = createDefault(repositoryAsString = "containrrr/watchtower")
  val Vaultwarden: DockerImageDescriptorWithoutTag = createDefault(repositoryAsString = "vaultwarden/server")
  val Rclone: DockerImageDescriptorWithoutTag = createDefault(repositoryAsString = "rclone/rclone")
  val Wordpress: DockerImageDescriptorWithoutTag = createDefault(repositoryAsString = "wordpress")
  val Mysql: DockerImageDescriptorWithoutTag = createDefault(repositoryAsString = "mysql")

  /**
   * Returns the name of the variable that is used to reference the image
   */
  fun variableName(descriptor: DockerImageDescriptorWithoutTag): String {
    return "$VariableNamePrefix${descriptor.repository}"
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
    Wordpress,
    Mysql
  )

  internal fun createDefault(repositoryAsString: String): DockerImageDescriptorWithoutTag {
    return create(DockerRegistry.DockerHub, repositoryAsString)
  }

  internal fun create(registry: DockerRegistry, repositoryAsString: String): DockerImageDescriptorWithoutTag {
    return DockerImageDescriptorWithoutTag.create(registry, DockerRepository(repositoryAsString))
  }

  /**
   * Returns the image for the given name component
   */
  fun get(repository: DockerRepository): DockerImageDescriptorWithoutTag {
    return entries.firstOrNull { it.repository == repository } ?: throw IllegalArgumentException("No docker image found for [$repository]")
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
