package it.neckar.gradle

import it.neckar.gradle.deployment.PipedScriptName
import it.neckar.projects.Projects
import it.neckar.runtime.context.HostPath
import org.gradle.api.tasks.AbstractCopyTask

/**
 * A role-specific subdirectory of `:internal:infrastructure:common` whose
 * files can be pulled into the destination of a Gradle [AbstractCopyTask].
 *
 * All role definitions live together in this file — every magic string for
 * source folder, include glob and destination folder has exactly one home.
 *
 * @property sourceSubdir    subfolder under `common/` that holds the role's assets
 * @property includePatterns  globs matched against files in [sourceSubdir]
 * @property destinationSubdir  subfolder inside the copy destination where matched files land
 */
private class CommonInfrastructureRole(
  val sourceSubdir: String,
  val includePatterns: List<String>,
  val destinationSubdir: String,
) {
  fun applyTo(task: AbstractCopyTask) {
    val commonProjectDir = Projects.infrastructure_common.project().projectDir
    task.from(commonProjectDir.resolve(sourceSubdir)) {
      includePatterns.forEach { pattern -> include(pattern) }
      into(destinationSubdir)
    }
  }
}

/** The directory on a host holding the host stack's compose file, the role fragments beside it and the host's own scripts. */
val HostStackDirectory: HostPath = HostPath("/srv/host")

/** [HostStackDirectory] within the materialized deployment directory of a host. */
private val HostStackRemoteTreeDirectory: String = "remote${HostStackDirectory.value}"

/** The directory of the materialized deployment directory holding the scripts `prepareHost` pipes into a shell on the host. */
const val PipedScriptsDirectory: String = "piped"

/** The fragments of [role] plus the paths they mount, copied beside the host stack compose file. */
private fun composeRoleFiles(role: CommonComposeRole, carriedMountedPaths: List<String> = role.mountedPaths): CommonInfrastructureRole {
  return CommonInfrastructureRole(
    sourceSubdir = role.sourceSubdir,
    includePatterns = listOf(CommonComposeFragmentPattern) + carriedMountedPaths,
    destinationSubdir = HostStackRemoteTreeDirectory,
  )
}

private val CommonTraefikCompose = composeRoleFiles(CommonComposeRole.Traefik)

private val CommonRestrictedEgressAssets = CommonInfrastructureRole(
  sourceSubdir = "gitlab-runner/restricted-egress",
  includePatterns = listOf("*"),
  destinationSubdir = "scripts/restricted-egress",
)

private val CommonOtelAgentCompose = composeRoleFiles(CommonComposeRole.OtelAgent)

private val CommonHostExportersCompose = composeRoleFiles(CommonComposeRole.HostExporters)

private val CommonHostManagementCompose = composeRoleFiles(CommonComposeRole.HostManagement)

private val CommonHostLogsCompose = composeRoleFiles(CommonComposeRole.HostLogs)

private val CommonWorkerHostScripts = CommonInfrastructureRole(
  sourceSubdir = "worker-host",
  includePatterns = listOf("*.sh"),
  destinationSubdir = "",
)

// The mounted page directory is filled from `html/`, so only the fragment is copied from the role directory.
private val CommonHostLandingPageCompose = composeRoleFiles(CommonComposeRole.HostLandingPage, carriedMountedPaths = emptyList())

private val CommonHostLandingPageHtml = CommonInfrastructureRole(
  sourceSubdir = "${CommonComposeRole.HostLandingPage.sourceSubdir}/html",
  includePatterns = listOf("*"),
  destinationSubdir = "$HostStackRemoteTreeDirectory/${CommonComposeRole.HostLandingPage.sourceSubdir}",
)

/**
 * Pulls in the shared Traefik Docker Compose fragment.
 * Host declares explicitly that it plays the reverse-proxy role.
 */
fun AbstractCopyTask.includeCommonTraefikCompose() = CommonTraefikCompose.applyTo(this)

/**
 * Pulls in the assets needed by `setup-restricted-runner.sh` to provision the
 * second, egress-restricted GitLab Runner instance on a worker host:
 * `apply-rules.sh`, `sanitize-runner-config.py`, `dnsmasq.conf.template`,
 * `allowlist.conf`, and the systemd unit files.
 *
 * Target: every file under `common/gitlab-runner/restricted-egress/` →
 * `scripts/restricted-egress/` in the build-output directory. `setup-restricted-runner.sh`
 * scp's them to the target host during `bootstrap`.
 *
 * See `docs/workflow/restricted-egress-runner.md`.
 */
