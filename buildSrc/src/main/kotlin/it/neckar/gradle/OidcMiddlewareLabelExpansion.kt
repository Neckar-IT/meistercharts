package it.neckar.gradle

import it.neckar.traefik.ClientSecret
import it.neckar.traefik.KeycloakClientId
import it.neckar.traefik.OidcMiddlewareDirective
import it.neckar.traefik.SessionEncryptionSecret
import org.gradle.api.tasks.AbstractCopyTask

/**
 * Expands a `# @oidc-middleware:` directive inside a compose `labels:` list into the canonical
 * `traefik-oidc-auth` label block at materialization — single source of truth for the block,
 * compose analog of [inlineCommonShellIncludes]:
 * ```yaml
 * # @oidc-middleware: name=oidc-mea client-id=mea.neckar.it client-secret=${mea-keycloak-client-secret}
 * ```
 *
 * Parameters: [OidcMiddlewareDirective].
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
 * host returns 401 while the browser logs in fine — that is health, not an outage. See
 * `internal/closed/auth.neckar.it/integration-guide.md` for the reproduction commands.
 */
fun AbstractCopyTask.expandOidcMiddlewareLabels() {
  // No consumer's source reflects a generator change, so the generated block is the task input —
  // and every optional parameter belongs in [FingerprintDirectives], or its label never moves it.
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
 * Throws on a malformed directive — see [OidcMiddlewareDirective.parse].
 */
internal fun expandOidcMiddlewareDirective(line: String): String {
  if (OidcMiddlewareDirective.isDirective(line).not()) {
    return line
  }
  val indent = OidcMiddlewareDirective.indentOf(line)
  val directive = OidcMiddlewareDirective.parse(line)

  val options = directive.options(
    encryptionSecret = SessionEncryptionSecret($$"${traefik-oidc-encryption-secret}"),
    resolvedClientId = KeycloakClientId(directive.clientId.written),
    resolvedClientSecret = directive.clientSecret?.let { ClientSecret(it.written) },
  )

  return buildList {
    add("$indent# ==== BEGIN oidc-middleware labels: ${directive.name} (generated — edit the @oidc-middleware directive in the source compose) ====")
    options.optionPaths().forEach { (optionPath, value) ->
      add("""$indent- "traefik.http.middlewares.${directive.name}.plugin.traefik-oidc-auth.$optionPath=$value"""")
    }
    add("$indent# ==== END oidc-middleware labels: ${directive.name} ====")
  }.joinToString("\n")
}

/**
 * Canonical directives used to fingerprint the generated block as a task input property.
 *
 * Two of them, because one cannot reach every branch: the minimal directive covers the defaults
 * (notably [it.neckar.traefik.OidcMiddlewareOptions.DefaultSessionMaxAge], which only applies when
 * `session-max-age` is absent),
 * the maximal one covers every optional label. A new optional parameter belongs in the maximal
 * directive — see [expandOidcMiddlewareLabels] for what goes wrong when it is missing.
 */
internal val FingerprintDirectives: List<String> = listOf(
  $$"# @oidc-middleware: name=oidc-fingerprint client-id=${fingerprint-client-id} client-secret=${fingerprint-client-secret}",
  $$"# @oidc-middleware: name=oidc-fingerprint-full client-id=${fingerprint-client-id} client-secret=${fingerprint-client-secret} callback-uri=/fingerprint/oidc/callback session-max-age=1 forward-token=bearer assert-claim=fingerprint-claim any-of=/fingerprint",
)
