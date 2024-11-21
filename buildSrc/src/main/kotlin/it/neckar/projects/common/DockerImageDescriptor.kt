package it.neckar.projects.common

/**
 * Describes a docker image
 */
interface DockerImageDescriptor : DockerImageDescriptorWithoutTag {

  /**
   * The tag of the image - if there is one
   */
  val tag: String

  /**
   * The image name + tag
   */
  val imageName: String
    get() = "$nameComponent:$tag"

  /**
   * The complete fq name
   */
  val fqName: String
    get() = "$registry/$imageName"

  companion object {
    /**
     * Creates a new descriptor for the "default" registry
     */
    fun default(nameComponent: String, tag: String): DockerImageDescriptor {
      return DefaultDockerImageDescriptor(
        registry = DockerRegistry.DockerHub,
        nameComponent = nameComponent,
        tag = tag,
      )
    }

    fun neckarIt(nameComponent: String, tag: String): DockerImageDescriptor {
      return DefaultDockerImageDescriptor(
        registry = DockerRegistry.NeckarIT,
        nameComponent = nameComponent,
        tag = tag,
      )
    }

    fun create(registry: String, nameComponent: String, tag: String): DockerImageDescriptor {
      return DefaultDockerImageDescriptor(
        registry = DockerRegistry(registry),
        nameComponent = nameComponent,
        tag = tag,
      )
    }

    fun create(registry: DockerRegistry, nameComponent: String, tag: String): DockerImageDescriptor {
      return DefaultDockerImageDescriptor(
        registry = registry,
        nameComponent = nameComponent,
        tag = tag,
      )
    }
  }

  data class DefaultDockerImageDescriptor(
    override val registry: DockerRegistry,
    override val nameComponent: String,
    override val tag: String,
  ) : DockerImageDescriptor {

    override fun withTag(tag: String): DockerImageDescriptor {
      return copy(tag = tag)
    }

    override fun toString(): String {
      return fqName
    }
  }
}

/**
 * Represents a docker registry
 */
data class DockerRegistry(
  val hostName: String,
) {
  override fun toString(): String {
    return hostName
  }

  companion object {
    val DockerHub: DockerRegistry = DockerRegistry("docker.io")
    val NeckarIT: DockerRegistry = DockerRegistry("registry.git.cedarsoft.com/cedarsoft/com.cedarsoft.monorepo")

    /**
     * Repository managed by Red Hat
     */
    val Quay: DockerRegistry = DockerRegistry("quay.io")

    /**
     * "GitHub Container Registry"
     */
    val Ghcr: DockerRegistry = DockerRegistry("ghcr.io")
  }
}
