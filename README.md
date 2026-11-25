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

## Structure du code

- `app/src/main/java/com/swapcampus/ui/` — écrans et composants Compose, ViewModels
- `app/src/main/java/com/swapcampus/domain/` — entités et catalogue en mémoire
- `app/src/main/java/com/swapcampus/repository/` — dépôts (annonces, authentification)
- `app/src/main/java/com/swapcampus/navigation/` — routes et NavHost
- `app/src/main/java/com/swapcampus/di/AppContainer.kt` — injection de dépendances manuelle
- `app/src/main/java/com/isep/shared/domain/` — ancien package domaine, encore présent (doublon à fusionner)
- `app/src/test/` — tests unitaires du dépôt d'annonces

## État des données

Les annonces sont stockées en mémoire (`CatalogueAnnonces`) et la liste est vide au démarrage :
l'écran Liste affiche donc « No listings match your search » tant qu'aucune annonce n'a été
publiée. Il n'y a ni backend ni base de données à ce stade.
