package it.neckar.gradle

import org.gradle.api.tasks.AbstractCopyTask

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
 * - `client-id`, `client-secret` (mandatory) — Keycloak client (literal or `${…}` placeholder).
 * - `callback-uri` (optional) — overrides the plugin default `/oidc/callback` (Traefik dashboards,
 *   jitsi prosody).
 * - `session-max-age` (optional, default 604800 = 7 days) — `SessionCookie.MaxAge` in seconds; the
 *   cookie slides on every silent token renewal, so a login expires only after this long without a
 *   visit (#2306).
 *
 * The block always requests the optional `offline_access` scope; if the Keycloak client lacks it,
 * Keycloak ignores the request — login never breaks. `${…}` placeholders pass through verbatim:
 * the expansion runs BEFORE the secrets filter, which resolves them like hand-written labels.
 */
fun AbstractCopyTask.expandOidcMiddlewareLabels() {
  // Fingerprint the generated block as task input — template edits must re-materialize consumers.
  inputs.property("oidcMiddlewareLabelBlock", expandOidcMiddlewareDirective(FingerprintDirective))

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
  val parameters: Map<String, String> = rawParameters.associate { parameter ->
    val separatorIndex = parameter.indexOf('=')
    require(separatorIndex > 0) { "Malformed @oidc-middleware parameter (expected key=value): [$parameter] in [$line]" }
    val value = parameter.substring(separatorIndex + 1)
    require(value.isNotEmpty()) { "Empty @oidc-middleware parameter value: [$parameter] in [$line]" }
    parameter.take(separatorIndex) to value
  }
  require(parameters.size == rawParameters.size) { "Duplicate @oidc-middleware parameter key in [$line]" }

  parameters.keys.forEach { key ->
    require(key in KnownParameters) {
      "Unknown @oidc-middleware parameter [$key] in [$line] — known parameters: $KnownParameters"
    }
  }

  fun mandatory(key: String): String = parameters[key].requireNotNull {
    "Missing mandatory @oidc-middleware parameter [$key] in [$line]"
  }

  val middlewareName = mandatory("name")
  val clientId = mandatory("client-id")
  val clientSecret = mandatory("client-secret")
  val callbackUri = parameters["callback-uri"]
  val sessionMaxAge = parameters["session-max-age"]?.let { value ->
    value.toIntOrNull().requireNotNull {
      "@oidc-middleware parameter session-max-age must be an integer (seconds) but was [$value] in [$line]"
    }.also { parsed ->
      require(parsed >= 0) { "@oidc-middleware parameter session-max-age must be >= 0 but was [$parsed] in [$line]" }
    }
  } ?: DefaultSessionMaxAgeSeconds

  fun label(optionPath: String, value: String): String =
    """$indent- "traefik.http.middlewares.$middlewareName.plugin.traefik-oidc-auth.$optionPath=$value""""

  return buildList {
    add("$indent# ==== BEGIN oidc-middleware labels: $middlewareName (generated — edit the @oidc-middleware directive in the source compose) ====")
    // MUST be the full external URL incl. realm path (never an internal Docker service name) —
    // the browser is redirected here for the interactive login.
    add(label("Provider.Url", "https://auth.neckar.it/realms/main"))
    add(label("Provider.ClientId", clientId))
    add(label("Provider.ClientSecret", clientSecret))
    add(label("Provider.UsePkce", "true"))
    callbackUri?.let { add(label("CallbackUri", it)) }
    add(label("Secret", $$"${traefik-oidc-encryption-secret}"))
    add(label("Scopes[0]", "openid"))
    add(label("Scopes[1]", "profile"))
    add(label("Scopes[2]", "email"))
    add(label("Scopes[3]", "offline_access"))
    add(label("SessionCookie.MaxAge", sessionMaxAge.toString()))
    add(label("AuthorizationHeader.Name", "Authorization"))
    add("$indent# ==== END oidc-middleware labels: $middlewareName ====")
  }.joinToString("\n")
}

/** Persistent-session default (#2306): one interactive login lasts until 7 days pass without a visit. */
private const val DefaultSessionMaxAgeSeconds: Int = 604800

private val KnownParameters: Set<String> = setOf("name", "client-id", "client-secret", "callback-uri", "session-max-age")

private val DirectiveRegex = Regex("""^(\s*)#\s*@oidc-middleware:\s*(.+)$""")

/**
 * Comment line that starts like the directive marker but fails [DirectiveRegex] → rejected.
 * Anchored at line start so prose mentions of the directive do not match.
 */
private val MarkerRegex = Regex("""^\s*#\s*@oidc-middleware""")

private val WhitespaceRegex = Regex("""\s+""")

/** Canonical directive used to fingerprint the generated block as a task input property. */
private const val FingerprintDirective: String =
  $$"# @oidc-middleware: name=oidc-fingerprint client-id=${fingerprint-client-id} client-secret=${fingerprint-client-secret}"
