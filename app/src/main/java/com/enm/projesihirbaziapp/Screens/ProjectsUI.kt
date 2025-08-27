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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.enm.projesihirbaziapp.Business.FiltreManager
import com.enm.projesihirbaziapp.Business.ProjectManager
import com.enm.projesihirbaziapp.DataAccess.UserDataAccess
import com.enm.projesihirbaziapp.Models.Projects
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProjectsUI(
    projectsType: String,
    onOpenAIWizard: (Int) -> Unit = {} // ProjectRow içindeki "Yapay Zeka ile Konuş" için nav callback
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Manager’lar (suspend + Result mimarisiyle)
    val filtreManager = remember { FiltreManager() }
    val projectManager = remember { ProjectManager() }
    val userDataAccess = remember { UserDataAccess(context) } // token refresh için

    // --- State'ler ---
    var projectsArr by remember { mutableStateOf<List<Projects>>(emptyList()) }
    var kurumlar by remember { mutableStateOf<List<String>>(emptyList()) }
    var sektorler by remember { mutableStateOf<List<String>>(emptyList()) }

    // Görünen seçenekler
    val basvuruDurumlari = remember { listOf("Açık", "Yakında Açılacaklar", "Sürekli Açık") }
    val siralamalar = remember { listOf("Tarihe göre(Artan)", "Tarihe göre(Azalan)", "Ada göre(A-Z)", "Ada göre(Z-A)") }
    // API eşlemesi (Swift'tekiyle aynı sıra!)
    val basvuruDurumlariAPI = remember { listOf("AÇIK", "YAKINDA_AÇILACAK", "SÜREKLİ_AÇIK") }
    val siralamalarAPI = remember { listOf("date_desc", "date_asc", "name_asc", "name_desc") }

    var currentPage by remember { mutableStateOf(1) }
    var selectedAd by remember { mutableStateOf("") }
    var selectedSiralama by remember { mutableStateOf("") }
    var selectedKurum by remember { mutableStateOf("") }
    var selectedSektor by remember { mutableStateOf("") }
    var selectedDurum by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var totalPages by remember { mutableStateOf(1) }
    var showFilterSheet by remember { mutableStateOf(false) }

    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }

    // İlk yüklemeler
    LaunchedEffect(Unit) {
        // Token yenile (isteğe bağlı)
        // userDataAccess.refreshToken { /* success -> loglayabilirsin */ }

        // Filtre listeleri
        filtreManager.getSektorler()
            .onSuccess { sektorler = it }
            .onFailure { sektorler = emptyList() }

        filtreManager.getKurumlar(projectsType)
            .onSuccess { kurumlar = it }
            .onFailure { kurumlar = emptyList() }

        // Projeleri çek
        loadProjects(
            manager = projectManager,
            type = projectsType,
            page = currentPage,
            sektor = selectedSektor,
            search = selectedAd,
            durumApi = mapDurum(selectedDurum, basvuruDurumlari, basvuruDurumlariAPI),
            kurum = selectedKurum,
            siralamaApi = mapSiralama(selectedSiralama, siralamalar, siralamalarAPI),
            onLoading = { isLoading = it },
            onError = { msg -> alertMessage = msg; showAlert = true },
            onSuccess = { list, pages -> projectsArr = list; totalPages = maxOf(pages, 1) }
        )
    }

    // Arama alanı debounce
    LaunchedEffect(selectedAd) {
        currentPage = 1
        delay(300)
        loadProjects(
            manager = projectManager,
            type = projectsType,
            page = currentPage,
            sektor = selectedSektor,
            search = selectedAd,
            durumApi = mapDurum(selectedDurum, basvuruDurumlari, basvuruDurumlariAPI),
            kurum = selectedKurum,
            siralamaApi = mapSiralama(selectedSiralama, siralamalar, siralamalarAPI),
            onLoading = { isLoading = it },
            onError = { msg -> alertMessage = msg; showAlert = true },
            onSuccess = { list, pages -> projectsArr = list; totalPages = maxOf(pages, 1) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(projectsType) },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filtreler")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            // Arama alanı
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = selectedAd,
                    onValueChange = { selectedAd = it },
                    singleLine = true,
                    placeholder = { Text("Ad ara") },
                    modifier = Modifier.weight(1f)
                )
                if (selectedAd.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            selectedAd = ""
                            currentPage = 1
                            scope.launch {
                                loadProjects(
                                    manager = projectManager,
                                    type = projectsType,
                                    page = currentPage,
                                    sektor = selectedSektor,
                                    search = selectedAd,
                                    durumApi = mapDurum(selectedDurum, basvuruDurumlari, basvuruDurumlariAPI),
                                    kurum = selectedKurum,
                                    siralamaApi = mapSiralama(selectedSiralama, siralamalar, siralamalarAPI),
                                    onLoading = { isLoading = it },
                                    onError = { msg -> alertMessage = msg; showAlert = true },
                                    onSuccess = { list, pages -> projectsArr = list; totalPages = maxOf(pages, 1) }
                                )
                            }
                        }
                    ) { Icon(Icons.Filled.Close, contentDescription = "Temizle") }
                }
            }

            // Liste / Yükleniyor
            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (isLoading) {
                    Column(
                        Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
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
                            items = projectsArr,
                            key = { it.id }
                        ) { project ->
                            ProjectRow(
                                project = project,
                                projectsType = projectsType,
                                onOpenAIWizard = onOpenAIWizard
                            )
                        }
                    }
                }
            }

            // Sayfalama
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val canPrev = currentPage > 1
                val canNext = currentPage < totalPages

                FilledTonalIconButton(
                    onClick = {
                        if (canPrev) {
                            currentPage -= 1
                            scope.launch {
                                loadProjects(
                                    manager = projectManager,
                                    type = projectsType,
                                    page = currentPage,
                                    sektor = selectedSektor,
                                    search = selectedAd,
                                    durumApi = mapDurum(selectedDurum, basvuruDurumlari, basvuruDurumlariAPI),
                                    kurum = selectedKurum,
                                    siralamaApi = mapSiralama(selectedSiralama, siralamalar, siralamalarAPI),
                                    onLoading = { isLoading = it },
                                    onError = { msg -> alertMessage = msg; showAlert = true },
                                    onSuccess = { list, pages -> projectsArr = list; totalPages = maxOf(pages, 1) }
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
                                loadProjects(
                                    manager = projectManager,
                                    type = projectsType,
                                    page = currentPage,
                                    sektor = selectedSektor,
                                    search = selectedAd,
                                    durumApi = mapDurum(selectedDurum, basvuruDurumlari, basvuruDurumlariAPI),
                                    kurum = selectedKurum,
                                    siralamaApi = mapSiralama(selectedSiralama, siralamalar, siralamalarAPI),
                                    onLoading = { isLoading = it },
                                    onError = { msg -> alertMessage = msg; showAlert = true },
                                    onSuccess = { list, pages -> projectsArr = list; totalPages = maxOf(pages, 1) }
                                )
                            }
                        }
                    },
                    enabled = canNext
                ) { Icon(Icons.Filled.ChevronRight, contentDescription = "Sonraki") }
            }
        }
    }

    // Filtre bottom sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showFilterSheet = false
                scope.launch {
                    currentPage = 1
                    loadProjects(
                        manager = projectManager,
                        type = projectsType,
                        page = currentPage,
                        sektor = selectedSektor,
                        search = selectedAd,
                        durumApi = mapDurum(selectedDurum, basvuruDurumlari, basvuruDurumlariAPI),
                        kurum = selectedKurum,
                        siralamaApi = mapSiralama(selectedSiralama, siralamalar, siralamalarAPI),
                        onLoading = { isLoading = it },
                        onError = { msg -> alertMessage = msg; showAlert = true },
                        onSuccess = { list, pages -> projectsArr = list; totalPages = maxOf(pages, 1) }
                    )
                }
            }
        ) {
            // Daha önce çevirdiğimiz FilterViewProject composable
            FilterViewProject(
                selectedKurum = selectedKurum,
                onKurumChange = { selectedKurum = it },
                selectedSektor = selectedSektor,
                onSektorChange = { selectedSektor = it },
                selectedName = selectedAd,
                onNameChange = { selectedAd = it },
                selectedDurum = selectedDurum,
                onDurumChange = { selectedDurum = it },
                selectedSiralama = selectedSiralama,
                onSiralamaChange = { selectedSiralama = it },
                kurumlar = kurumlar,
                sektorler = sektorler,
                basvuruDurumlari = basvuruDurumlari,
                siralamalar = siralamalar,
                onClose = {
                    showFilterSheet = false
                    scope.launch {
                        currentPage = 1
                        loadProjects(
                            manager = projectManager,
                            type = projectsType,
                            page = currentPage,
                            sektor = selectedSektor,
                            search = selectedAd,
                            durumApi = mapDurum(selectedDurum, basvuruDurumlari, basvuruDurumlariAPI),
                            kurum = selectedKurum,
                            siralamaApi = mapSiralama(selectedSiralama, siralamalar, siralamalarAPI),
                            onLoading = { isLoading = it },
                            onError = { msg -> alertMessage = msg; showAlert = true },
                            onSuccess = { list, pages -> projectsArr = list; totalPages = maxOf(pages, 1) }
                        )
                    }
                }
            )
        }
    }

    // Swift'teki .alert karşılığı
    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            confirmButton = { TextButton(onClick = { showAlert = false }) { Text("Tamam") } },
            title = { Text("Uyarı") },
            text = { Text(alertMessage) }
        )
    }
}

