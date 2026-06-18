package com.examflow.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.examflow.app.ExamFlowViewModel
import com.examflow.app.R

@Composable
fun SettingsScreen(viewModel: ExamFlowViewModel) {
    val context = LocalContext.current
    var showRestore by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
        )
        QuietCard {
            SettingRow(stringResource(R.string.app_version), "1.0.0")
            Spacer(Modifier.height(10.dp))
            SettingRow(stringResource(R.string.theme_info), stringResource(R.string.theme_value))
        }
        QuietCard {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    viewModel.exportJson { text ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("ExamFlow", text))
                        Toast.makeText(context, context.getString(R.string.backup_created), Toast.LENGTH_SHORT).show()
                    }
                }
            ) { Text(stringResource(R.string.backup)) }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { showRestore = true }) {
                Text(stringResource(R.string.restore))
            }
        }
        QuietCard {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    viewModel.resetAll {
                        Toast.makeText(context, context.getString(R.string.reset_complete), Toast.LENGTH_SHORT).show()
                    }
                }
            ) { Text(stringResource(R.string.data_reset)) }
        }
    }

    if (showRestore) {
        RestoreDialog(
            onDismiss = { showRestore = false },
            onRestore = { text ->
                viewModel.importJson(text) { success ->
                    Toast.makeText(
                        context,
                        context.getString(if (success) R.string.restore_complete else R.string.need_data),
                        Toast.LENGTH_SHORT
                    ).show()
                    if (success) showRestore = false
                }
            }
        )
    }
}

@Composable
private fun SettingRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RestoreDialog(onDismiss: () -> Unit, onRestore: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.restore)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.restore_hint)) },
                minLines = 5
            )
        },
        confirmButton = { TextButton(onClick = { onRestore(text) }) { Text(stringResource(R.string.save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
