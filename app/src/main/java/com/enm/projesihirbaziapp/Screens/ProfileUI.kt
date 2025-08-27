package com.enm.projesihirbaziapp.Screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.enm.projesihirbaziapp.Business.UserManager
import com.enm.projesihirbaziapp.DataAccess.UserDataAccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person

import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Description

import androidx.compose.material3.Icon


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileUI(
    onLoggedOut: () -> Unit
) {
    val context = LocalContext.current
    val manager = remember { UserManager(context) }
    val uda = remember { UserDataAccess(context) }
    val scope = rememberCoroutineScope()

    // State’ler
    var profileBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // Galeriden resim seçici
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                profileBitmap = loadBitmapFromUri(context, uri)
            }
        }
    }

    // Ekrana gelirken: token yenile + kullanıcı verilerini çek
    LaunchedEffect(Unit) {
        // token refresh (sessiz)
        runCatching { manager.refreshToken() }

        uda.getAccessToken()?.let { token ->
            isLoading = true
            val user = runCatching { manager.getUserData(token) }.getOrNull()
            if (user != null) {
                name = user.getName()
                surname = user.getSurname()
                email = user.getEmail()
                phone = user.getPhone()
                val imageUrl = user.getImageFile()
                if (!imageUrl.isNullOrBlank()) {
                    profileBitmap = loadBitmapFromUrl(imageUrl)
                }
                isLoading = false
            } else {
                isLoading = false
                alertMessage = "Kullanıcı verileri alınamadı."
                showAlert = true
            }
        } ?: run {
            isLoading = false
            alertMessage = "Oturum bulunamadı."
            showAlert = true
        }
    }

    // UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil") }
            )
        }
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(inner)) {

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(12.dp))

                // Profil resmi
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (profileBitmap != null) {
                        Image(
                            bitmap = profileBitmap!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Placeholder ikon
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Bilgiler kartı
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        LabeledField(label = "Ad", value = name, onValueChange = { name = it })
                        LabeledField(label = "Soyad", value = surname, onValueChange = { surname = it })
                        LabeledField(label = "E-posta", value = email, onValueChange = {}, enabled = false)
                        LabeledField(label = "Telefon", value = phone, onValueChange = { phone = it })

                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            singleLine = true,
                            label = { Text("Yeni Şifre") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Güncelle
                Button(
                    onClick = {
                        scope.launch {
                            val token = uda.getAccessToken()
                            if (token.isNullOrBlank()) {
                                alertMessage = "Oturum bulunamadı."
                                showAlert = true
                                return@launch
                            }
                            isLoading = true
                            // Swift kodunda imageFile "" gönderilmişti; burada da aynı
                            val updated = manager.updateUserData(
                                token = token,
                                name = name,
                                surname = surname,
                                imageFile = "",
                                password = newPassword
                            )
                            isLoading = false
                            if (updated != null) {
                                name = updated.getName()
                                surname = updated.getSurname()
                                email = updated.getEmail()
                                phone = updated.getPhone()
                                alertMessage = "Kullanıcı bilgileri başarıyla güncellendi."
                            } else {
                                alertMessage = "Güncelleme başarısız."
                            }
                            showAlert = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Güncelle") }

                Spacer(Modifier.height(8.dp))

                // Çıkış Yap
                Button(
                    onClick = {
                        // Token’ları temizle
                        context.getSharedPreferences("proje_sihirbazi_prefs", Context.MODE_PRIVATE)
                            .edit()
                            .remove("accessToken")
                            .remove("refreshToken")
                            .putBoolean("isLoggedIn", false)
                            .apply()
                        onLoggedOut()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Çıkış Yap") }

                Spacer(Modifier.height(24.dp))
            }

            // Loading overlay
            if (isLoading) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        tonalElevation = 6.dp,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(Modifier.size(28.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Yükleniyor...")
                        }
                    }
                }
            }
        }
    }

    // Alert
    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            confirmButton = { TextButton(onClick = { showAlert = false }) { Text("Tamam") } },
            title = { Text("Uyarı") },
            text = { Text(alertMessage) }
        )
    }
}

/* ---------- Küçük yardımcılar ---------- */

@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true
) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private suspend fun loadBitmapFromUrl(urlStr: String): Bitmap? = withContext(Dispatchers.IO) {
    return@withContext try {
        val url = URL(urlStr)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 15000
            readTimeout = 15000
        }
        conn.inputStream.use { BitmapFactory.decodeStream(it) }
    } catch (_: Exception) {
        null
    }
}

private suspend fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
    return@withContext try {
        val input: InputStream? = context.contentResolver.openInputStream(uri)
        input.use { stream -> BitmapFactory.decodeStream(stream) }
    } catch (_: Exception) {
        null
    }
}
