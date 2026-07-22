package it.neckar.gradle

import it.neckar.projects.Projects
import org.gradle.api.tasks.AbstractCopyTask

/**
 * A role-specific subdirectory of `:internal:infrastructure:common` whose
 * files can be pulled into the destination of a Gradle [AbstractCopyTask].
 *
 * All role definitions live together in this file — every magic string for
 * source folder, include glob and destination folder has exactly one home.
 *
 * @property sourceSubdir    subfolder under `common/` that holds the role's assets
 * @property includePattern  glob matched against files in [sourceSubdir]
 * @property destinationSubdir  subfolder inside the copy destination where matched files land
 */
private class CommonInfrastructureRole(
  val sourceSubdir: String,
  val includePattern: String,
  val destinationSubdir: String,
) {
  fun applyTo(task: AbstractCopyTask) {
    val commonProjectDir = Projects.infrastructure_common.project().projectDir
    task.from(commonProjectDir.resolve(sourceSubdir)) {
      include(includePattern)
      into(destinationSubdir)
    }
  }
}

private val CommonTraefikCompose = CommonInfrastructureRole(
  sourceSubdir = CommonComposeRole.Traefik.sourceSubdir,
  includePattern = "docker-compose-common-*.yml",
  destinationSubdir = "docker-compose",
)

private val CommonGitlabRunnerScripts = CommonInfrastructureRole(
  sourceSubdir = "gitlab-runner",
  includePattern = "*.sh",
  destinationSubdir = "scripts",
)

private val CommonRestrictedEgressAssets = CommonInfrastructureRole(
  sourceSubdir = "gitlab-runner/restricted-egress",
  includePattern = "*",
  destinationSubdir = "scripts/restricted-egress",
)

private val CommonOtelAgentCompose = CommonInfrastructureRole(
  sourceSubdir = CommonComposeRole.OtelAgent.sourceSubdir,
  includePattern = "*.yml",
  destinationSubdir = "docker-compose",
)

private val CommonHostExportersCompose = CommonInfrastructureRole(
  sourceSubdir = CommonComposeRole.HostExporters.sourceSubdir,
  includePattern = "*.yml",
  destinationSubdir = "docker-compose",
)

private val CommonHostManagementCompose = CommonInfrastructureRole(
  sourceSubdir = CommonComposeRole.HostManagement.sourceSubdir,
  includePattern = "docker-compose-common-*.yml",
  destinationSubdir = "docker-compose",
)

private val CommonHostLogsCompose = CommonInfrastructureRole(
  sourceSubdir = CommonComposeRole.HostLogs.sourceSubdir,
  includePattern = "docker-compose-common-*.yml",
  destinationSubdir = "docker-compose",
)

private val CommonWorkerHostScripts = CommonInfrastructureRole(
  sourceSubdir = "worker-host",
  includePattern = "*.sh",
  destinationSubdir = "",
)

private val CommonHostLandingPageCompose = CommonInfrastructureRole(
  sourceSubdir = CommonComposeRole.HostLandingPage.sourceSubdir,
  includePattern = "docker-compose-common-*.yml",
  destinationSubdir = "docker-compose",
)

private val CommonHostLandingPageHtml = CommonInfrastructureRole(
  sourceSubdir = "${CommonComposeRole.HostLandingPage.sourceSubdir}/html",
  includePattern = "*",
  destinationSubdir = "docker-compose/${CommonComposeRole.HostLandingPage.sourceSubdir}",
)

/**
 * Pulls in the shared Traefik Docker Compose fragment.
 * Host declares explicitly that it plays the reverse-proxy role.
 */
fun AbstractCopyTask.includeCommonTraefikCompose() = CommonTraefikCompose.applyTo(this)

/**
 * Pulls in the shared GitLab-Runner maintenance scripts.
 * Host declares explicitly that it runs a GitLab Runner with Docker executor.
 * The deploy script is expected to scp the resulting `scripts/` folder to
 * `/srv/scripts` on the target host and to register a cron entry.
 *
 * The executable bit is preserved from the source file permissions (`chmod +x` in the repo).
 */
fun AbstractCopyTask.includeCommonGitlabRunnerScripts() = CommonGitlabRunnerScripts.applyTo(this)

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
 * Copies two files into `docker-compose/`:
 * - `docker-compose-common-otel-agent.yml` (compose fragment)
 * - `otel-agent-config.yml` (agent configuration)
 *
 * The main `docker-compose.yml` pulls the service via `extends:` — the config
 * file is mounted by the fragment via a relative path, so both files must land
 * side by side on the deployed host.
 *
 * Consumers must supply `otel-collector-client-id` and `otel-collector-client-secret`
 * in their `secretsLoader.keys`, plus a `${host_role}` filter substitution
 * (worker hosts → "worker", others → "infrastructure").
 */
