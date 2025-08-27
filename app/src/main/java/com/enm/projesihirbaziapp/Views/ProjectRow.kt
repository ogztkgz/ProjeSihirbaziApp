package com.enm.projesihirbaziapp.Screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.enm.projesihirbaziapp.Models.Projects  // <-- ÖNEMLİ: modeli import et

@Composable
fun ProjectRow(
    project: Projects,
    projectsType: String,
    onOpenAIWizard: (Int) -> Unit = {}
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    val imageUrl = remember(project.resim) {
        val path = project.resim
        if (path.isNotBlank()) "https://projesihirbaziapi.enmdigital.com/$path" else null
    }
    val detailUrl = remember(project.basvuruLinki) { project.basvuruLinki }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(enabled = detailUrl.isNotBlank()) {
                runCatching { uriHandler.openUri(detailUrl) }
            },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), // outlineVariant yoksa outline kullan
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp)) {

            // Görsel
            if (imageUrl != null) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    },
                    error = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = rememberVectorPainter(Icons.Filled.Image),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Görsel yüklenemedi",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
                Spacer(Modifier.height(12.dp))
            }

            // Metin içerik
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = project.ad,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = project.kurum,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (project.basvuruDurumu.isNotBlank()) {
                        Pill(text = project.basvuruDurumu)
                    }
                    if (project.sektorler.isNotBlank()) {
                        Pill(text = project.sektorler)
                    }
                }

                if (project.eklenmeTarihi.isNotBlank()) {
                    Text(
                        text = project.eklenmeTarihi,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // CTA’lar
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        if (detailUrl.isNotBlank()) {
                            runCatching { uriHandler.openUri(detailUrl) }
                        }
                    }
                ) { Text("Detay / Başvuru Linki") }

                if (projectsType == "Hibe") {
                    Button(onClick = { onOpenAIWizard(project.id) }) {
                        Text("Yapay Zeka ile Konuş")
                    }
                }
            }
        }
    }
}

/** Küçük kapsül etiket */
@Composable
private fun Pill(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
