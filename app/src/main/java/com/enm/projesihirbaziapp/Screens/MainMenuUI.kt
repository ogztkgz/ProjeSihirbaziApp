@file:OptIn(ExperimentalMaterial3Api::class)

package com.enm.projesihirbaziapp.Screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.enm.projesihirbaziapp.Business.DashboardManager   // <-- ÖNEMLİ
import kotlinx.coroutines.launch

@Composable
fun MainMenuUI(
    onOpenGrants: () -> Unit = {},
    onOpenAcademics: () -> Unit = {},
    onOpenTenders: () -> Unit = {},
    onOpenProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dashboardManager = remember { DashboardManager() }

    var grantCount by remember { mutableStateOf(0) }
    var academicianCount by remember { mutableStateOf(0) }
    var tenderCount by remember { mutableStateOf(0) }

    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }

    // Ekran açılınca dashboard verisini çek
    LaunchedEffect(Unit) {
        // Swift'teki onAppear: selectedChatId = 0
        context.getSharedPreferences("proje_sihirbazi_prefs", Context.MODE_PRIVATE)
            .edit().putInt("selectedChatId", 0).apply()

        val res = dashboardManager.fetchDashboardData()
        res.onSuccess { dash ->
            grantCount = dash.grantCount
            academicianCount = dash.academicianCount
            tenderCount = dash.tenderCount
        }.onFailure { e ->
            alertMessage = "Veriler alınamadı. Hata: ${e.message ?: "Bilinmeyen hata"}"
            showAlert = true
        }
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

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(title = "Hibe", value = grantCount, modifier = Modifier.weight(1f))
                StatCard(title = "Akademisyen", value = academicianCount, modifier = Modifier.weight(1f))
                StatCard(title = "İhale", value = tenderCount, modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Hızlı Erişim",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(Modifier.height(8.dp))

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
