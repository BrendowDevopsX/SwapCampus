package com.swapcampus.domain

/**
 * Catalogue en mémoire : source de vérité des annonces pour le TP1
 * (pas de base de données, voir cahier des charges §8.3).
 *
 * La classe ne connaît ni Compose ni Android : elle peut être testée en
 * JVM pur, puis réutilisée telle quelle dans le module partagé KMP.
 *
 * @param annoncesInitiales annonces de départ (jeu de démonstration), vide par défaut.
 */
class CatalogueAnnonces(
    annoncesInitiales: List<Annonce> = emptyList()
) {

    private val annonces: MutableList<Annonce> = annoncesInitiales.toMutableList()
    private var prochainId: Int = (annoncesInitiales.maxOfOrNull { it.id } ?: 0) + 1

    /** Toutes les annonces, quel que soit leur état. */
    fun toutesLesAnnonces(): List<Annonce> = annonces.toList()

    /**
     * Publie une annonce (UC1) : l'auteur est rattaché automatiquement,
     * la date est celle du jour et l'état initial est [Disponible].
     */
    fun publierAnnonce(
        titre: String,
        description: String,
        prix: Double,
        categorie: Categorie,
        auteur: CompteUtilisateur,
        photoUrl: String? = null
    ): Annonce {
        val nouvelleAnnonce = Annonce(
            id = prochainId,
            titre = titre,
            description = description,
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
     * Recherche du catalogue public (UC2 + UC3) : uniquement des annonces
     * disponibles, filtrées par mot-clé et/ou catégorie.
     */
    fun rechercherAnnonces(
        motCle: String? = null,
        categorie: Categorie? = null
    ): List<Annonce> = annonces.filtrerPourRecherche(motCle, categorie)

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
            titre = titre ?: existante.titre,
            description = description ?: existante.description,
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

    /** Retire définitivement une annonce (UC6). */
    fun retirerAnnonce(id: Int): Boolean = annonces.removeAll { it.id == id }
}
