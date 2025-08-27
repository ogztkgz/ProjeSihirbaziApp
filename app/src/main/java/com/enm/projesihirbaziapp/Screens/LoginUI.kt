package com.enm.projesihirbaziapp.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.enm.projesihirbaziapp.R
import com.enm.projesihirbaziapp.Business.UserManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginUI(
    onNavigateHome: () -> Unit
) {
    val context = LocalContext.current
    val manager = remember { UserManager(context) }
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }
    var showForgotPassword by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(28.dp))

            // Logo (drawable/logo.xml veya logo.png olmalı)

                Image(
                    painter = painterResource(id = R.drawable.logo), // dosya adın farklıysa burada değiştir
                    contentDescription = null,
                    modifier = Modifier.size(120.dp)
                )


            Spacer(Modifier.height(16.dp))
            Text("Giriş Yap", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))

            // E-posta
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-posta") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Şifre
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Şifre") },
                singleLine = true,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        val iconId = if (isPasswordVisible) android.R.drawable.ic_menu_view
                        else android.R.drawable.ic_secure
                        Icon(painter = painterResource(id = iconId), contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Giriş Yap
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        val success = manager.logIn(email, password)
                        isLoading = false
                        if (success) {
                            onNavigateHome()
                        } else {
                            alertMessage = "Giriş başarısız. Lütfen bilgilerinizi kontrol edin."
                            showAlert = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Giriş Yap")
            }

            // Şifremi Unuttum
            TextButton(onClick = { showForgotPassword = true }) {
                Text("Şifremi Unuttum?")
            }

            Spacer(Modifier.height(24.dp))
        }

        // Loading overlay
        if (isLoading) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    // Alert
    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            confirmButton = {
                TextButton(onClick = { showAlert = false }) { Text("Tamam") }
            },
            title = { Text("Uyarı") },
            text = { Text(alertMessage) }
        )
    }

    // Forgot Password Sheet
    if (showForgotPassword) {
        ModalBottomSheet(
            onDismissRequest = { showForgotPassword = false },
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .fillMaxWidth()
            ) {
                Text("Şifremi Unuttum", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = forgotPasswordEmail,
                    onValueChange = { forgotPasswordEmail = it },
                    singleLine = true,
                    label = { Text("E-posta") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            val msg = manager.forgetPassword(forgotPasswordEmail)
                            isLoading = false
                            alertMessage = msg
                            showAlert = true
                            showForgotPassword = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Gönder") }

                TextButton(
                    onClick = { showForgotPassword = false },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("İptal") }
            }
        }
    }
}
