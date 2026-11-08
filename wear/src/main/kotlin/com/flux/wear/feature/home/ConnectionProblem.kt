package com.flux.wear.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.flux.core.model.PhoneConnection
import com.flux.wear.R
import com.flux.wear.ui.WatchDimens

data class ProblemCopy(val title: Int, val message: Int, val action: ProblemAction)

/** What the button of each problem does. OPEN_APP and LINK both open FLUX on the phone at Ajustes > Reloj. */
enum class ProblemAction { RETRY, OPEN_STORE, OPEN_APP, LINK, OPEN_SETTINGS, NONE }

/** One message per cause, so "not paired", "no FLUX", "no session" and "other account" never look alike. */
fun PhoneConnection.problemCopy(): ProblemCopy? = when (this) {
    PhoneConnection.NoPairedPhone -> ProblemCopy(R.string.problem_no_phone_title, R.string.problem_no_phone, ProblemAction.RETRY)
    PhoneConnection.AppNotInstalled -> ProblemCopy(
        R.string.problem_app_missing_title,
        R.string.problem_app_missing,
        ProblemAction.OPEN_STORE
    )
    PhoneConnection.PhoneUnreachable -> ProblemCopy(R.string.problem_unreachable_title, R.string.problem_unreachable, ProblemAction.RETRY)
    PhoneConnection.SetupIncomplete -> ProblemCopy(R.string.problem_setup_title, R.string.problem_setup, ProblemAction.OPEN_APP)
    PhoneConnection.NotLinked -> ProblemCopy(R.string.problem_not_linked_title, R.string.problem_not_linked, ProblemAction.LINK)
    PhoneConnection.LinkedToOtherAccount -> ProblemCopy(
        R.string.problem_other_account_title,
        R.string.problem_other_account,
        ProblemAction.OPEN_SETTINGS,
    )
    PhoneConnection.UpdateRequired -> ProblemCopy(R.string.problem_update_title, R.string.problem_update, ProblemAction.NONE)
    PhoneConnection.Checking, is PhoneConnection.Ready -> null
}

private fun ProblemAction.label(): Int? = when (this) {
    ProblemAction.RETRY -> R.string.problem_retry
    ProblemAction.OPEN_STORE -> R.string.problem_open_store
    ProblemAction.OPEN_APP -> R.string.problem_open_on_phone
    ProblemAction.LINK -> R.string.problem_link
    ProblemAction.OPEN_SETTINGS -> R.string.home_settings
    ProblemAction.NONE -> null
}

/** Clear message + the action that fixes it; centered so round screens never cut it. */
@Composable
fun ConnectionProblemCard(connection: PhoneConnection, onAction: (ProblemAction) -> Unit, modifier: Modifier = Modifier) {
    val copy = connection.problemCopy() ?: return
    Card(onClick = { onAction(copy.action) }, modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WatchDimens.gapSmall),
        ) {
            Text(stringResource(copy.title), style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
            Text(stringResource(copy.message), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            copy.action.label()?.let { CompactButton(onClick = { onAction(copy.action) }, label = { Text(stringResource(it)) }) }
        }
    }
}
