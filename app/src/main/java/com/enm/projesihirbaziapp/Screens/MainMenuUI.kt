package com.enm.projesihirbaziapp.Screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuUI(

    onOpenGrants: () -> Unit = {},
    onOpenAcademics: () -> Unit = {},
    onOpenTenders: () -> Unit = {},
    onOpenProfile: () -> Unit = {}


) {
    val context = LocalContext.current

    // Basit state – gerçek veriyi sonra API'den doldururuz
    var grantCount by remember { mutableStateOf(12) }
    var academicianCount by remember { mutableStateOf(34) }
    var tenderCount by remember { mutableStateOf(7) }
    var showProfileSheet by remember { mutableStateOf(false) }
    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }

    // Swift'teki onAppear eşleniği
    LaunchedEffect(Unit) {
        context.getSharedPreferences("proje_sihirbazi_prefs", Context.MODE_PRIVATE)
            .edit().putInt("selectedChatId", 0).apply()
        // TODO: Burada dashboard API çağrısı yapıp sayıları güncelle.
        // Hata durumunda:
        // alertMessage = "Veriler alınamadı."
        // showAlert = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ana Menü") },
                actions = {
                    IconButton(onClick = { onOpenProfile() }) {
                        Icon(Icons.Filled.Person, contentDescription = "Profil")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Ana Menü",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(Modifier.height(16.dp))

            // Küçük istatistik kartları
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(title = "Hibe", value = grantCount, modifier = Modifier.weight(1f))
                StatCard(title = "Akademisyen", value = academicianCount, modifier = Modifier.weight(1f))
                StatCard(title = "İhale", value = tenderCount, modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))

            Text("Hızlı Erişim", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            Spacer(Modifier.height(8.dp))

            // Hızlı erişim kartı
            Card(Modifier.fillMaxWidth()) {
                Column {
                    ListItem(
                        headlineContent = { Text("Hibe Projeleri") },
                        supportingContent = { Text("$grantCount kayıt") },
                        modifier = Modifier.clickable { onOpenGrants() }
                    )
                    Divider()
                    ListItem(
                        headlineContent = { Text("Akademisyenler") },
                        supportingContent = { Text("$academicianCount kişi") },
                        modifier = Modifier.clickable { onOpenAcademics() }
                    )
                    Divider()
                    ListItem(
                        headlineContent = { Text("İhale Projeleri") },
                        supportingContent = { Text("$tenderCount kayıt") },
                        modifier = Modifier.clickable { onOpenTenders() }
                    )
                }
            }
        }
    }

    // Profil Sheet
    if (showProfileSheet) {
        ModalBottomSheet(onDismissRequest = { showProfileSheet = false }) {
            Column(Modifier.padding(16.dp).fillMaxWidth()) {
                Text("Profil", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                Text("Profil detayları burada olacak.")
                Spacer(Modifier.height(16.dp))
                Button(onClick = { showProfileSheet = false }, modifier = Modifier.fillMaxWidth()) {
                    Text("Kapat")
                }
            }
        }
    }

    // Uyarı
    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            confirmButton = { TextButton(onClick = { showAlert = false }) { Text("Tamam") } },
            title = { Text("Uyarı") },
            text = { Text(alertMessage) }
        )
    }
}

@Composable
private fun StatCard(title: String, value: Int, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = MaterialTheme.shapes.medium) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Text("$value", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}