fun AbstractCopyTask.includeCommonRestrictedEgressAssets() = CommonRestrictedEgressAssets.applyTo(this)

/**
 * Pulls in the shared per-host OTel Agent Docker Compose fragment and its
 * agent config. Host declares explicitly that it runs the OTel Agent per
 * ADL 0143 ("one agent per host"). The agent forwards local telemetry to
 * the central OTel Gateway on `monitoring-host.neckar.it`.
 *
 * Copies two files beside the host stack compose file in [HostStackDirectory]:
 * - `docker-compose-common-otel-agent.yml` (compose fragment)
 * - `otel-agent-config.yml` (agent configuration)
 *
 * The main `docker-compose.yml` pulls the service via `extends:` — the config
 * file is mounted by the fragment via a relative path, so both files must land
 * side by side on the deployed host.
 *
 * Consumers must supply `otel-collector-client-id` and `otel-collector-client-secret`
 * in their `secretsLoader.keys`, plus two filter substitutions: `${host_role}`
 * (worker hosts → "worker", others → "infrastructure") and `${deployment_environment}`
 * (the host's stage, lowercased: "production" / "development" / "demo").
 */
fun AbstractCopyTask.includeCommonOtelAgentCompose() = CommonOtelAgentCompose.applyTo(this)

/**
 * Pulls in the shared per-host Prometheus-compatible exporter containers (per ADL 0147).
 * Every host that runs the OTel-Collector also runs these exporters — they are scraped
 * by the OTel-Collector via its sub-keyed `prometheus/<name>` receivers and form the only path for
 * host-level metric sources without a native OTel receiver (smartctl, IPMI, …).
 *
 * Copies one file beside the host stack compose file in [HostStackDirectory]:
 * - `docker-compose-common-host-exporters.yml` (compose fragment with one service per exporter)
 *
 * The host's main `docker-compose.yml` pulls each exporter via `extends:` and joins it to
 * the `traefik-public` network so the OTel-Collector can resolve it by container name.
 */
fun AbstractCopyTask.includeCommonHostExportersCompose() = CommonHostExportersCompose.applyTo(this)

/**
 * Pulls in the shared host-management compose fragment (portainer) beside the host stack compose file.
 * Every host runs it via its host stack (see [CommonComposeRole.HostManagement], folded into
 * `hostStack()`).
 *
 * portainer publishes only to 127.0.0.1 (SSH-tunnel access) and needs no secrets.
 */
fun AbstractCopyTask.includeCommonHostManagementCompose() = CommonHostManagementCompose.applyTo(this)

/**
 * Pulls in the shared host-logs compose fragment (Dozzle) beside the host stack compose file.
 *
 * Opt-in per host via an explicit `composeRole(CommonComposeRole.HostLogs)` — NOT part of
 * `hostStack()`. Dozzle is public (via Traefik) and guarded only by a Keycloak OIDC
 * middleware, so a host may enable it only once it supplies `logs-keycloak-client-id`,
 * `logs-keycloak-client-secret`, `traefik-oidc-encryption-secret` and DNS for `logs.<host>`.
 */
fun AbstractCopyTask.includeCommonHostLogsCompose() = CommonHostLogsCompose.applyTo(this)

/**
 * Pulls in the shared host-landing-page compose fragment plus its static HTML beside the host stack
 * compose file (nginx serving a deliberate 200 host-info page on the host's root URL,
 * instead of the error-pages 503 fallback).
 *
 * Opt-in per host via an explicit `composeRole(CommonComposeRole.HostLandingPage)` — NOT part
 * of `hostStack()`. The page substitutes `${deployTarget}` and `${host-landing-purpose}`
 * (the latter supplied per host via `deployment { extraReplacements }`) and links `/traefik`
 * and `logs.<host>`, so a host should enable it only together with working dashboard routing
 * and the [CommonComposeRole.HostLogs] role.
 */
fun AbstractCopyTask.includeCommonHostLandingPageCompose() {
  CommonHostLandingPageCompose.applyTo(this)
  CommonHostLandingPageHtml.applyTo(this)
}

