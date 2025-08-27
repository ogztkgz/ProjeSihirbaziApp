@file:OptIn(ExperimentalMaterial3Api::class)

package com.enm.projesihirbaziapp.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.enm.projesihirbaziapp.Business.AcademicianManager
import com.enm.projesihirbaziapp.Business.FiltreManager
import com.enm.projesihirbaziapp.Models.Academician
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun AcademicsUI() {
    val scope = rememberCoroutineScope()
    val academicianManager = remember { AcademicianManager() }
    val filtreManager = remember { FiltreManager() }

    var currentPage by remember { mutableStateOf(1) }
    var selectedName by remember { mutableStateOf("") }
    var selectedProvince by remember { mutableStateOf("") }
    var selectedUniversity by remember { mutableStateOf("") }
    var selectedKeywords by remember { mutableStateOf("") }

    var academicsArr by remember { mutableStateOf<List<Academician>>(emptyList()) }
    var iller by remember { mutableStateOf<List<String>>(emptyList()) }
    var universiteler by remember { mutableStateOf<List<String>>(emptyList()) }
    var anahtarKelimeler by remember { mutableStateOf<List<String>>(emptyList()) }

    var isLoading by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var totalPages by remember { mutableStateOf(1) }

    // İlk yükleme
    LaunchedEffect(Unit) {
        getAcademics(scope, academicianManager, currentPage, selectedName, selectedProvince, selectedUniversity, selectedKeywords,
            onLoading = { isLoading = it },
            onResult = { items ->
                academicsArr = items
                totalPages = if (items.isNotEmpty()) maxOf(items.size / 10, 1) else 1
            }
        )
        getIl(scope, filtreManager) { iller = it }
        getUni(scope, filtreManager) { universiteler = it }
        getKeyword(scope, filtreManager) { anahtarKelimeler = it }
    }

    // İsim değiştikçe debounce
    LaunchedEffect(selectedName) {
        currentPage = 1
        delay(300)
        getAcademics(scope, academicianManager, currentPage, selectedName, selectedProvince, selectedUniversity, selectedKeywords,
            onLoading = { isLoading = it },
            onResult = { items ->
                academicsArr = items
                totalPages = if (items.isNotEmpty()) maxOf(items.size / 10, 1) else 1
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Akademisyenler") },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filtreler")
                    }
                }
            )
        }
    ) { inner ->
        Column(Modifier.padding(inner).fillMaxSize()) {

            // Arama
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth().heightIn(min = 48.dp)
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = selectedName,
                    onValueChange = { selectedName = it },
                    singleLine = true,
                    placeholder = { Text("Ad ara") },
                    modifier = Modifier.weight(1f)
                )
                if (selectedName.isNotEmpty()) {
                    IconButton(onClick = {
                        selectedName = ""
                        currentPage = 1
                        scope.launch {
                            getAcademics(scope, academicianManager, currentPage, selectedName, selectedProvince, selectedUniversity, selectedKeywords,
                                onLoading = { isLoading = it },
                                onResult = { items ->
                                    academicsArr = items
                                    totalPages = if (items.isNotEmpty()) maxOf(items.size / 10, 1) else 1
                                }
                            )
                        }
                    }) { Icon(Icons.Filled.Close, contentDescription = "Temizle") }
                }
            }

            // Liste / Yükleniyor
            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (isLoading) {
                    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text("Yükleniyor…")
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = academicsArr,
                            key = { a -> a.name }      // <-- burada 'it' yok, id gerekmiyor
                        ) { academician ->
                            AcademicianRow(academician = academician) // Dış dosyadaki bileşen
                        }
                    }
                }
            }

            // Sayfalama
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                val canPrev = currentPage > 1
                val canNext = currentPage < totalPages

                FilledTonalIconButton(
                    onClick = {
                        if (canPrev) {
                            currentPage -= 1
                            scope.launch {
                                getAcademics(scope, academicianManager, currentPage, selectedName, selectedProvince, selectedUniversity, selectedKeywords,
                                    onLoading = { isLoading = it },
                                    onResult = { items ->
                                        academicsArr = items
                                        totalPages = if (items.isNotEmpty()) maxOf(items.size / 10, 1) else 1
                                    }
                                )
                            }
                        }
                    },
                    enabled = canPrev
                ) { Icon(Icons.Filled.ChevronLeft, contentDescription = "Önceki") }

                Spacer(Modifier.weight(1f))
                Text("Sayfa $currentPage / ${maxOf(totalPages, 1)}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))

                FilledTonalIconButton(
                    onClick = {
                        if (canNext) {
                            currentPage += 1
                            scope.launch {
                                getAcademics(scope, academicianManager, currentPage, selectedName, selectedProvince, selectedUniversity, selectedKeywords,
                                    onLoading = { isLoading = it },
                                    onResult = { items ->
                                        academicsArr = items
                                        totalPages = if (items.isNotEmpty()) maxOf(items.size / 10, 1) else 1
                                    }
                                )
                            }
                        }
                    },
                    enabled = canNext
                ) { Icon(Icons.Filled.ChevronRight, contentDescription = "Sonraki") }
            }
        }
    }

    // Filtre sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showFilterSheet = false
                scope.launch {
                    currentPage = 1
                    getAcademics(scope, academicianManager, currentPage, selectedName, selectedProvince, selectedUniversity, selectedKeywords,
                        onLoading = { isLoading = it },
                        onResult = { items ->
                            academicsArr = items
                            totalPages = if (items.isNotEmpty()) maxOf(items.size / 10, 1) else 1
                        }
                    )
                }
            }
        ) {
            // Dış dosyadaki FilterViewAcademician bileşenini KULLAN
            FilterViewAcademician(
                selectedUniversity = selectedUniversity,
                onUniversityChange = { selectedUniversity = it },
                selectedProvince = selectedProvince,
                onProvinceChange = { selectedProvince = it },
                selectedKeywords = selectedKeywords,
                onKeywordsChange = { selectedKeywords = it },
                iller = iller,
                universiteler = universiteler,
                anahtarKelimeler = anahtarKelimeler,
                onClose = {
                    showFilterSheet = false
                    scope.launch {
                        currentPage = 1
                        getAcademics(scope, academicianManager, currentPage, selectedName, selectedProvince, selectedUniversity, selectedKeywords,
                            onLoading = { isLoading = it },
                            onResult = { items ->
                                academicsArr = items
                                totalPages = if (items.isNotEmpty()) maxOf(items.size / 10, 1) else 1
                            }
                        )
                    }
                }
            )
        }
    }
}

