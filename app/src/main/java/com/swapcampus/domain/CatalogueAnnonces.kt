package com.swapcampus.domain

/**
 * Catalogue en mémoire : source de vérité des annonces pour le TP1
 * (pas de base de données, voir cahier des charges §8.3).
 *
 * La classe ne connaît ni Compose ni Android : elle peut être testée en
 * JVM pur, puis réutilisée telle quelle dans le module partagé KMP.
 *
 * Les règles de validation et de transition sont dans [ReglesAnnonce] ; ce
 * catalogue se contente de les appliquer et de conserver l'historique.
 *
 * @param annoncesInitiales annonces de départ (jeu de démonstration), vide par défaut.
 */
class CatalogueAnnonces(
    annoncesInitiales: List<Annonce> = emptyList()
) {

    private val annonces: MutableList<Annonce> = annoncesInitiales.toMutableList()
    private val moderations: MutableList<ActionModeration> = mutableListOf()
    private var prochainId: Int = (annoncesInitiales.maxOfOrNull { it.id } ?: 0) + 1

    /** Toutes les annonces, quel que soit leur état. */
    fun toutesLesAnnonces(): List<Annonce> = annonces.toList()

    /** Trace des suppressions effectuées par un administrateur, de la plus récente à la plus ancienne. */
    fun actionsModeration(): List<ActionModeration> =
        moderations.sortedByDescending { it.date }

    /**
     * Publie une annonce (UC1) : l'auteur est rattaché automatiquement,
     * la date est celle du jour et l'état initial est [Disponible].
     *
     * @throws ErreurMetierException si les champs sont invalides.
     */
    fun publierAnnonce(
        titre: String,
        description: String,
        prix: Double,
        categorie: Categorie,
        auteur: CompteUtilisateur,
        photoUrl: String? = null
    ): Annonce {
        ReglesAnnonce.validerAnnonce(titre, prix, description)?.let { throw it.exception() }

        val nouvelleAnnonce = Annonce(
            id = prochainId,
            titre = titre.trim(),
            description = description.trim(),
            prix = prix,
            categorie = categorie,
            photoUrl = photoUrl,
            auteur = auteur,
            etat = Disponible,
            dateCreation = aujourdHui()
        )
        annonces.add(nouvelleAnnonce)
        prochainId++
        return nouvelleAnnonce
    }

    /**
     * Republie une annonce vendue (UC4) sous une nouvelle identité : `copy()`
     * reprend le contenu, l'état repart à [Disponible] et la date de création
     * est celle du jour.
     *
     * @return la nouvelle annonce, ou `null` si l'annonce d'origine n'existe pas.
     */
    fun republierAnnonce(
        id: Int,
        auteur: CompteUtilisateur
    ): Annonce? {
        val originale = annonces.firstOrNull { it.id == id } ?: return null
        val copie = originale.copy(
            id = prochainId,
            auteur = auteur,
            etat = Disponible,
            dateCreation = aujourdHui()
        )
        annonces.add(copie)
        prochainId++
        return copie
    }

    /**
     * Recherche du catalogue public (UC2 + UC3) : uniquement des annonces
     * disponibles, filtrées par mot-clé, catégorie et fourchette de prix.
     */
    fun rechercherAnnonces(
        motCle: String? = null,
        categorie: Categorie? = null,
        prixMin: Double? = null,
        prixMax: Double? = null
    ): List<Annonce> = annonces.filtrerPourRecherche(motCle, categorie, prixMin, prixMax)

    /** Met à jour les champs fournis (UC4). */
    fun modifierAnnonce(
        id: Int,
        titre: String? = null,
        description: String? = null,
        prix: Double? = null,
        categorie: Categorie? = null,
        photoUrl: String? = null
    ): Annonce? {
        val index = annonces.indexOfFirst { it.id == id }
        if (index == -1) return null
        val existante = annonces[index]
        val miseAJour = existante.copy(
            titre = titre?.trim() ?: existante.titre,
            description = description?.trim() ?: existante.description,
            prix = prix ?: existante.prix,
            categorie = categorie ?: existante.categorie,
            photoUrl = photoUrl ?: existante.photoUrl
        )
        annonces[index] = miseAJour
        return miseAJour
    }

    /** Applique une transition d'état déjà validée (UC5). */
    fun changerEtat(id: Int, nouvelEtat: EtatAnnonce): Annonce? {
        val index = annonces.indexOfFirst { it.id == id }
        if (index == -1) return null
        return annonces[index].copy(etat = nouvelEtat).also { annonces[index] = it }
    }

    /** Retire définitivement une annonce (UC6, suppression par son propriétaire). */
    fun retirerAnnonce(id: Int): Boolean = annonces.removeAll { it.id == id }

    /**
     * Retire une annonce sur décision d'un administrateur (UC6, §3.2) et
     * journalise le motif [motif] dans [actionsModeration].
     *
     * @throws ErreurMetierException si le motif est absent.
     */
    fun retirerAnnonceParAdmin(
        id: Int,
        moderateur: CompteUtilisateur,
        motif: String
    ): Boolean {
        ReglesAnnonce.validerModeration(motif)?.let { throw it.exception() }
        val cible = annonces.firstOrNull { it.id == id } ?: return false
        moderations.add(
            ActionModeration(
                annonceId = cible.id,
                annonceTitre = cible.titre,
                moderateur = moderateur,
                motif = motif.trim(),
                date = aujourdHui()
            )
        )
        return annonces.removeAll { it.id == id }
    }
}
