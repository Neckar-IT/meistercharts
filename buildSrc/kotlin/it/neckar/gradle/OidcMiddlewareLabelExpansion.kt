package it.neckar.gradle

import it.neckar.traefik.CallbackUri
import it.neckar.traefik.ClaimAssertion
import it.neckar.traefik.ClaimName
import it.neckar.traefik.ClaimValue
import it.neckar.traefik.ClientSecret
import it.neckar.traefik.KeycloakClientId
import it.neckar.traefik.OidcMiddlewareName
import it.neckar.traefik.OidcMiddlewareOptions
import it.neckar.traefik.SessionEncryptionSecret
import it.neckar.traefik.TokenForwarding
import org.gradle.api.tasks.AbstractCopyTask
import kotlin.time.Duration.Companion.seconds

/**
 * Expands a `# @oidc-middleware:` directive inside a compose `labels:` list into the canonical
 * `traefik-oidc-auth` label block at materialization — single source of truth for the block,
 * compose analog of [inlineCommonShellIncludes]:
 * ```yaml
 * # @oidc-middleware: name=oidc-mea client-id=${mea-keycloak-client-id} client-secret=${mea-keycloak-client-secret}
 * ```
 *
 * Parameters:
 * - `name` (mandatory) — Traefik middleware name.
 * - `client-id` (mandatory) — Keycloak client (literal or `${…}` placeholder).
 * - `client-secret` (optional) — omit it for a public client. The block always sets
 *   `Provider.UsePkce`, which is what secures the code exchange without a secret (#2856).
 * - `callback-uri` (optional) — overrides the plugin default `/oidc/callback` (Traefik dashboards,
 *   jitsi prosody).
 * - `session-max-age` (optional, default 604800 = 7 days) — `SessionCookie.MaxAge` in seconds; the
 *   cookie slides on every silent token renewal, so a login expires only after this long without a
 *   visit (#2306).
 * - `forward-token` (optional, only value `bearer`) — pass the OIDC session's access token on to
 *   the upstream as `Authorization: Bearer …`, for services that authorize the request themselves.
 *   Named rather than spelled out because the header value is a Go template containing spaces
 *   (`Bearer {{ .accessToken }}`), which this whitespace-separated syntax cannot carry.
 * - `assert-claim` + `any-of` (optional, only together) — gate the route on a token claim rather
 *   than on which client fronted the login: `assert-claim=groups any-of=/ops` admits members of
 *   `/ops` only. `any-of` takes a comma separated list, e.g. `any-of=/staff,/bots`, and each value
 *   becomes an option of its own.
 *   Without them the middleware admits every authenticated user of the realm.
 *
 * The block always requests the optional `offline_access` scope; if the Keycloak client lacks it,
 * Keycloak ignores the request — login never breaks. It also always sets
 * `SessionCookie.SameSite=lax`: the cookie alone authenticates the request, and the plugin default
 * emits no attribute at all. `${…}` placeholders pass through verbatim:
 * the expansion runs BEFORE the secrets filter, which resolves them like hand-written labels.
 *
 * `UnauthorizedBehavior` is deliberately left at the plugin default `Auto` (v0.16.0+): an
 * unauthenticated request is answered with a redirect to Keycloak only when its `Accept` header
 * leads with `text/html`, and with a 401 problem document otherwise. The same middleware fronts
 * browser routes and `/api` routes, so a machine caller is better served by the 401 than by a 302
 * pointing at an HTML login form. Consequence for diagnosis: a bare `curl -I` against a guarded
 * host returns 401 while the browser logs in fine — that is health, not an outage (#1427). See
 * `internal/closed/auth.neckar.it/integration-guide.md` for the reproduction commands.
 */
fun AbstractCopyTask.expandOidcMiddlewareLabels() {
  // Fingerprint the generated block as task input — a change to the generator must re-materialize
  // consumers, and no source file of theirs reflects it.
  // Every optional parameter appears in [FingerprintDirectives], otherwise a change to a label
  // only some directives produce would leave this property untouched: Gradle would call the
  // materialization up to date and the edit would never reach a server.
  inputs.property(
    "oidcMiddlewareLabelBlock",
    FingerprintDirectives.joinToString("\n") { expandOidcMiddlewareDirective(it) },
  )

  // Registered at configuration time so this filter runs BEFORE the doFirst-registered secrets
  // filter, which must see the expanded labels to resolve their `${…}` placeholders. Do NOT move
  // into doFirst.
  filter { line -> expandOidcMiddlewareDirective(line) }
}

/**
 * Expands [line] if it is an `# @oidc-middleware:` directive; returns it unchanged otherwise.
 * Throws on missing/unknown/malformed/duplicate parameters and on a near-miss marker (e.g.
 * forgotten colon) — a typo fails the build instead of deploying a router whose middleware
 * never gets defined.
 */
