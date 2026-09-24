package org.eazyportal.gradle.utils.signing

import java.io.File
import java.io.FileNotFoundException

/**
 * A throwaway, ASCII-armored PGP keypair (RSA 2048, passphrase-protected) used only to exercise
 * `publication-convention`'s release-gated `useInMemoryPgpKeys(SIGNING_KEY, SIGNING_PASSWORD)` wiring
 * (design §4b.1, TOOLS-72). Not tied to any real identity or used for anything but these tests.
 */
object SigningKeyFixtures {

    val TEST_SIGNING_KEY: String = this
        .javaClass
        .classLoader
        .getResource("test-signed-key.asc")
        ?.readText()
        ?: throw FileNotFoundException("test-signed-key.asc.txt not found in test resources")

    const val TEST_SIGNING_PASSWORD: String = "test-signing-passphrase"

}
