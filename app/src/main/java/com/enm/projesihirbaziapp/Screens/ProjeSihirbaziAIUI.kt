@file:OptIn(ExperimentalMaterial3Api::class)

package com.enm.projesihirbaziapp.Screens

import android.app.Application
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.enm.projesihirbaziapp.Abstraction.ProjeSihirbaziAIService
import com.enm.projesihirbaziapp.Business.ProjeSihirbaziAIManager
import com.enm.projesihirbaziapp.Models.ChatMessage
import com.enm.projesihirbaziapp.Models.ProjeSihirbaziAI
import kotlinx.coroutines.launch

/* =========================
 * ViewModel (Coroutine'lu)
 * ========================= */
class ProjeSihirbaziAIViewModel(
    application: Application,
    private val projectId: Int,
    private val service: ProjeSihirbaziAIService = ProjeSihirbaziAIManager()
) : AndroidViewModel(application) {

    var messageText by mutableStateOf("")
        private set

    var chat by mutableStateOf(listOf<ChatMessage>())
        private set

    var chatId by mutableIntStateOf(0)
        private set

    var chats by mutableStateOf(listOf<ProjeSihirbaziAI>())
        private set

    var showAlert by mutableStateOf(false)
        private set
    var alertMessage by mutableStateOf("")
        private set

    var showOldChats by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isSending by mutableStateOf(false)
        private set

    private val prefs by lazy {
        getApplication<Application>().getSharedPreferences("proje_sihirbazi_prefs", Context.MODE_PRIVATE)
    }

    private val token: String
        get() = getApplication<Application>()
            .getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .getString("accessToken", "") ?: ""

    init {
        chatId = prefs.getInt("selectedChatId", 0)
    }

    fun onAppear() {
        if (token.isNotBlank() && chatId != 0) {
            loadChat(chatId)
        }
    }

    fun refresh() {
        if (token.isNotBlank() && chatId != 0) loadChat(chatId)
    }

    /* -------- Old Chats -------- */
    fun openOldChats() {
        showOldChats = true
        viewModelScope.launch {
            if (token.isBlank()) return@launch inform("Oturum bulunamadı.")
            isLoading = true
            val res = service.getOldChat(projectId, token)
            isLoading = false
            res.onSuccess { chats = it }
                .onFailure { inform("Eski sohbetler alınamadı.\n${it.localizedMessage ?: it.message.orEmpty()}") }
        }
    }

    fun closeOldChats() { showOldChats = false }

    fun tapOldChat(item: ProjeSihirbaziAI) {
        chatId = item.id
        prefs.edit().putInt("selectedChatId", chatId).apply()
        closeOldChats()
        loadChat(chatId)
    }

    fun createNewChat() {
        viewModelScope.launch {
            if (token.isBlank()) return@launch inform("Oturum bulunamadı.")
            isLoading = true
            val res = service.createNewChat(projectId, token)
            isLoading = false
            res.onSuccess {
                chatId = it.id
                prefs.edit().putInt("selectedChatId", chatId).apply()
                closeOldChats()
                loadChat(chatId)
            }.onFailure {
                inform("Yeni sohbet oluşturulamadı.\n${it.localizedMessage ?: it.message.orEmpty()}")
            }
        }
    }

    fun deleteChat(item: ProjeSihirbaziAI) {
        viewModelScope.launch {
            if (token.isBlank()) return@launch inform("Oturum bulunamadı.")
            isLoading = true
            val res = service.deleteChat(token, item.id)
            isLoading = false
            res.onSuccess {
                chats = chats.filterNot { it.id == item.id }
                if (chatId == item.id) {
                    chatId = 0
                    prefs.edit().remove("selectedChatId").apply()
                    chat = emptyList()
                }
            }.onFailure {
                inform("Sohbet silinemedi.\n${it.localizedMessage ?: it.message.orEmpty()}")
            }
        }
    }

    /* -------- Send -------- */
    fun onMessageTextChange(v: String) { messageText = v }

    fun sendMessageClicked() {
        if (messageText.trim().isEmpty()) return
        if (chatId == 0) {
            viewModelScope.launch {
                if (token.isBlank()) return@launch inform("Oturum bulunamadı.")
                isSending = true
                val res = service.createNewChat(projectId, token)
                res.onSuccess {
                    chatId = it.id
                    prefs.edit().putInt("selectedChatId", chatId).apply()
                    sendMessageInternal()
                }.onFailure {
                    isSending = false
                    inform("Yeni sohbet oluşturulamadı.\n${it.localizedMessage ?: it.message.orEmpty()}")
                }
            }
        } else {
            viewModelScope.launch { sendMessageInternal() }
        }
    }

    private suspend fun sendMessageInternal() {
        if (token.isBlank()) { inform("Oturum bulunamadı."); return }
        val payload = messageText
        isSending = true
        val res = service.sendMessage(chatId, payload, token)
        isSending = false
        res.onSuccess {
            messageText = ""
            loadChat(chatId)
        }.onFailure {
            inform("Mesaj gönderilemedi.\n${it.localizedMessage ?: it.message.orEmpty()}")
        }
    }

    /* -------- Fetch -------- */
    private fun loadChat(id: Int) {
        viewModelScope.launch {
            if (token.isBlank()) return@launch inform("Oturum bulunamadı.")
            isLoading = true
            val res = service.getChatWithId(projectId, id, token)
            isLoading = false
            res.onSuccess { chat = it }
                .onFailure { inform("Sohbet yüklenemedi.\n${it.localizedMessage ?: it.message.orEmpty()}") }
        }
    }

    /* -------- Utils -------- */
    private fun inform(msg: String) { alertMessage = msg; showAlert = true }
    fun dismissAlert() { showAlert = false }
}