fun AbstractCopyTask.includeCommonOtelAgentCompose() = CommonOtelAgentCompose.applyTo(this)

/**
 * Pulls in the shared per-host Prometheus-compatible exporter containers (per ADL 0147).
 * Every host that runs the OTel-Collector also runs these exporters — they are scraped
 * by the OTel-Collector via its sub-keyed `prometheus/<name>` receivers and form the only path for
 * host-level metric sources without a native OTel receiver (smartctl, IPMI, …).
 *
 * Copies one file into `docker-compose/`:
 * - `docker-compose-common-host-exporters.yml` (compose fragment with one service per exporter)
 *
 * The host's main `docker-compose.yml` pulls each exporter via `extends:` and joins it to
 * the `traefik-public` network so the OTel-Collector can resolve it by container name.
 */
fun AbstractCopyTask.includeCommonHostExportersCompose() = CommonHostExportersCompose.applyTo(this)

/**
 * Pulls in the shared host-management compose fragment (portainer + watchtower) into
 * `docker-compose/`. Every host runs these host-level tools via its host stack
 * (see [CommonComposeRole.HostManagement], folded into `hostStack()`).
 *
 * portainer publishes only to 127.0.0.1 (SSH-tunnel access); watchtower exposes no port.
 * Neither needs secrets.
 */
fun AbstractCopyTask.includeCommonHostManagementCompose() = CommonHostManagementCompose.applyTo(this)

/**
 * Pulls in the shared host-logs compose fragment (Dozzle) into `docker-compose/`.
 *
 * Opt-in per host via an explicit `composeRole(CommonComposeRole.HostLogs)` — NOT part of
 * `hostStack()`. Dozzle is public (via Traefik) and guarded only by a Keycloak OIDC
 * middleware, so a host may enable it only once it supplies `logs-keycloak-client-id`,
 * `logs-keycloak-client-secret`, `traefik-oidc-encryption-secret` and DNS for `logs.<host>`.
 */
fun AbstractCopyTask.includeCommonHostLogsCompose() = CommonHostLogsCompose.applyTo(this)

/**
 * Pulls in the shared host-landing-page compose fragment plus its static HTML into
 * `docker-compose/` (nginx serving a deliberate 200 host-info page on the host's root URL,
 * instead of the error-pages 503 fallback — #1587).
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
 * Target: `*.sh` files from `common/worker-host/` → build-output root.
 *
 * Used by worker hosts that run a GitLab Runner (see `:worker-01-host.neckar.it`,
 * `:worker-02-host.neckar.it`), where they back the `registerRunners` task. The one-command
 * `bootstrap` task in each worker's `build.gradle.kts` chains `provision` → `registerRunners`
 * → `deploy`.
 */
fun AbstractCopyTask.includeCommonWorkerHostScripts() = CommonWorkerHostScripts.applyTo(this)

/**
 * The shared host-stack compose fragments a host can fold into its materialized
 * `docker-compose/` directory. Declared on the `deployment { … }` extension so the
 * host-stack deploys through the same plugin as every other container, instead of a
 * hand-rolled `processResources` configuration.
 */
enum class CommonComposeRole(
  /**
   * The role's asset directory under `internal/infrastructure/common/`. Single home of the
   * subdir string (the role objects above reference it), and the join key for the
   * continuous-deploy common-fragment edge: a changed file under this subdir marks every
   * continuous-deploy module consuming the role (`ContinuousDeployResolver`, #2341).
   */
  val sourceSubdir: String,
) {
  Traefik("traefik"),
  OtelAgent("otel-agent"),
  HostExporters("host-exporters"),
  HostManagement("host-management"),
  HostLogs("host-logs"),
  HostLandingPage("host-landing-page"),
}

/** Applies the [role]'s shared compose fragment to this copy task's destination. */
fun AbstractCopyTask.includeCommonComposeRole(role: CommonComposeRole) = when (role) {
  CommonComposeRole.Traefik -> includeCommonTraefikCompose()
  CommonComposeRole.OtelAgent -> includeCommonOtelAgentCompose()
  CommonComposeRole.HostExporters -> includeCommonHostExportersCompose()
  CommonComposeRole.HostManagement -> includeCommonHostManagementCompose()
  CommonComposeRole.HostLogs -> includeCommonHostLogsCompose()
  CommonComposeRole.HostLandingPage -> includeCommonHostLandingPageCompose()
}
