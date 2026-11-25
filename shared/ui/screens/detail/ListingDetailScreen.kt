package com.swapcampus.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swapcampus.domain.Annonce
import com.swapcampus.domain.Disponible
import com.swapcampus.domain.Reservee
import com.swapcampus.domain.Vendue
import com.swapcampus.domain.libelle
import com.swapcampus.ui.components.ConfirmDialog
import com.swapcampus.ui.components.EmptyState
import com.swapcampus.ui.components.ListingThumbnail
import com.swapcampus.ui.components.LoadingIndicator
import com.swapcampus.ui.components.SectionTitle
import com.swapcampus.ui.components.StatusBadge
import com.swapcampus.ui.components.formaterDate
import com.swapcampus.ui.components.formaterPrix
import com.swapcampus.ui.components.initiales
import com.swapcampus.ui.theme.DisponibleFond
import com.swapcampus.ui.theme.DisponibleTexte
import com.swapcampus.ui.theme.ReserveeFond
import com.swapcampus.ui.theme.ReserveeTexte
import com.swapcampus.ui.theme.VendueFond
import com.swapcampus.ui.theme.VendueTexte
import com.swapcampus.ui.theme.style

/**
 * Fiche détaillée d'une annonce (UC4, UC5, UC6).
 *
 * Le propriétaire dispose des actions de gestion ; un visiteur connecté peut
 * contacter le vendeur. Les vérifications de propriété et de transition sont
 * faites dans le ViewModel / le domaine, pas ici.
 */
@Composable
fun ListingDetailScreen(
    listingId: Int,
    onEdit: () -> Unit,
    onDeleted: () -> Unit,
    onBack: () -> Unit,
    onLogin: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ListingDetailViewModel = viewModel(key = "detail_$listingId")
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val utilisateur by viewModel.utilisateurCourant.collectAsStateWithLifecycle()
    val erreurAction by viewModel.actionError.collectAsStateWithLifecycle()

    var afficherSuppression by remember { mutableStateOf(false) }
    var afficherReservation by remember { mutableStateOf(false) }
    var afficherContact by remember { mutableStateOf(false) }
    var acheteur by remember { mutableStateOf("") }

    LaunchedEffect(listingId) { viewModel.load(listingId) }

    when (val etat = uiState) {
        DetailUiState.Loading -> LoadingIndicator()

        is DetailUiState.Error -> EmptyState(
            titre = "Annonce introuvable",
            message = etat.message,
            glyphe = "🔍",
            actionLabel = "Retour au catalogue",
            onAction = onBack
        )

        is DetailUiState.Ready -> {
            val annonce = etat.annonce
            val estProprietaire = utilisateur?.id == annonce.auteur.id

            Column(modifier = modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Entete(
                        annonce = annonce,
                        estProprietaire = estProprietaire,
                        onBack = onBack,
                        onEdit = onEdit,
                        onSupprimer = { afficherSuppression = true }
                    )

                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                        PuceCategorieDetail(annonce = annonce)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = annonce.titre,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = formaterPrix(annonce.prix),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (annonce.etat !is Disponible) {
                            Spacer(Modifier.height(12.dp))
                            BandeauEtat(annonce = annonce)
                        }

                        Spacer(Modifier.height(20.dp))
                        BlocVendeur(annonce = annonce)

                        Spacer(Modifier.height(20.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(20.dp))

                        SectionTitle(titre = "Description")
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = annonce.description.ifBlank { "Aucune description fournie." },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(28.dp))
                    }
                }

                if (erreurAction != null) {
                    Text(
                        text = erreurAction.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }

                BarreActions(
                    annonce = annonce,
                    estProprietaire = estProprietaire,
                    connecte = utilisateur != null,
                    onReserver = { afficherReservation = true },
                    onMarquerVendue = { viewModel.marquerVendue(annonce.id) },
                    onAnnulerReservation = { viewModel.annulerReservation(annonce.id) },
                    onRemettreEnVente = { viewModel.annulerReservation(annonce.id) },
                    onModifier = onEdit,
                    onContacter = {
                        if (utilisateur == null) onLogin() else afficherContact = true
                    }
                )
            }

            if (afficherSuppression) {
                ConfirmDialog(
                    title = "Supprimer l'annonce ?",
                    message = "« " + annonce.titre + " » sera définitivement retirée du catalogue.",
                    confirmLabel = "Supprimer",
                    dismissLabel = "Annuler",
                    onConfirm = {
                        afficherSuppression = false
                        viewModel.supprimer(annonce.id, onDeleted)
                    },
                    onDismiss = { afficherSuppression = false }
                )
            }

            if (afficherReservation) {
                AlertDialog(
                    onDismissRequest = { afficherReservation = false },
                    title = { Text("Réserver l'annonce") },
                    text = {
                        Column {
                            Text(
                                "Indiquez le nom de l'acheteur : l'annonce passera en « Réservée » " +
                                    "et ne sera plus visible dans le catalogue.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = acheteur,
                                onValueChange = { acheteur = it },
                                label = { Text("Nom de l'acheteur") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("champ_acheteur")
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.reserver(annonce.id, acheteur)
                                afficherReservation = false
                            }
                        ) { Text("Réserver") }
                    },
                    dismissButton = {
                        TextButton(onClick = { afficherReservation = false }) { Text("Annuler") }
                    }
                )
            }

            if (afficherContact) {
                AlertDialog(
                    onDismissRequest = { afficherContact = false },
                    title = { Text("Contacter le vendeur") },
                    text = {
                        Column {
                            Text(
                                annonce.auteur.nom + " est joignable à l'adresse suivante :",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = annonce.auteur.email,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { afficherContact = false }) { Text("Fermer") }
                    }
                )
            }
        }
    }
}

@Composable
private fun Entete(
    annonce: Annonce,
    estProprietaire: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onSupprimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
    ) {
        ListingThumbnail(
            categorie = annonce.categorie,
            photoUrl = annonce.photoUrl,
            modifier = Modifier.fillMaxSize(),
            corner = 0.dp,
            glypheTaille = 72.sp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RondAction(onClick = onBack, etiquette = "Retour") {
                GlypheAction("←")
            }
            Spacer(Modifier.weight(1f))
            if (estProprietaire) {
                RondAction(onClick = onEdit, etiquette = "Modifier") {
                    GlypheAction("✎")
                }
                Spacer(Modifier.size(8.dp))
                RondAction(onClick = onSupprimer, etiquette = "Supprimer") {
                    GlypheAction("🗑")
                }
                Spacer(Modifier.size(8.dp))
            }
            StatusBadge(etat = annonce.etat)
        }
    }
}

