package it.neckar.gradle.pnpm.dependencies

/**
 * Type of dependency
 */
enum class NpmDependencyType(val prefix: String?) {
  Production(null),
  Peer("peer"),
  Dev("dev"),
}
