package com.flux.mobile.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flux.mobile.R

/** Main area of the app (tabs are added with the notes, categories and settings features). */
@Composable
fun MainScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val name by viewModel.userName.collectAsStateWithLifecycle()
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Box(Modifier.safeDrawingPadding(), contentAlignment = Alignment.Center) {
            Text(
                text = name?.let { stringResource(R.string.home_greeting, it) } ?: stringResource(R.string.home_greeting_anonymous),
                style = MaterialTheme.typography.headlineMedium,
            )
        }
    }
}
