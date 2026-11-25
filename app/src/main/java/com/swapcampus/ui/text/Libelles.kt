package com.swapcampus.ui.text

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.swapcampus.R
import com.swapcampus.domain.Categorie
import com.swapcampus.domain.Disponible
import com.swapcampus.domain.ErreurMetier
import com.swapcampus.domain.ErreurMetierException
import com.swapcampus.domain.EtatAnnonce
import com.swapcampus.domain.Reservee
import com.swapcampus.domain.RoleUtilisateur
import com.swapcampus.domain.Vendue
import com.swapcampus.ui.components.formaterDate

/**
 * Traduction des types du domaine en chaînes d'interface.
 *
 * Le domaine reste sans texte (cahier des charges §10.2 : aucune chaîne en dur)
 * et la couche UI choisit l'identifiant de ressource correspondant, ce qui rend
 * la traduction FR/EN automatique (§8.7).
 */

@StringRes
fun resCategorie(categorie: Categorie): Int = when (categorie) {
    Categorie.LIVRES -> R.string.categorie_livres
    Categorie.COURS -> R.string.categorie_cours
    Categorie.ELECTRONIQUE -> R.string.categorie_electronique
    Categorie.SPORT_LOISIRS -> R.string.categorie_sport
    Categorie.MOBILIER -> R.string.categorie_mobilier
    Categorie.VETEMENTS -> R.string.categorie_vetements
    Categorie.AUTRE -> R.string.categorie_autre
}

@Composable
fun Categorie.libelle(): String = stringResource(resCategorie(this))

@StringRes
fun resEtatCourt(etat: EtatAnnonce): Int = when (etat) {
    is Disponible -> R.string.etat_disponible
    is Reservee -> R.string.etat_reservee
    is Vendue -> R.string.etat_vendue
}

@Composable
fun EtatAnnonce.libelleCourt(): String = stringResource(resEtatCourt(this))

/** Libellé détaillé : « Disponible », « Réservée par X », « Vendue le 12 nov. 2026 ». */
@Composable
fun EtatAnnonce.libelleComplet(): String = when (this) {
    is Disponible -> stringResource(R.string.etat_disponible)
    is Reservee -> stringResource(R.string.etat_reservee_par, acheteur)
    is Vendue -> stringResource(R.string.etat_vendue_le, formaterDate(dateVente))
}

@StringRes
fun resRole(role: RoleUtilisateur): Int = when (role) {
    RoleUtilisateur.ETUDIANT -> R.string.role_etudiant
    RoleUtilisateur.ADMINISTRATEUR -> R.string.role_administrateur
}

@Composable
fun RoleUtilisateur.libelle(): String = stringResource(resRole(this))

@StringRes
fun resErreurMetier(erreur: ErreurMetier): Int = when (erreur) {
    ErreurMetier.TITRE_VIDE -> R.string.erreur_titre_vide
    ErreurMetier.TITRE_TROP_LONG -> R.string.erreur_titre_trop_long
    ErreurMetier.DESCRIPTION_TROP_LONGUE -> R.string.erreur_description_trop_longue
    ErreurMetier.PRIX_INVALIDE -> R.string.erreur_prix_invalide
    ErreurMetier.ACHETEUR_OBLIGATOIRE -> R.string.erreur_acheteur_obligatoire
    ErreurMetier.MOTIF_OBLIGATOIRE -> R.string.erreur_motif_obligatoire
    ErreurMetier.MOTIF_TROP_LONG -> R.string.erreur_motif_trop_long
    ErreurMetier.TRANSITION_INTERDITE -> R.string.erreur_transition_interdite
    ErreurMetier.ANNONCE_INTROUVABLE -> R.string.erreur_annonce_introuvable
    ErreurMetier.NON_PROPRIETAIRE -> R.string.erreur_non_proprietaire
    ErreurMetier.NON_ADMINISTRATEUR -> R.string.erreur_non_administrateur
}

/**
 * Identifiant de ressource à afficher pour une erreur remontée par le domaine,
 * ou `null` si l'erreur n'est pas une erreur métier connue.
 */
@StringRes
fun resErreurDe(cause: Throwable?): Int? =
    (cause as? ErreurMetierException)?.let { resErreurMetier(it.erreur) }

@Composable
fun messageErreur(cause: Throwable?, @StringRes defaut: Int): String =
    stringResource(resErreurDe(cause) ?: defaut)
