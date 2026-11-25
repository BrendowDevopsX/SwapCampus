package com.swapcampus.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.swapcampus.domain.Categorie
import com.swapcampus.ui.theme.style
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Sélection de la photo d'une annonce : galerie du téléphone (Photo Picker
 * système, sans permission à déclarer) ou lien vers une image distante.
 *
 * La photo choisie est conservée sous forme d'URI (`content://…`) dans
 * `Annonce.photoUrl`, exactement comme un lien http : le domaine ne connaît
 * donc qu'une `String`, et l'affichage retombe sur l'illustration de la
 * catégorie si l'image n'est plus accessible.
 */
@Composable
fun ChampPhoto(
    photoUrl: String,
    onPhotoUrlChange: (String) -> Unit,
    categorie: Categorie?,
    modifier: Modifier = Modifier
) {
    val style = (categorie ?: Categorie.AUTRE).style()

    val selecteur = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) onPhotoUrlChange(uri.toString())
    }

    Column(modifier = modifier, verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(style.couleurs)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = style.glyphe,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                if (photoUrl.isNotBlank()) {
                    PhotoAnnonce(
                        source = photoUrl,
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }

            Column(modifier = Modifier.padding(start = 14.dp)) {
                OutlinedButton(
                    onClick = {
                        selecteur.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.testTag("bouton_galerie")
                ) {
                    Text("Choisir une photo")
                }
                if (photoUrl.isNotBlank()) {
                    TextButton(
                        onClick = { onPhotoUrlChange("") },
                        modifier = Modifier.testTag("bouton_retirer_photo")
                    ) {
                        Text("Retirer")
                    }
                }
            }
        }

        OutlinedTextField(
            value = photoUrl,
            onValueChange = onPhotoUrlChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("champ_photo"),
            label = { Text("Photo (facultatif)") },
            placeholder = { Text("Lien d'une image ou photo choisie") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done
            ),
            supportingText = { Text("Choisissez une photo de votre galerie ou collez un lien.") },
            shape = MaterialTheme.shapes.medium
        )
    }
}

/**
 * Charge une image à partir d'une URI locale (`content://`, `file://`) ou
 * d'une URL http(s), sur un thread d'arrière-plan.
 *
 * Silencieux en cas d'échec : l'illustration de la catégorie reste affichée.
 */
@Composable
fun PhotoAnnonce(source: String, modifier: Modifier = Modifier) {
    val contexte = LocalContext.current
    var image by remember(source) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(source) {
        image = withContext(Dispatchers.IO) { chargerImage(contexte, source) }
    }

    image?.let {
        Image(
            bitmap = it,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}

private fun chargerImage(contexte: Context, source: String): ImageBitmap? = runCatching {
    val uri = Uri.parse(source)
    when (uri.scheme) {
        "content", "file", "android.resource" -> chargerLocale(contexte, uri)
        else -> chargerDistante(source)
    }
}.getOrNull()

private fun chargerLocale(contexte: Context, uri: Uri): ImageBitmap? {
    val resolveur = contexte.contentResolver

    // Première passe : dimensions seulement, pour ne pas décoder une photo de
    // 12 Mpx dans une vignette de 84 dp.
    val bornes = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolveur.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bornes) }

    val options = BitmapFactory.Options().apply {
        inSampleSize = facteurEchantillonnage(bornes.outWidth, bornes.outHeight, MAX_COTE)
    }
    return resolveur.openInputStream(uri)
        ?.use { BitmapFactory.decodeStream(it, null, options) }
        ?.asImageBitmap()
}

private fun chargerDistante(url: String): ImageBitmap? {
    val connexion = URL(url).openConnection() as HttpURLConnection
    connexion.connectTimeout = 4_000
    connexion.readTimeout = 6_000
    return connexion.inputStream.use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
}

private fun facteurEchantillonnage(largeur: Int, hauteur: Int, coteMax: Int): Int {
    if (largeur <= 0 || hauteur <= 0) return 1
    var facteur = 1
    while (largeur / (facteur * 2) >= coteMax && hauteur / (facteur * 2) >= coteMax) {
        facteur *= 2
    }
    return facteur
}

private const val MAX_COTE = 1_024