/**
 * Pulls in the shared worker-host runner-registration scripts (`register-runners.sh`,
 * `setup-gitlab-runner.sh`, `setup-restricted-runner.sh`). These scripts sit at the root
 * of the build output directory and are intended to be executed locally against the target
 * host (not copied to the host).
 *
 * `runner-identity-lib.sh` is copied along by the `*.sh` pattern but is never executed from there:
 * all three scripts fold it in via `# @inline: worker-host/runner-identity-lib.sh`, so the
 * materialized copies are self-contained.
 *
 * Target: `*.sh` files from `common/worker-host/` → build-output root.
 *
 * Used by worker hosts that run a GitLab Runner (see `:worker-01-host.neckar.it`,
 * `:worker-02-host.neckar.it`), where they back the `registerRunners` task. The one-command
 * `bootstrap` task in each worker's `build.gradle.kts` chains `provision` → `registerRunners`
 * → `deploy`.
 */
fun AbstractCopyTask.includeCommonWorkerHostScripts() = CommonWorkerHostScripts.applyTo(this)

/**
 * The shared host-stack compose fragments a host folds into [HostStackDirectory], beside its own compose file.
 * Declared on the `deployment { … }` extension, which materializes each declared role into the remote tree.
 */
enum class CommonComposeRole(
  /**
   * The role's asset directory under `internal/infrastructure/common/`. Single home of the
   * subdir string (the role objects above reference it), and the join key for the
   * continuous-deploy common-fragment edge: a changed file under this subdir marks every
   * continuous-deploy module consuming the role (`ContinuousDeployResolver`).
   */
  val sourceSubdir: String,
  /**
   * Ant patterns of the paths the role's fragment bind-mounts relative to itself. A role whose mounted files lie beside its fragment
   * copies exactly these; `HostLandingPage` copies its page from `html/` into the mounted directory.
   */
  val mountedPaths: List<String> = emptyList(),
  /** Directories beside the fragment whose content on the host the role owns, so a file the role no longer carries is deleted there. */
  val ownedDirectories: List<HostPath> = emptyList(),
  /** The configs the role's container reads only at startup; a change to one restarts the container. */
  val configPaths: List<HostPath> = emptyList(),
) {
  Traefik("traefik"),
  OtelAgent(
    "otel-agent",
    mountedPaths = listOf("otel-agent-config.yml"),
    configPaths = listOf(HostPath("/srv/host/otel-agent-config.yml"), HostPath("/srv/host/otel-agent-config-overlay.yml")),
  ),
  HostExporters("host-exporters"),
  HostManagement("host-management"),
  HostLogs("host-logs"),
  HostLandingPage("host-landing-page", mountedPaths = listOf("host-landing-page/**"), ownedDirectories = listOf(HostPath("/srv/host/host-landing-page"))),
}

/** Ant pattern matching every shared fragment, in a role's source directory and in the materialized one. */
const val CommonComposeFragmentPattern: String = "docker-compose-common-*.yml"

/** Applies the [role]'s shared compose fragment to this copy task's destination. */
fun AbstractCopyTask.includeCommonComposeRole(role: CommonComposeRole) = when (role) {
  CommonComposeRole.Traefik -> includeCommonTraefikCompose()
  CommonComposeRole.OtelAgent -> includeCommonOtelAgentCompose()
  CommonComposeRole.HostExporters -> includeCommonHostExportersCompose()
  CommonComposeRole.HostManagement -> includeCommonHostManagementCompose()
  CommonComposeRole.HostLogs -> includeCommonHostLogsCompose()
  CommonComposeRole.HostLandingPage -> includeCommonHostLandingPageCompose()
}

/**
 * A script under `internal/infrastructure/common/` that `prepareHost` pipes into a shell on the host, materialized into the
 * [PipedScriptsDirectory] of every module whose declarations need it.
 */
enum class CommonPipedScript(val sourceSubdir: String, val fileName: PipedScriptName) {
  /** Keeps the continuous-deploy key in root's `authorized_keys`. */
  ContinuousDeployKey("host-keys", PipedScriptName("install-continuous-deploy-key.sh")),

  /** Installs the host's maintenance crontab; its arguments are the host's extra crontab lines. */
  MaintenanceCron("host-maintenance", PipedScriptName("install-maintenance-cron.sh")),
  ;

  /** The path relative to `internal/infrastructure/common/`. */
  val relativePath: String
    get() = "$sourceSubdir/${fileName.value}"
}

fun AbstractCopyTask.includeCommonPipedScript(script: CommonPipedScript) {
  CommonInfrastructureRole(script.sourceSubdir, listOf(script.fileName.value), PipedScriptsDirectory).applyTo(this)
}
