package com.flux.wear.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.flux.core.model.PhoneConnection
import com.flux.wear.R

private data class ProblemCopy(val title: Int, val message: Int, val action: ProblemAction)

enum class ProblemAction { RETRY, OPEN_ON_PHONE, OPEN_SETTINGS, NONE }

private fun PhoneConnection.copy(): ProblemCopy? = when (this) {
    PhoneConnection.NoPairedPhone -> ProblemCopy(R.string.problem_no_phone_title, R.string.problem_no_phone, ProblemAction.RETRY)
    PhoneConnection.AppNotInstalled -> ProblemCopy(
        R.string.problem_app_missing_title,
        R.string.problem_app_missing,
        ProblemAction.OPEN_ON_PHONE
    )
    PhoneConnection.PhoneUnreachable -> ProblemCopy(R.string.problem_unreachable_title, R.string.problem_unreachable, ProblemAction.RETRY)
    PhoneConnection.SetupIncomplete -> ProblemCopy(R.string.problem_setup_title, R.string.problem_setup, ProblemAction.OPEN_ON_PHONE)
    PhoneConnection.NotLinked -> ProblemCopy(R.string.problem_not_linked_title, R.string.problem_not_linked, ProblemAction.RETRY)
    PhoneConnection.LinkedToOtherAccount -> ProblemCopy(
        R.string.problem_other_account_title,
        R.string.problem_other_account,
        ProblemAction.OPEN_SETTINGS
    )
    PhoneConnection.UpdateRequired -> ProblemCopy(R.string.problem_update_title, R.string.problem_update, ProblemAction.NONE)
    PhoneConnection.Checking, is PhoneConnection.Ready -> null
}

/** Clear message + the action that fixes it, for each way the watch can fail to reach the phone. */
@Composable
fun ConnectionProblemCard(connection: PhoneConnection, onAction: (ProblemAction) -> Unit, modifier: Modifier = Modifier) {
    val copy = connection.copy() ?: return
    Card(onClick = { onAction(copy.action) }, modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(copy.title), style = MaterialTheme.typography.titleSmall)
            Text(stringResource(copy.message), style = MaterialTheme.typography.bodySmall)
            val label = when (copy.action) {
                ProblemAction.RETRY -> R.string.problem_retry
                ProblemAction.OPEN_ON_PHONE -> R.string.problem_open_on_phone
                ProblemAction.OPEN_SETTINGS -> R.string.home_settings
                ProblemAction.NONE -> null
            }
            label?.let { CompactButton(onClick = { onAction(copy.action) }, label = { Text(stringResource(it)) }) }
        }
    }
}
