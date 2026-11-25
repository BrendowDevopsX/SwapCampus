package com.swapcampus.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.swapcampus.R

/**
 * Filtre par fourchette de prix (UC3).
 *
 * La puce affiche « Prix » quand aucun bornage n'est actif, sinon la fourchette
 * courante. Le dialogue ne valide pas les bornes entre elles : une borne
 * inversée donne simplement un résultat vide, ce qui reste compréhensible.
 */
@Composable
fun PuceFiltrePrix(
    prixMin: Double?,
    prixMax: Double?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val actif = prixMin != null || prixMax != null

    val libelle = if (actif) {
        stringResource(
            R.string.filtre_prix_resume,
            prixMin?.let { formaterPrix(it) } ?: "…",
            prixMax?.let { formaterPrix(it) } ?: "…"
        )
    } else {
        stringResource(R.string.filtre_prix_bouton)
    }

    PuceFiltre(
        libelle = libelle,
        selectionne = actif,
        onClick = onClick,
        glyphe = "💰",
        testTag = "filtre_prix",
        modifier = modifier
    )
}

/**
 * Dialogue de saisie des bornes de prix.
 *
 * @param onValider reçoit les bornes saisies (`null` = pas de borne).
 */
@Composable
fun DialogueFiltrePrix(
    prixMin: Double?,
    prixMax: Double?,
    onValider: (Double?, Double?) -> Unit,
    onFermer: () -> Unit
) {
    var minSaisi by rememberSaveable(prixMin) {
        mutableStateOf(prixMin?.let { formaterSaisie(it) }.orEmpty())
    }
    var maxSaisi by rememberSaveable(prixMax) {
        mutableStateOf(prixMax?.let { formaterSaisie(it) }.orEmpty())
    }

    AlertDialog(
        onDismissRequest = onFermer,
        title = { Text(stringResource(R.string.filtre_prix_titre)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = minSaisi,
                        onValueChange = { if (saisiePrixValide(it)) minSaisi = it },
                        label = { Text(stringResource(R.string.filtre_prix_min)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("champ_prix_min")
                    )
                    OutlinedTextField(
                        value = maxSaisi,
                        onValueChange = { if (saisiePrixValide(it)) maxSaisi = it },
                        label = { Text(stringResource(R.string.filtre_prix_max)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("champ_prix_max")
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onValider(minSaisi.toPrix(), maxSaisi.toPrix())
                    onFermer()
                },
                modifier = Modifier.testTag("bouton_appliquer_prix")
            ) {
                Text(stringResource(R.string.filtre_prix_appliquer))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onValider(null, null)
                    onFermer()
                },
                modifier = Modifier.testTag("bouton_effacer_prix")
            ) {
                Text(stringResource(R.string.filtre_prix_effacer))
            }
        }
    )
}

/**
 * Une borne de prix accepte la virgule ou le point décimal, et au plus deux
 * décimales (même règle que le champ prix du formulaire).
 */
private val REGEX_SAISIE_PRIX = Regex("^\\d{0,6}([.,]\\d{0,2})?$")

private fun saisiePrixValide(saisie: String): Boolean =
    saisie.isEmpty() || saisie.matches(REGEX_SAISIE_PRIX)

private fun String.toPrix(): Double? = trim()
    .replace(',', '.')
    .toDoubleOrNull()
    ?.takeIf { it >= 0.0 }

private fun formaterSaisie(prix: Double): String =
    if (prix % 1.0 == 0.0) prix.toInt().toString() else prix.toString().replace('.', ',')

/**
 * Raccourci pratique pour les écrans : garde l'état d'ouverture du dialogue et
 * délègue le filtrage au ViewModel.
 */
@Composable
fun FiltrePrix(
    prixMin: Double?,
    prixMax: Double?,
    onFiltre: (Double?, Double?) -> Unit,
    modifier: Modifier = Modifier
) {
    var ouvert by remember { mutableStateOf(false) }

    PuceFiltrePrix(
        prixMin = prixMin,
        prixMax = prixMax,
        onClick = { ouvert = true },
        modifier = modifier
    )

    if (ouvert) {
        DialogueFiltrePrix(
            prixMin = prixMin,
            prixMax = prixMax,
            onValider = onFiltre,
            onFermer = { ouvert = false }
        )
    }
}
