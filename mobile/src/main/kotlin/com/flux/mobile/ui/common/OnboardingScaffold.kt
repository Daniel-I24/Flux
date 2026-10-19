package com.flux.mobile.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.flux.core.designsystem.theme.FluxSizes
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.mobile.R

/** Shared layout for onboarding steps: progress, title, explanation, content and actions. */
@Composable
fun OnboardingScaffold(
    step: Int,
    totalSteps: Int,
    title: String,
    subtitle: String,
    primaryLabel: String,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
    primaryEnabled: Boolean = true,
    loading: Boolean = false,
    secondaryLabel: String? = null,
    onSecondary: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = modifier.fillMaxSize()) {
        Column(Modifier.safeDrawingPadding().imePadding().padding(FluxSpacing.xl)) {
            LinearProgressIndicator(progress = { step / totalSteps.toFloat() }, modifier = Modifier.fillMaxWidth())
            Text(
                text = stringResource(R.string.onboarding_progress, step, totalSteps),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = FluxSpacing.sm),
            )
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(vertical = FluxSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(FluxSpacing.lg),
            ) {
                Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
                Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                content()
            }
            Button(
                onClick = onPrimary,
                enabled = primaryEnabled && !loading,
                modifier = Modifier.fillMaxWidth().heightIn(min = FluxSizes.minTouchTarget),
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.heightIn(max = FluxSizes.iconMd)
                    )
                } else {
                    Text(primaryLabel)
                }
            }
            secondaryLabel?.let {
                TextButton(onClick = onSecondary, modifier = Modifier.fillMaxWidth().heightIn(min = FluxSizes.minTouchTarget)) { Text(it) }
            }
        }
    }
}
