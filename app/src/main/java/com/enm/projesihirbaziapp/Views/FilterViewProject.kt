package com.enm.projesihirbaziapp.Screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FilterViewProject(
    selectedKurum: String,
    onKurumChange: (String) -> Unit,
    selectedSektor: String,
    onSektorChange: (String) -> Unit,
    selectedName: String,
    onNameChange: (String) -> Unit,
    selectedDurum: String,
    onDurumChange: (String) -> Unit,
    selectedSiralama: String,
    onSiralamaChange: (String) -> Unit,
    kurumlar: List<String> = emptyList(),
    sektorler: List<String> = emptyList(),
    basvuruDurumlari: List<String> = emptyList(),
    siralamalar: List<String> = emptyList(),
    onClose: () -> Unit
) {
    var searchKurum by remember { mutableStateOf("") }
    var searchSektor by remember { mutableStateOf("") }
    var showKurumDropdown by remember { mutableStateOf(false) }
    var showSektorDropdown by remember { mutableStateOf(false) }
    var showDurumDropdown by remember { mutableStateOf(false) }
    var showSiralamaDropdown by remember { mutableStateOf(false) }

    val filteredKurumlar by remember(kurumlar, searchKurum) {
        mutableStateOf(if (searchKurum.isBlank()) kurumlar else kurumlar.filter { it.contains(searchKurum, true) })
    }
    val filteredSektorler by remember(sektorler, searchSektor) {
        mutableStateOf(if (searchSektor.isBlank()) sektorler else sektorler.filter { it.contains(searchSektor, true) })
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        // Başlık + Kapat
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Filtreler", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            TextButton(onClick = onClose) { Text("Kapat") }
        }

        Spacer(Modifier.height(12.dp))

        // Kurum
        SectionCard {
            TextLabel("Kurum")
            SearchField(
                placeholder = "Kurum ara",
                value = searchKurum,
                onValueChange = { searchKurum = it },
                expanded = showKurumDropdown,
                onToggleExpand = { showKurumDropdown = !showKurumDropdown }
            )
            if (showKurumDropdown) {
                ListBox(
                    items = filteredKurumlar.take(50),
                    onPick = {
                        onKurumChange(it)
                        searchKurum = it
                        showKurumDropdown = false
                    }
                )
            } else if (selectedKurum.isNotBlank()) {
                SelectedPill(text = selectedKurum) { onKurumChange("") }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Sektör
        SectionCard {
            TextLabel("Sektör")
            SearchField(
                placeholder = "Sektör ara",
                value = searchSektor,
                onValueChange = { searchSektor = it },
                expanded = showSektorDropdown,
                onToggleExpand = { showSektorDropdown = !showSektorDropdown }
            )
            if (showSektorDropdown) {
                ListBox(
                    items = filteredSektorler.take(50),
                    onPick = {
                        onSektorChange(it)
                        searchSektor = it
                        showSektorDropdown = false
                    }
                )
            } else if (selectedSektor.isNotBlank()) {
                SelectedPill(text = selectedSektor) { onSektorChange("") }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Başvuru Durumu
        SectionCard {
            TextLabel("Başvuru Durumu")
            SelectorRow(
                title = if (selectedDurum.isBlank()) "Başvuru durumu seçin" else selectedDurum,
                expanded = showDurumDropdown,
                onToggle = { showDurumDropdown = !showDurumDropdown }
            )
            if (showDurumDropdown) {
                PickerList(items = basvuruDurumlari) {
                    onDurumChange(it)
                    showDurumDropdown = false
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Sıralama
        SectionCard {
            TextLabel("Sıralama")
            SelectorRow(
                title = if (selectedSiralama.isBlank()) "Sıralama seçin" else selectedSiralama,
                expanded = showSiralamaDropdown,
                onToggle = { showSiralamaDropdown = !showSiralamaDropdown }
            )
            if (showSiralamaDropdown) {
                PickerList(items = siralamalar) {
                    onSiralamaChange(it)
                    showSiralamaDropdown = false
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Butonlar
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = {
                    onKurumChange("")
                    onSektorChange("")
                    onDurumChange("")
                    onSiralamaChange("")
                    onNameChange("")
                    searchKurum = ""
                    searchSektor = ""
                    showKurumDropdown = false
                    showSektorDropdown = false
                    showDurumDropdown = false
                    showSiralamaDropdown = false
                },
                modifier = Modifier.weight(1f)
            ) { Text("Temizle") }

            Button(onClick = onClose, modifier = Modifier.weight(1f)) { Text("Uygula") }
        }
    }
}

/* ----------------- Alt bileşenler ----------------- */

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun TextLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun SearchField(
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    expanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "chevronRotation")
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            placeholder = { Text(placeholder) },
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onToggleExpand) {
            Icon(Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.rotate(rotation))
        }
    }
}

@Composable
private fun ListBox(
    items: List<String>,
    onPick: (String) -> Unit
) {
    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().heightIn(max = 180.dp)) {
        LazyColumn {
            items(items) { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(item) }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(item)
                }
                Divider()
            }
        }
    }
}

@Composable
private fun PickerList(
    items: List<String>,
    onPick: (String) -> Unit
) {
    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp)) {
        LazyColumn {
            items(items) { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(item) }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(item)
                }
                Divider()
            }
        }
    }
}

@Composable
private fun SelectedPill(text: String, onRemove: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.width(6.dp))
        IconButton(onClick = onRemove, modifier = Modifier.size(18.dp)) {
            Icon(Icons.Filled.Close, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorRow(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "selectorChevronRotation"
    )

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onToggle, // kartın tamamı tıklanabilir
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                color = if (title.contains("seçin", ignoreCase = true))
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
            Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = null,
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}
