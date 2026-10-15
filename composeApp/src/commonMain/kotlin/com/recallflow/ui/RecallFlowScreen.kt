package com.recallflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.recallflow.ui.resources.Res
import com.recallflow.ui.resources.app_name
import com.recallflow.ui.resources.brand_monogram
import com.recallflow.ui.resources.foundation_label
import com.recallflow.ui.resources.notebook_body
import com.recallflow.ui.resources.notebook_heading
import com.recallflow.ui.resources.notes_saved
import com.recallflow.ui.resources.platform_footer
import com.recallflow.ui.resources.scope_body
import com.recallflow.ui.resources.scope_heading
import com.recallflow.ui.resources.storage_opening
import com.recallflow.ui.resources.storage_unavailable
import com.recallflow.ui.resources.welcome_body
import com.recallflow.ui.resources.welcome_heading
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private val LightColors = lightColorScheme(
    primary = Color(0xFF245E4B),
    onPrimary = Color.White,
    background = Color(0xFFF5F5EF),
    onBackground = Color(0xFF202B25),
    surface = Color(0xFFFFFEF9),
    onSurface = Color(0xFF202B25),
    surfaceVariant = Color(0xFFE3EBE1),
    onSurfaceVariant = Color(0xFF46554C),
    outlineVariant = Color(0xFFD3DCD1),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA2D4B9),
    onPrimary = Color(0xFF123B2C),
    background = Color(0xFF141C18),
    onBackground = Color(0xFFE1E9DF),
    surface = Color(0xFF1D2821),
    onSurface = Color(0xFFE1E9DF),
    surfaceVariant = Color(0xFF2C3B31),
    onSurfaceVariant = Color(0xFFBACBBE),
    outlineVariant = Color(0xFF405147),
)

@Composable
fun RecallFlowScreen(
    modifier: Modifier = Modifier,
    noteCount: Int? = null,
    storageUnavailable: Boolean = false,
) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors) {
        Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(
                modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 720.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(28.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                stringResource(Res.string.brand_monogram),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Text(
                            stringResource(Res.string.app_name),
                            modifier = Modifier.semantics { heading() },
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            stringResource(Res.string.foundation_label),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            stringResource(Res.string.welcome_heading),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            stringResource(Res.string.welcome_body),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }

                    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Text(
                                stringResource(Res.string.notebook_heading),
                                modifier = Modifier.semantics { heading() },
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = when {
                                    storageUnavailable -> stringResource(Res.string.storage_unavailable)
                                    noteCount != null -> pluralStringResource(Res.plurals.notes_saved, noteCount, noteCount)
                                    else -> stringResource(Res.string.storage_opening)
                                },
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Medium,
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Text(
                                stringResource(Res.string.notebook_body),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(22.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                stringResource(Res.string.scope_heading),
                                modifier = Modifier.semantics { heading() },
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                stringResource(Res.string.scope_body),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }

                    Text(
                        stringResource(Res.string.platform_footer),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}
