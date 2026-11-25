package com.swapcampus.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.swapcampus.R

/**
 * Boîte de confirmation générique.
 *
 * Les libellés par défaut viennent des ressources (donc traduits) : seuls les
 * écrans qui ont besoin d'un verbe spécifique passent leurs propres libellés.
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String = stringResource(R.string.action_supprimer),
    dismissLabel: String = stringResource(R.string.action_annuler),
    testTag: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                modifier = if (testTag != null) Modifier.testTag("${testTag}_confirmer")
                else Modifier
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissLabel)
            }
        }
    )
}