internal fun expandOidcMiddlewareDirective(line: String): String {
  val match = DirectiveRegex.matchEntire(line)
  if (match == null) {
    require(MarkerRegex.containsMatchIn(line).not()) {
      "Malformed @oidc-middleware directive (expected `# @oidc-middleware: key=value …`): [$line]"
    }
    return line
  }
  val indent = match.groupValues[1]

  val rawParameters: List<String> = match.groupValues[2].trim().split(WhitespaceRegex)

  // Before the values are read: `name=oidc-a name=` is a duplicate, not an empty value.
  rawParameters.map { it.substringBefore("=") }.groupingBy { it }.eachCount().forEach { (key, count) ->
    require(count == 1) { "Duplicate @oidc-middleware parameter key [$key] in [$line]" }
  }

  val parameters: Map<String, String> = rawParameters.associate { parameter ->
    val separatorIndex = parameter.indexOf('=')
    require(separatorIndex > 0) { "Malformed @oidc-middleware parameter (expected key=value): [$parameter] in [$line]" }
    val value = parameter.substring(separatorIndex + 1)
    require(value.isNotEmpty()) { "Empty @oidc-middleware parameter value: [$parameter] in [$line]" }
    parameter.take(separatorIndex) to value
  }
  parameters.keys.forEach { key ->
    require(key in KnownParameters) {
      "Unknown @oidc-middleware parameter [$key] in [$line] — known parameters: $KnownParameters"
    }
  }

  fun mandatory(key: String): String = parameters[key].requireNotNull {
    "Missing mandatory @oidc-middleware parameter [$key] in [$line]"
  }

  val middlewareName = OidcMiddlewareName(mandatory("name"))
  val clientId = mandatory("client-id")
  val clientSecret = parameters["client-secret"]

  // Checked here rather than only in [CallbackUri], so the message names the directive line.
  val callbackUri = parameters["callback-uri"]
  require(callbackUri == null || callbackUri.startsWith("/")) {
    "@oidc-middleware parameter callback-uri must be a path starting with a slash but was [$callbackUri] in [$line]"
  }

  // Both or neither: a claim name without accepted values would assert nothing, and accepted
  // values without a claim name have nothing to assert on. Either half alone is a typo that would
  // otherwise deploy an unguarded route. Reported separately so the message names the half that
  // is missing rather than leaving the author to work it out.
  val assertedClaim = parameters["assert-claim"]
  val acceptedValues = parameters["any-of"]
  require(assertedClaim == null || acceptedValues != null) {
    "@oidc-middleware parameter assert-claim needs any-of alongside it in [$line]"
  }
  require(acceptedValues == null || assertedClaim != null) {
    "@oidc-middleware parameter any-of needs assert-claim alongside it in [$line]"
  }
  // Checked here rather than only in [ClaimValue], so the message names the directive line.
  require(acceptedValues == null || acceptedValues.split(",").none { it.isBlank() }) {
    "@oidc-middleware parameter any-of must not contain an empty value but was [$acceptedValues] in [$line]"
  }

  val forwardToken = parameters["forward-token"]
  require(forwardToken == null || forwardToken == BearerForwardToken) {
    "@oidc-middleware parameter forward-token accepts only [$BearerForwardToken] but was [$forwardToken] in [$line]"
  }

  val sessionMaxAge = parameters["session-max-age"]?.let { value ->
    value.toIntOrNull().requireNotNull {
      "@oidc-middleware parameter session-max-age must be an integer (seconds) but was [$value] in [$line]"
    }.also { parsed ->
      require(parsed > 0) { "@oidc-middleware parameter session-max-age must be > 0 but was [$parsed] in [$line]" }
    }
  }?.seconds ?: OidcMiddlewareOptions.DefaultSessionMaxAge

  val options = OidcMiddlewareOptions(
    clientId = KeycloakClientId(clientId),
    encryptionSecret = SessionEncryptionSecret($$"${traefik-oidc-encryption-secret}"),
    clientSecret = clientSecret?.let { ClientSecret(it) },
    callbackUri = callbackUri?.let { CallbackUri(it) },
    sessionMaxAge = sessionMaxAge,
    tokenForwarding = if (forwardToken == BearerForwardToken) TokenForwarding.Bearer else TokenForwarding.None,
    claimAssertion = if (assertedClaim == null || acceptedValues == null) null else
      ClaimAssertion(ClaimName(assertedClaim), acceptedValues.split(",").map { ClaimValue(it) }),
  )

  return buildList {
    add("$indent# ==== BEGIN oidc-middleware labels: $middlewareName (generated — edit the @oidc-middleware directive in the source compose) ====")
    options.optionPaths().forEach { (optionPath, value) ->
      add("""$indent- "traefik.http.middlewares.$middlewareName.plugin.traefik-oidc-auth.$optionPath=$value"""")
    }
    add("$indent# ==== END oidc-middleware labels: $middlewareName ====")
  }.joinToString("\n")
}

/** The only accepted `forward-token` value — see [expandOidcMiddlewareDirective]. */
private const val BearerForwardToken: String = "bearer"

internal val KnownParameters: Set<String> =
  setOf(
    "name", "client-id", "client-secret", "callback-uri", "session-max-age",
    "assert-claim", "any-of", "forward-token",
  )

private val DirectiveRegex = Regex("""^(\s*)#\s*@oidc-middleware:\s*(.+)$""")

/**
 * Comment line that starts like the directive marker but fails [DirectiveRegex] → rejected.
 * Anchored at line start so prose mentions of the directive do not match.
 */
private val MarkerRegex = Regex("""^\s*#\s*@oidc-middleware""")

private val WhitespaceRegex = Regex("""\s+""")

/**
 * Canonical directives used to fingerprint the generated block as a task input property.
 *
 * Two of them, because one cannot reach every branch: the minimal directive covers the defaults
 * (notably [OidcMiddlewareOptions.DefaultSessionMaxAge], which only applies when
 * `session-max-age` is absent),
 * the maximal one covers every optional label. A new optional parameter belongs in the maximal
 * directive — see [expandOidcMiddlewareLabels] for what goes wrong when it is missing.
 */
internal val FingerprintDirectives: List<String> = listOf(
  $$"# @oidc-middleware: name=oidc-fingerprint client-id=${fingerprint-client-id} client-secret=${fingerprint-client-secret}",
  $$"# @oidc-middleware: name=oidc-fingerprint-full client-id=${fingerprint-client-id} client-secret=${fingerprint-client-secret} callback-uri=/fingerprint/oidc/callback session-max-age=1 forward-token=bearer assert-claim=fingerprint-claim any-of=/fingerprint",
)