/* -------------------- Yardımcılar -------------------- */

private fun mapDurum(
    selected: String,
    display: List<String>,
    api: List<String>
): String {
    val idx = display.indexOf(selected)
    return if (idx in api.indices) api[idx] else ""
}

private fun mapSiralama(
    selected: String,
    display: List<String>,
    api: List<String>
): String {
    val idx = display.indexOf(selected)
    return if (idx in api.indices) api[idx] else ""
}

/** ProjectManager.getProject çağrısı ve state güncelleme */
private suspend fun loadProjects(
    manager: ProjectManager,
    type: String,
    page: Int,
    sektor: String,
    search: String,
    durumApi: String,
    kurum: String,
    siralamaApi: String,
    onLoading: (Boolean) -> Unit,
    onError: (String) -> Unit,
    onSuccess: (List<Projects>, Int) -> Unit
) {
    onLoading(true)
    val res = manager.getProject(
        tur = type,
        page = page,
        sector = sektor,
        search = search,
        status = durumApi,
        company = kurum,
        sortOrder = siralamaApi
    )
    onLoading(false)
    res.onSuccess { pageRes ->
        onSuccess(pageRes.items, pageRes.totalPages)
    }.onFailure { e ->
        onError(e.message ?: "Bilinmeyen hata")
    }
}