/* ---------- Veri çekme yardımcıları ---------- */

private fun getAcademics(
    scope: CoroutineScope,
    manager: AcademicianManager,
    page: Int,
    selectedName: String,
    selectedProvince: String,
    selectedUniversity: String,
    selectedKeywords: String,
    onLoading: (Boolean) -> Unit,
    onResult: (List<Academician>) -> Unit
) {
    scope.launch {
        onLoading(true)
        val res = manager.getAcademics(page, selectedName, selectedProvince, selectedUniversity, selectedKeywords)
        onLoading(false)
        res.onSuccess { onResult(it) }
            .onFailure { onResult(emptyList()) }
    }
}

private fun getIl(scope: CoroutineScope, filtreManager: FiltreManager, onDone: (List<String>) -> Unit) {
    scope.launch { filtreManager.getIl().onSuccess(onDone).onFailure { onDone(emptyList()) } }
}
private fun getUni(scope: CoroutineScope, filtreManager: FiltreManager, onDone: (List<String>) -> Unit) {
    scope.launch { filtreManager.getUni().onSuccess(onDone).onFailure { onDone(emptyList()) } }
}
private fun getKeyword(scope: CoroutineScope, filtreManager: FiltreManager, onDone: (List<String>) -> Unit) {
    scope.launch { filtreManager.getKeyword().onSuccess(onDone).onFailure { onDone(emptyList()) } }
}
