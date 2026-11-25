# SwapCampus

Project Dev. App. Mobile — application Android (Kotlin + Jetpack Compose) pour publier et
consulter des annonces entre étudiants.

## Environnement requis

| Outil | Version |
|---|---|
| Android Studio | 2026.1.4 ou plus récent (JDK 17 ou plus, le JBR intégré convient) |
| Android SDK Platform | 37 (Android 17) |
| Android SDK Build-Tools | 36.0.0 |
| Gradle | 9.6 — téléchargé automatiquement par le wrapper |
| Android Gradle Plugin | 9.4.1 |

`local.properties`, `.idea/` et les dossiers `build/` / `.gradle/` ne sont **pas** versionnés :
dans ce projet, ils contiennent des chemins propres à chaque machine (dont le chemin du SDK et
celui de l'émulateur). Android Studio les régénère au premier import et vous demandera votre
chemin de SDK.

## Lancer le projet

1. `File > Open`, sélectionner le dossier du projet, puis attendre la fin du **Gradle Sync**.
   Si Android Studio propose d'installer des packages SDK manquants (Platform 37), accepter.
2. Choisir un appareil ou un émulateur (AVD), puis cliquer sur **Run ▶**.
3. Le premier lancement télécharge Gradle et les dépendances : comptez plusieurs minutes.

### Astuce émulateur

Si l'émulateur affiche l'écran d'accueil figé et que l'application ne s'ouvre jamais, c'est un
démarrage par snapshot rapide corrompu, pas un bug du projet. Correctif :

`Tools > Device Manager` → `⋮` sur l'AVD → **Cold Boot Now**.
Pour éviter que cela se reproduise : `Edit` l'AVD → *Boot option* = **Cold boot**.

## Fonctionnalités (Lab 1 / TP1)

| Cas d'utilisation | Où |
|---|---|
| UC1 — publier une annonce | FAB « + » ou bouton de l'en-tête « Mes annonces » → formulaire (titre, description, prix, catégorie, photo choisie dans la galerie ou lien) |
| UC2 — parcourir | Onglet « Annonces » : cartes avec illustration de catégorie, statut, prix, vendeur ; seules les annonces **disponibles** sont listées |
| UC3 — rechercher / filtrer | Champ de recherche (titre et description) + puces de catégorie ; état vide explicite avec bouton « Réinitialiser » |
| UC4 — modifier | Fiche de l'annonce → « Modifier » (propriétaire uniquement ; une annonce vendue doit être republiée) |
| UC5 — cycle d'état | Fiche → « Réserver à un acheteur » (nom obligatoire) → « Marquer comme vendue » ; « Annuler la réservation » et « Remettre en vente » |
| UC6 — supprimer | Fiche → icône corbeille, avec confirmation (propriétaire uniquement) |
| UC7 — statistiques vendeur | Onglet « Mes annonces » : publiées / en ligne / réservées / vendues, plus les mêmes chiffres sur le profil |
| Connexion | Onglet « Profil » ou toute action protégée ; comptes de démonstration cliquables sur l'écran de connexion |

Comptes de démonstration (mot de passe entre parenthèses) :

- `alice@etu.fr` (alice123) — 4 annonces
- `bob@etu.fr` (bob123) — 4 annonces
- `admin@swapcampus.fr` (admin123) — administrateur

## Architecture

- `shared/ui` — écrans Compose, composants, ViewModels (MVVM)
- `shared/commonMain/domain` — entités, états, catalogue en mémoire, règles métier
- `app/src/main/java/com/swapcampus/data/` — jeu de données de démonstration
- `shared/commonMain/repository` — dépôts (annonces, authentification), exposés en `Flow`
- `app/src/main/java/com/swapcampus/navigation/` — routes et NavHost
- `shared/commonMain/di` — injection de dépendances manuelle
- `app/src/main/java/com/isep/shared/domain/` — ancien package domaine, encore présent (doublon à fusionner)
- `app/src/test/` — tests unitaires (domaine, dépôt d'annonces, authentification)
- `app/src/androidTest/` — test d'interface du parcours de publication

## Photos

Le champ photo de la publication et de la modification propose deux entrées :

- **« Choisir une photo »** — ouvre le sélecteur de photos du système (Photo Picker,
  `ActivityResultContracts.PickVisualMedia`), donc l'accès à la galerie sans aucune permission à
  déclarer dans le manifeste ;
- **un lien** `https://…`, pour utiliser une image distante.

La photo retenue est stockée dans `Annonce.photoUrl` sous forme d'URI `content://…` ou d'URL : le
domaine ne manipule donc qu'une `String`. Le chargement est fait sans bibliothèque externe
(`ContentResolver` pour les URI locales, `HttpURLConnection` pour les URL), avec un sous-
échantillonnage à 1 024 px pour ne pas décoder une photo de 12 Mpx dans une vignette de 84 dp.
L'affichage retombe toujours sur l'illustration de la catégorie si l'image n'est plus lisible.

> Limite actuelle (TP1) : l'autorisation d'accès accordée par le sélecteur dure le temps du
> processus, et rien n'est persisté. Au TP2 (stockage local), il faudra copier le fichier dans le
> stockage de l'application pour que la photo survive au redémarrage.

## Tests

```bash
# Tests unitaires JVM (domaine + dépôts)
./gradlew :app:testDebugUnitTest

# Tests d'interface sur un émulateur ou un appareil connecté
./gradlew :app:connectedDebugAndroidTest
```

Les règles métier sont vérifiées côté domaine : validation du titre et du prix, transitions
`Disponible → Réservée → Vendue` (et retour), filtre des annonces non disponibles dans le
catalogue, statistiques du vendeur.

## État des données

Les annonces sont stockées en mémoire (`CatalogueAnnonces`) et amorcées avec huit annonces de
démonstration (`EchantillonDonnees`) couvrant les trois états et plusieurs catégories, pour que la
démonstration ne démarre pas sur un écran vide. Rien n'est persisté : tout repart à zéro au
prochain lancement (cahier des charges §8.3, le stockage local arrive au TP2).
