package com.swapcampus.domain

/**
 * Règles métier du catalogue, indépendantes d'Android et de Compose
 * (cahier des charges §10.5 : la validation se fait dans le domaine, pas dans
 * l'interface).
 *
 * Les erreurs sont typées ([ErreurMetier]) et non pas des messages : la couche
 * UI choisit la chaîne à afficher, ce qui permet la traduction FR/EN (§8.7).
 */
object ReglesAnnonce {

    /** Longueur maximale du titre d'une annonce. */
    const val TITRE_MAX: Int = 80

    /** Longueur maximale de la description. */
    const val DESCRIPTION_MAX: Int = 1_000

    /** Longueur maximale du motif d'une suppression par un administrateur. */
    const val MOTIF_MAX: Int = 200

    /**
     * Valide les champs d'une publication (UC1) ou d'une modification (UC4).
     *
     * @return `null` si tout est valide, sinon la première erreur rencontrée.
     */
    fun validerAnnonce(
        titre: String?,
        prix: Double?,
        description: String? = null
    ): ErreurMetier? = when {
        titre != null && titre.isBlank() -> ErreurMetier.TITRE_VIDE
        titre != null && titre.trim().length > TITRE_MAX -> ErreurMetier.TITRE_TROP_LONG
        description != null && description.length > DESCRIPTION_MAX ->
            ErreurMetier.DESCRIPTION_TROP_LONGUE
        prix != null && prix <= 0.0 -> ErreurMetier.PRIX_INVALIDE
        else -> null
    }

    /**
     * Table des transitions autorisées du cycle de vie d'une annonce (UC5) :
     * `Disponible → Réservée → Vendue`, avec retour possible à `Disponible`.
     */
    fun transitionAutorisee(depuis: EtatAnnonce, vers: EtatAnnonce): Boolean = when (depuis) {
        is Disponible -> vers is Reservee
        is Reservee -> vers is Vendue || vers is Disponible
        is Vendue -> vers is Disponible
    }

    /** Une réservation exige le nom de l'acheteur (§5, UC5). */
    fun validerReservation(acheteur: String?): ErreurMetier? =
        if (acheteur.isNullOrBlank()) ErreurMetier.ACHETEUR_OBLIGATOIRE else null

    /** Une suppression par un administrateur exige un motif (§5, UC6). */
    fun validerModeration(motif: String?): ErreurMetier? = when {
        motif.isNullOrBlank() -> ErreurMetier.MOTIF_OBLIGATOIRE
        motif.trim().length > MOTIF_MAX -> ErreurMetier.MOTIF_TROP_LONG
        else -> null
    }
}

/**
 * Erreurs métier, traduites par la couche UI.
 *
 * Le `message` technique n'est là que pour les journaux : il n'est jamais
 * affiché tel quel à l'utilisateur.
 */
enum class ErreurMetier(val message: String) {
    TITRE_VIDE("Titre vide"),
    TITRE_TROP_LONG("Titre trop long"),
    DESCRIPTION_TROP_LONGUE("Description trop longue"),
    PRIX_INVALIDE("Prix invalide"),
    ACHETEUR_OBLIGATOIRE("Acheteur non renseigné"),
    MOTIF_OBLIGATOIRE("Motif de modération obligatoire"),
    MOTIF_TROP_LONG("Motif de modération trop long"),
    TRANSITION_INTERDITE("Transition d'état interdite"),
    ANNONCE_INTROUVABLE("Annonce introuvable"),
    NON_PROPRIETAIRE("Action réservée au propriétaire"),
    NON_ADMINISTRATEUR("Action réservée à un administrateur");

    /** Exception porteuse, utilisée avec [Result.failure]. */
    fun exception(): ErreurMetierException = ErreurMetierException(this)
}

/** Exception métier portant une [ErreurMetier] exploitable par l'UI. */
class ErreurMetierException(val erreur: ErreurMetier) : Exception(erreur.message)