/* VM Factory — projectId geçmek için */
class ProjeSihirbaziAIViewModelFactory(
    private val app: Application,
    private val projectId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ProjeSihirbaziAIViewModel::class.java))
        return ProjeSihirbaziAIViewModel(app, projectId) as T
    }
}

/* =========================
 * Screen (Compose)
 * ========================= */
@Composable
fun ProjeSihirbaziAIUI(
    projectId: Int,
    onBack: (() -> Unit)? = null
) {
    val vm: ProjeSihirbaziAIViewModel = viewModel(
        factory = ProjeSihirbaziAIViewModelFactory(
            app = LocalContext.current.applicationContext as Application,
            projectId = projectId
        )
    )

    LaunchedEffect(Unit) { vm.onAppear() }

    if (vm.showAlert) {
        AlertDialog(
            onDismissRequest = { vm.dismissAlert() },
            confirmButton = { TextButton(onClick = { vm.dismissAlert() }) { Text("Tamam") } },
            title = { Text("Bilgi") },
            text = { Text(vm.alertMessage) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sohbet") },
                navigationIcon = {
                    onBack?.let {
                        IconButton(onClick = it) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Geri")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { vm.openOldChats() }) {
                        Icon(Icons.Filled.Chat, contentDescription = "Sohbetler")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { vm.createNewChat() }) {
                Icon(Icons.Filled.AddCircle, contentDescription = "Yeni Sohbet")
            }
        }
    ) { inner ->
        Column(Modifier.padding(inner).fillMaxSize()) {

            // Mesaj listesi — weight burada veriliyor
            MessagesList(
                modifier = Modifier.weight(1f),
                items = vm.chat,
                isLoading = vm.isLoading,
                onRefresh = { vm.refresh() }
            )

            // Giriş çubuğu
            InputBar(
                text = vm.messageText,
                isSending = vm.isSending,
                onTextChange = vm::onMessageTextChange,
                onSend = vm::sendMessageClicked
            )
        }
    }

    // Eski sohbetler: ModalBottomSheet
    if (vm.showOldChats) {
        ModalBottomSheet(onDismissRequest = { vm.closeOldChats() }) {
            OldChatsSheet(
                isLoading = vm.isLoading,
                chats = vm.chats,
                onPick = vm::tapOldChat,
                onNewChat = vm::createNewChat,
                onDelete = vm::deleteChat,
                onClose = vm::closeOldChats
            )
        }
    }
}

/* -------- Alt bileşenler -------- */

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(44.dp))
        Spacer(Modifier.height(8.dp))
        Text("Henüz mesaj yok", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            "Bir mesaj yazarak sohbete başlayabilirsin.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MessagesList(
    modifier: Modifier = Modifier,
    items: List<ChatMessage>,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Box(modifier.fillMaxWidth()) {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // ChatMessage modelinizde 'id' yoksa güvenli bir key üretin:
            items(
                items = items,
                key = { msg -> "${msg.sender}-${msg.text.hashCode()}" }
            ) { msg ->
                ChatBubble(message = msg)
            }
        }
        if (isLoading && items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun InputBar(
    text: String,
    isSending: Boolean,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            singleLine = false,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp),
            placeholder = { Text("Mesajınızı yazın…") }
        )
        Spacer(Modifier.width(8.dp))
        Button(onClick = onSend, enabled = text.trim().isNotEmpty() && !isSending) {
            if (isSending) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Gönderiliyor")
            } else {
                Icon(Icons.Filled.Send, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Gönder")
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.sender.lowercase() == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                color = if (isUser) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun OldChatsSheet(
    isLoading: Boolean,
    chats: List<ProjeSihirbaziAI>,
    onPick: (ProjeSihirbaziAI) -> Unit,
    onNewChat: () -> Unit,
    onDelete: (ProjeSihirbaziAI) -> Unit,
    onClose: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            "Eski Sohbetler",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(16.dp)
        )

        if (isLoading && chats.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        LazyColumn {
            items(chats, key = { it.id }) { item ->
                ListItem(
                    headlineContent = { Text("Chat #${item.id}") },
                    supportingContent = { Text(item.createdDateTime, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    },
                    trailingContent = {
                        IconButton(onClick = { onDelete(item) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Sil")
                        }
                    },
                    modifier = Modifier.clickable { onPick(item) }
                )
                Divider()
            }
        }

        Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onClose, modifier = Modifier.weight(1f)) { Text("Kapat") }
            Button(onClick = onNewChat, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.AddCircle, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Yeni Sohbet Oluştur")
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
