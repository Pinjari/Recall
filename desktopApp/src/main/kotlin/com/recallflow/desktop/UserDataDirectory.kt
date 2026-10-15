package com.recallflow.desktop

import java.nio.file.Path
import java.util.Locale

internal fun userDataDirectory(
    environment: Map<String, String> = System.getenv(),
    osName: String = System.getProperty("os.name"),
    userHome: String = System.getProperty("user.home"),
    overrideDirectory: String? = System.getProperty("recallflow.dataDir"),
): Path {
    val configured = overrideDirectory?.takeIf(String::isNotBlank)
        ?: environment["RECALLFLOW_DATA_DIR"]?.takeIf(String::isNotBlank)
    if (configured != null) {
        return Path.of(configured).also {
            require(it.isAbsolute) { "RecallFlow data directory must be an absolute path" }
        }.normalize()
    }

    val home = Path.of(userHome)
    require(home.isAbsolute) { "user.home must be an absolute path" }
    val os = osName.lowercase(Locale.ROOT)
    return when {
        os.startsWith("windows") -> {
            val localAppData = environment["LOCALAPPDATA"]
                ?.takeIf(String::isNotBlank)?.let(Path::of)?.takeIf(Path::isAbsolute)
            (localAppData ?: home.resolve("AppData").resolve("Local")).resolve("RecallFlow")
        }
        os.contains("mac") || os.contains("darwin") ->
            home.resolve("Library").resolve("Application Support").resolve("RecallFlow")
        else -> {
            val xdgDataHome = environment["XDG_DATA_HOME"]
                ?.takeIf(String::isNotBlank)?.let(Path::of)?.takeIf(Path::isAbsolute)
            (xdgDataHome ?: home.resolve(".local").resolve("share")).resolve("recallflow")
        }
    }.normalize()
}
