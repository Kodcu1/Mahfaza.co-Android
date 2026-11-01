package com.example.util

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object AvatarUtils {

    fun saveAvatarToInternalStorage(context: Context, sourceUri: Uri, userId: String): String? {
        return try {
            val avatarsDir = File(context.filesDir, "avatars")
            if (!avatarsDir.exists()) {
                avatarsDir.mkdirs()
            }
            val destinationFile = File(avatarsDir, "${userId}_avatar.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream: InputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            destinationFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveBitmapToInternalStorage(context: Context, bitmap: android.graphics.Bitmap, fileNamePrefix: String): String? {
        return try {
            val proofsDir = File(context.filesDir, "proofs")
            if (!proofsDir.exists()) proofsDir.mkdirs()
            val destinationFile = File(proofsDir, "${fileNamePrefix}_${System.currentTimeMillis()}.jpg")
            FileOutputStream(destinationFile).use { outputStream ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, outputStream)
            }
            destinationFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@Composable
fun StudentAvatar(
    name: String,
    avatarUri: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    textSize: TextUnit = 16.sp
) {
    val initials = name.trim().split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifEmpty { "M" }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (!avatarUri.isNullOrBlank()) {
            val file = File(avatarUri)
            val model = if (file.exists()) file else avatarUri
            AsyncImage(
                model = model,
                contentDescription = "$name Profil Fotoğrafı",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
            )
        } else {
            Text(
                text = initials,
                fontSize = textSize,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
