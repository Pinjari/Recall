package com.recallflow.desktop

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.recallflow.data.DesktopDatabaseDriverFactory
import com.recallflow.data.SqlDelightNoteRepository
import com.recallflow.ui.RecallFlowScreen
import com.recallflow.ui.resources.Res
import com.recallflow.ui.resources.app_name
import java.awt.EventQueue
import java.nio.file.Files
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess
import org.jetbrains.compose.resources.stringResource

fun main(args: Array<String>) {
    val smokeTest = "--smoke-test" in args
    val watchdog = if (smokeTest) {
        Thread.setDefaultUncaughtExceptionHandler { _, failure ->
            failure.printStackTrace(System.err)
            exitProcess(1)
        }
        Executors.newSingleThreadScheduledExecutor { runnable ->
            Thread(runnable, "RecallFlow smoke watchdog").apply { isDaemon = true }
        }.apply {
            schedule({
                System.err.println("RecallFlow smoke test timed out before the window closed")
                exitProcess(1)
            }, 30, TimeUnit.SECONDS)
        }
    } else {
        null
    }

    try {
        require(args.all { it == "--smoke-test" }) { "Supported option: --smoke-test" }
        val dataDirectory = Files.createDirectories(userDataDirectory())
        // Startup and closure run on the main thread, outside Compose's AWT render thread.
        val driver = DesktopDatabaseDriverFactory(dataDirectory.resolve("recallflow.db").toString())
            .createDriver()
        try {
            val repository = SqlDelightNoteRepository(driver)
            val frameRendered = AtomicBoolean(false)

            application(exitProcessOnExit = false) {
                Window(
                    onCloseRequest = ::exitApplication,
                    title = stringResource(Res.string.app_name),
                    state = rememberWindowState(width = 820.dp, height = 760.dp),
                ) {
                    val frameModifier = if (smokeTest) {
                        Modifier.drawWithContent {
                            drawContent()
                            if (frameRendered.compareAndSet(false, true)) {
                                // Wait until this real window's draw pass has returned to AWT.
                                EventQueue.invokeLater { exitApplication() }
                            }
                        }
                    } else {
                        Modifier
                    }
                    RecallFlowScreen(repository = repository, modifier = frameModifier)
                }
            }

            if (smokeTest) check(frameRendered.get()) { "Window closed without rendering a frame" }
        } finally {
            driver.close()
        }
    } catch (failure: Throwable) {
        failure.printStackTrace(System.err)
        exitProcess(1)
    } finally {
        watchdog?.shutdownNow()
    }
}
