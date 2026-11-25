package com.swapcampus.domain

/**
 * Extensions sur les annonces : règles de visibilité, de recherche et
 * d'affichage. Aucune logique de ce fichier ne dépend d'Android, ce qui les
 * rend directement testables en tests unitaires (JVM).
 */

/** Annonces visibles dans le catalogue : uniquement celles encore disponibles. */
fun List<Annonce>.disponibles(): List<Annonce> =
    filter { it.etat is Disponible }

/**
 * Recherche du catalogue (UC3).
 *
 * Règles métier :
 * 1. seules les annonces **Disponible** sont visibles : une annonce réservée ou
 *    vendue disparaît du catalogue ;
 * 2. le filtre de catégorie est exact ;
 * 3. le mot-clé est comparé, sans tenir compte de la casse, au titre et à la
 *    description.
 *
 * @param motCle terme recherché, `null` ou vide pour ne pas filtrer.
 * @param categorie catégorie retenue, `null` pour toutes les catégories.
 */
fun List<Annonce>.filtrerPourRecherche(
    motCle: String?,
    categorie: Categorie? = null
): List<Annonce> {
    val terme = motCle?.trim()?.lowercase().orEmpty()

    return asSequence()
        .filter { it.etat is Disponible }
        .filter { categorie == null || it.categorie == categorie }
        .filter { annonce ->
            terme.isEmpty() ||
                annonce.titre.lowercase().contains(terme) ||
                annonce.description.lowercase().contains(terme)
        }
        .sortedWith(
            compareByDescending<Annonce> { it.dateCreation }.thenByDescending { it.id }
        )
        .toList()
}

/** URL de photo réellement affichable, `null` si l'annonce n'en a pas. */
fun Annonce.urlAfficheeOuDefaut(): String? = photoUrl?.trim()?.takeIf { it.isNotEmpty() }

/** Texte de remplacement affiché quand l'annonce n'a pas de photo. */
fun Annonce.decrirePhoto(): String =
    if (urlAfficheeOuDefaut() == null) "Pas de photo" else "Photo de l'annonce"

/** Résumé de la description, tronqué proprement pour les cartes. */
fun Annonce.resume(longueurMax: Int = 110): String {
    val texte = description.trim().replace('\n', ' ')
    return if (texte.length <= longueurMax) texte else texte.take(longueurMax).trimEnd() + "…"
}