@Composable
private fun RondAction(
    onClick: () -> Unit,
    etiquette: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .size(40.dp)
            .testTag("action_$etiquette")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) { content() }
    }
}

/** Glyphe texte utilisé à la place d'une icône (aucune dépendance d'icônes dans le projet). */
@Composable
private fun GlypheAction(glyphe: String, taille: androidx.compose.ui.unit.TextUnit = 16.sp) {
    Text(
        text = glyphe,
        fontSize = taille,
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
private fun PuceCategorieDetail(annonce: Annonce, modifier: Modifier = Modifier) {
    val styleCategorie = annonce.categorie.style()
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(percent = 50),
        color = styleCategorie.couleurs.first().copy(alpha = 0.18f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = styleCategorie.glyphe, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.size(6.dp))
            Text(
                text = annonce.categorie.libelle(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun BandeauEtat(annonce: Annonce, modifier: Modifier = Modifier) {
    val etat = annonce.etat
    val fond: androidx.compose.ui.graphics.Color
    val texte: androidx.compose.ui.graphics.Color
    val message: String

    when (etat) {
        is Reservee -> {
            fond = ReserveeFond
            texte = ReserveeTexte
            message = "Réservée pour " + etat.acheteur
        }

        is Vendue -> {
            fond = VendueFond
            texte = VendueTexte
            message = "Vendue le " + formaterDate(etat.dateVente)
        }

        is Disponible -> {
            fond = DisponibleFond
            texte = DisponibleTexte
            message = "Disponible"
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = fond
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.labelLarge,
            color = texte,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun BlocVendeur(annonce: Annonce, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initiales(annonce.auteur.nom),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(Modifier.size(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = annonce.auteur.nom,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Publiée le " + formaterDate(annonce.dateCreation),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = annonce.auteur.email,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BarreActions(
    annonce: Annonce,
    estProprietaire: Boolean,
    connecte: Boolean,
    onReserver: () -> Unit,
    onMarquerVendue: () -> Unit,
    onAnnulerReservation: () -> Unit,
    onRemettreEnVente: () -> Unit,
    onModifier: () -> Unit,
    onContacter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (estProprietaire) {
                when (annonce.etat) {
                    is Disponible -> Button(
                        onClick = onReserver,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bouton_reserver")
                    ) { Text("Réserver à un acheteur") }

                    is Reservee -> {
                        Button(
                            onClick = onMarquerVendue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bouton_vendre")
                        ) { Text("Marquer comme vendue") }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onAnnulerReservation,
                                modifier = Modifier.weight(1f)
                            ) { Text("Annuler la réservation") }
                            OutlinedButton(
                                onClick = onModifier,
                                modifier = Modifier.weight(1f)
                            ) { Text("Modifier") }
                        }
                    }

                    is Vendue -> Button(
                        onClick = onRemettreEnVente,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bouton_remettre")
                    ) { Text("Remettre en vente") }
                }
            } else {
                Button(
                    onClick = onContacter,
                    enabled = annonce.etat is Disponible,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bouton_contacter")
                ) {
                    Text(
                        if (connecte) {
                            "Contacter " + annonce.auteur.nom
                        } else {
                            "Se connecter pour contacter le vendeur"
                        }
                    )
                }
            }
        }
    }
}
