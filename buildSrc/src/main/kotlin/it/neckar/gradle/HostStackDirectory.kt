package it.neckar.gradle

import it.neckar.runtime.context.HostPath

/**
 * The directory on a host holding the host stack's compose file, the role fragments beside it and the host's own scripts.
 *
 * Its own file: [CommonComposeRole] reads it while its enum constants initialize, which a value in the same file as the role's copy rules
 * would not have yet.
 */
val HostStackDirectory: HostPath = HostPath("/srv/host")
