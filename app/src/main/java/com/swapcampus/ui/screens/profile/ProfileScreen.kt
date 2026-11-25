package com.swapcampus.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swapcampus.R
import com.swapcampus.domain.ActionModeration
import com.swapcampus.domain.CompteUtilisateur
import com.swapcampus.ui.components.EmptyState
import com.swapcampus.ui.components.SectionTitle
import com.swapcampus.ui.components.StatCard
import com.swapcampus.ui.components.formaterDate
import com.swapcampus.ui.components.initiales
import com.swapcampus.ui.text.libelle
import com.swapcampus.ui.theme.Amber
import com.swapcampus.ui.theme.Indigo
import com.swapcampus.ui.theme.Teal

/**
 * Profil : identité de l'utilisateur connecté, récapitulatif de son activité
 * de vendeur (UC7) et, pour un administrateur, journal de modération (§3.2).
 */
@Composable
fun ProfileScreen(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel()
) {
    val utilisateur by viewModel.currentUser.collectAsStateWithLifecycle()
    val statistiques by viewModel.statistiques.collectAsStateWithLifecycle()
    val estAdmin by viewModel.estAdministrateur.collectAsStateWithLifecycle()
    val moderations by viewModel.moderations.collectAsStateWithLifecycle()

    if (utilisateur == null) {
        Column(modifier = modifier.fillMaxSize().padding(top = 24.dp)) {
            EmptyState(
                titre = stringResource(R.string.profil_non_connecte_titre),
                message = stringResource(R.string.profil_non_connecte_message),
                glyphe = "👤",
                actionLabel = stringResource(R.string.profil_se_connecter),
                onAction = onLoginClick
            )
        }
        return
    }

    val compte = utilisateur ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        BlocIdentite(compte = compte)

        Spacer(Modifier.height(20.dp))

        SectionTitle(titre = stringResource(R.string.profil_mon_activite))
        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                valeur = statistiques.publiees,
                libelle = stringResource(R.string.mes_stat_publiees),
                accent = Indigo,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                valeur = statistiques.enLigne,
                libelle = stringResource(R.string.mes_stat_en_ligne),
                accent = Teal,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                valeur = statistiques.vendues,
                libelle = stringResource(R.string.mes_stat_vendues),
                accent = Amber,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(24.dp))

        SectionTitle(titre = stringResource(R.string.profil_infos))
        Spacer(Modifier.height(10.dp))

        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                LigneInfo(libelle = stringResource(R.string.profil_nom), valeur = compte.nom)
                LigneInfo(libelle = stringResource(R.string.profil_email), valeur = compte.email)
                LigneInfo(
                    libelle = stringResource(R.string.profil_role),
                    valeur = compte.role.libelle()
                )
                LigneInfo(
                    libelle = stringResource(R.string.profil_membre_depuis),
                    valeur = formaterDate(compte.dateCreation),
                    derniere = true
                )
            }
        }

        if (estAdmin) {
            Spacer(Modifier.height(24.dp))
            SectionTitle(titre = stringResource(R.string.profil_moderation))
            Spacer(Modifier.height(10.dp))
            JournalModeration(moderations)
        }

        Spacer(Modifier.height(28.dp))

        OutlinedButton(
            onClick = viewModel::logout,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("bouton_deconnexion"),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = stringResource(R.string.profil_deconnexion),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

/** Journal des suppressions motivées, visible par les administrateurs. */
@Composable
private fun JournalModeration(moderations: List<ActionModeration>, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth().testTag("liste_moderation"),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            if (moderations.isEmpty()) {
                Text(
                    text = stringResource(R.string.profil_moderation_vide),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                moderations.forEachIndexed { index, action ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                    Text(
                        text = stringResource(
                            R.string.profil_moderation_entree,
                            action.annonceTitre,
                            action.motif
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("moderation_${action.annonceId}")
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = formaterDate(action.date) + " · " + action.moderateur.nom,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BlocIdentite(compte: CompteUtilisateur, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initiales(compte.nom),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(Modifier.size(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = compte.nom,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(percent = 50),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = compte.role.libelle(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun LigneInfo(
    libelle: String,
    valeur: String,
    derniere: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = libelle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valeur,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    if (!derniere) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
