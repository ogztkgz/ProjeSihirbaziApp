package com.enm.projesihirbaziapp.Screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
fun FilterViewAcademician(
    selectedUniversity: String,
    onUniversityChange: (String) -> Unit,
    selectedProvince: String,
    onProvinceChange: (String) -> Unit,
    selectedKeywords: String,
    onKeywordsChange: (String) -> Unit,
    iller: List<String>,
    universiteler: List<String>,
    anahtarKelimeler: List<String>,
    onClose: () -> Unit
) {
    var searchUniversity by remember { mutableStateOf("") }
    var searchProvince by remember { mutableStateOf("") }
    var searchKeywords by remember { mutableStateOf("") }

    var showUniversityDropdown by remember { mutableStateOf(false) }
    var showProvinceDropdown by remember { mutableStateOf(false) }
    var showKeywordsDropdown by remember { mutableStateOf(false) }

    val filteredUniversities by remember(universiteler, searchUniversity) {
        mutableStateOf(
            if (searchUniversity.isBlank()) universiteler
            else universiteler.filter { it.contains(searchUniversity, ignoreCase = true) }
        )
    }
    val filteredProvinces by remember(iller, searchProvince) {
        mutableStateOf(
            if (searchProvince.isBlank()) iller
            else iller.filter { it.contains(searchProvince, ignoreCase = true) }
        )
    }
    val filteredKeywords by remember(anahtarKelimeler, searchKeywords) {
        mutableStateOf(
            if (searchKeywords.isBlank()) anahtarKelimeler
            else anahtarKelimeler.filter { it.contains(searchKeywords, ignoreCase = true) }
        )
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        // Başlık + Kapat
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                "Filtreler",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onClose) { Text("Kapat") }
        }

        Spacer(Modifier.height(12.dp))

        // Üniversite
        SectionCard {
            TextLabel("Üniversite")
            SearchField(
                placeholder = "Üniversite ara",
                value = searchUniversity,
                onValueChange = { searchUniversity = it },
                expanded = showUniversityDropdown,
                onToggleExpand = { showUniversityDropdown = !showUniversityDropdown }
            )
            if (showUniversityDropdown) {
                ListBox(
                    items = filteredUniversities.take(50),
                    onPick = {
                        onUniversityChange(it)
                        searchUniversity = it
                        showUniversityDropdown = false
                    }
                )
            } else if (selectedUniversity.isNotBlank()) {
                SelectedPill(selectedUniversity) { onUniversityChange("") }
            }
        }

        Spacer(Modifier.height(12.dp))

        // İl
        SectionCard {
            TextLabel("İl")
            SearchField(
                placeholder = "İl ara",
                value = searchProvince,
                onValueChange = { searchProvince = it },
                expanded = showProvinceDropdown,
                onToggleExpand = { showProvinceDropdown = !showProvinceDropdown }
            )
            if (showProvinceDropdown) {
                ListBox(
                    items = filteredProvinces.take(50),
                    onPick = {
                        onProvinceChange(it)
                        searchProvince = it
                        showProvinceDropdown = false
                    }
                )
            } else if (selectedProvince.isNotBlank()) {
                SelectedPill(selectedProvince) { onProvinceChange("") }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Anahtar Kelime
        SectionCard {
            TextLabel("Anahtar Kelime")
            SearchField(
                placeholder = "Anahtar kelime ara",
                value = searchKeywords,
                onValueChange = { searchKeywords = it },
                expanded = showKeywordsDropdown,
                onToggleExpand = { showKeywordsDropdown = !showKeywordsDropdown }
            )
            if (showKeywordsDropdown) {
                ListBox(
                    items = filteredKeywords.take(50),
                    onPick = {
                        onKeywordsChange(it)
                        searchKeywords = it
                        showKeywordsDropdown = false
                    }
                )
            } else if (selectedKeywords.isNotBlank()) {
                SelectedPill(selectedKeywords) { onKeywordsChange("") }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Butonlar
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = {
                    onUniversityChange("")
                    onProvinceChange("")
                    onKeywordsChange("")
                    searchUniversity = ""
                    searchProvince = ""
                    searchKeywords = ""
                    showUniversityDropdown = false
                    showProvinceDropdown = false
                    showKeywordsDropdown = false
                },
                modifier = Modifier.weight(1f)
            ) { Text("Temizle") }

            Button(onClick = onClose, modifier = Modifier.weight(1f)) { Text("Uygula") }
        }
    }
}

/* ---------- Alt bileşenler ---------- */

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) { Column(Modifier.padding(16.dp), content = content) }
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
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "chevronRotation")
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onToggleExpand) {
            Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = null,
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}

@Composable
private fun ListBox(
    items: List<String>,
    onPick: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 180.dp)
    ) {
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
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = CircleShape
            )
            .border(0.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.0f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.width(6.dp))
        IconButton(onClick = onRemove, modifier = Modifier.size(18.dp)) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
