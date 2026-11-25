package com.swapcampus.ui.screens.login

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swapcampus.domain.CompteUtilisateur
import com.swapcampus.domain.libelle
import com.swapcampus.ui.components.SectionTitle
import com.swapcampus.ui.components.initiales
import androidx.compose.ui.unit.sp

/**
 * Écran de connexion. La publication et la modification d'annonces exigent un
 * compte (§3.1) ; le catalogue reste consultable sans être connecté.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    viewModel: LoginViewModel = viewModel()
) {
    val email by viewModel.email.collectAsStateWithLifecycle()
    val motDePasse by viewModel.motDePasse.collectAsStateWithLifecycle()
    val erreur by viewModel.error.collectAsStateWithLifecycle()
    val chargement by viewModel.chargement.collectAsStateWithLifecycle()

    var motDePasseVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("login_retour")
            ) {
                Text(text = "←", style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.height(8.dp))
        }

        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SC",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Bon retour 👋",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Connectez-vous pour publier une annonce, réserver un objet et suivre vos ventes.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(28.dp))

        OutlinedTextField(
            value = email,
            onValueChange = viewModel::onEmailChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("champ_email"),
            label = { Text("Email ISEP") },
            placeholder = { Text("prenom.nom@etu.fr") },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = motDePasse,
            onValueChange = viewModel::onMotDePasseChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("champ_mot_de_passe"),
            label = { Text("Mot de passe") },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            leadingIcon = {
                Text(text = "🔒", fontSize = 15.sp)
            },
            trailingIcon = {
                TextButton(onClick = { motDePasseVisible = !motDePasseVisible }) {
                    Text(
                        text = if (motDePasseVisible) "Masquer" else "Afficher",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            },
            visualTransformation = if (motDePasseVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { viewModel.login(onLoginSuccess) }
            )
        )

        if (erreur != null) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = erreur.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { viewModel.login(onLoginSuccess) },
            enabled = !chargement,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("bouton_connexion"),
            shape = MaterialTheme.shapes.medium
        ) {
            if (chargement) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Se connecter", style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(Modifier.height(36.dp))

        SectionTitle(
            titre = "Comptes de démonstration",
            secondaire = "toucher pour remplir"
        )

        Spacer(Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            viewModel.comptesDemo.forEach { compte ->
                CarteCompteDemo(
                    compte = compte,
                    onClick = { viewModel.remplirAvec(compte) }
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

/** Ligne cliquable d'un compte de démonstration (nom, email, rôle). */
@Composable
private fun CarteCompteDemo(
    compte: CompteUtilisateur,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("compte_demo_${compte.id}")
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initiales(compte.nom),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = compte.nom,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = compte.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = compte.role.libelle(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
