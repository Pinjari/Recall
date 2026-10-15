package com.recallflow.desktop

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UserDataDirectoryTest {
    private val home = Path.of(System.getProperty("java.io.tmpdir")).toAbsolutePath().resolve("recallflow-home")

    @Test
    fun linuxUsesXdgDataHome() {
        val xdg = home.resolve("xdg")
        assertEquals(
            xdg.resolve("recallflow"),
            userDataDirectory(mapOf("XDG_DATA_HOME" to xdg.toString()), "Linux", home.toString(), null),
        )
    }

    @Test
    fun linuxIgnoresRelativeXdgDataHome() {
        assertEquals(
            home.resolve(".local/share/recallflow"),
            userDataDirectory(mapOf("XDG_DATA_HOME" to "relative"), "Linux", home.toString(), null),
        )
    }

    @Test
    fun macUsesApplicationSupport() {
        assertEquals(
            home.resolve("Library/Application Support/RecallFlow"),
            userDataDirectory(emptyMap(), "Mac OS X", home.toString(), null),
        )
    }

    @Test
    fun windowsUsesLocalAppData() {
        val local = home.resolve("local")
        assertEquals(
            local.resolve("RecallFlow"),
            userDataDirectory(mapOf("LOCALAPPDATA" to local.toString()), "Windows 11", home.toString(), null),
        )
    }

    @Test
    fun windowsFallsBackToHomeAppData() {
        assertEquals(
            home.resolve("AppData/Local/RecallFlow"),
            userDataDirectory(emptyMap(), "Windows 11", home.toString(), null),
        )
    }

    @Test
    fun propertyOverrideTakesPrecedenceOverEnvironment() {
        val property = home.resolve("property")
        assertEquals(
            property,
            userDataDirectory(
                mapOf("RECALLFLOW_DATA_DIR" to home.resolve("env").toString()),
                "Linux",
                home.toString(),
                property.toString(),
            ),
        )
    }

    @Test
    fun environmentOverrideIsSupported() {
        val directory = home.resolve("custom")
        assertEquals(
            directory,
            userDataDirectory(mapOf("RECALLFLOW_DATA_DIR" to directory.toString()), "Linux", home.toString(), null),
        )
    }

    @Test
    fun relativeOverrideIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            userDataDirectory(emptyMap(), "Linux", home.toString(), "relative")
        }
    }
}
