/**
 * Provides information about the Gitlab CI
 *
 * https://docs.gitlab.com/ee/ci/variables/predefined_variables.html
 * https://docs.gitlab.com/ee/ci/variables/deprecated_variables.html#gitlab-90-renamed-variables
 *
 */
class GitlabCiInformation(private val env: Map<String, String?>) {

  val inCi: Boolean
    get() = env.containsKey("GITLAB_CI")

  val buildNumber: String?
    get() = env.getOrDefault("CI_PIPELINE_IID", null)

  val branch: String?
    get() {
      if (isTag()) {
        return null
      }
      return if (isPullRequest()) {
        env.getOrDefault("CI_MERGE_REQUEST_SOURCE_BRANCH_NAME", null)
      } else env.getOrDefault("CI_COMMIT_REF_NAME", env.getOrDefault("CI_BUILD_REF_NAME", null))
    }

  val pullRequest: String?
    get() = env.getOrDefault("CI_MERGE_REQUEST_IID", null)

  val pullRequestTargetBranch: String?
    get() = if (isPullRequest()) env.getOrDefault("CI_MERGE_REQUEST_TARGET_BRANCH_NAME", null) else null

  val tag: String?
    get() = env.getOrDefault("CI_COMMIT_TAG", env.getOrDefault("CI_BUILD_TAG", null))

  /**
   * @return The SCM reference that is currently being built. Either a tag or a branch, depending on what is being built
   */
  fun getReference(): String? {
    return if (isTag()) tag else branch
  }

  /**
   * @return If the current build is a pull request
   */
  fun isPullRequest(): Boolean {
    return pullRequest != null
  }

  /**
   * @return If the current build is a build of a tag
   */
  fun isTag(): Boolean {
    return tag != null
  }

  companion object {
    /**
     * Creates a new instance
     */
    fun create(): GitlabCiInformation {
      return GitlabCiInformation(System.getenv())
    }
  }
}
