package com.enm.projesihirbaziapp.Components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import java.io.InputStream

/**
 * SwiftUI'deki ImagePicker eşleniği.
 * Görünür olduğunda galeriyi açar; seçilen resmi Bitmap olarak döner.
 *
 * Not: iOS'taki allowsEditing benzeri yerleşik kırpma yoktur.
 * Kırpma istiyorsanız uCrop gibi bir kütüphane ekleyin.
 */
@Composable
fun ImagePicker(
    onPicked: (Bitmap?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val bmp = uri?.let { loadBitmapFromUri(context, it) }
        onPicked(bmp)
        onDismiss()
    }

    // Composable görünür olur olmaz galeriyi aç
    LaunchedEffect(Unit) {
        launcher.launch("image/*")
    }
}

private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        val input: InputStream? = context.contentResolver.openInputStream(uri)
        input.use { stream -> BitmapFactory.decodeStream(stream) }
    } catch (_: Exception) {
        null
    }
}
