package com.enm.projesihirbaziapp.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.enm.projesihirbaziapp.R
import java.util.Calendar

@Composable
fun BeforeLoginUI(
    onLoginClick: () -> Unit,
    onOpenPrivacy: () -> Unit = {}
) {
    val year = remember { Calendar.getInstance().get(Calendar.YEAR) }

    Scaffold { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Logo + başlık
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Proje Sihirbazı",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Hesabınla giriş yaparak kişiselleştirilmiş deneyime geç.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Kart + Butonlar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Giriş Yap") }

                    Spacer(Modifier.height(8.dp))

                    TextButton(
                        onClick = onOpenPrivacy,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Gizlilik ve Koşullar") }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "© $year ENM Digital",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(bottom = 12.dp)
            )
        }
    }
}
