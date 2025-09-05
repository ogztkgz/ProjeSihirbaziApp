package com.enm.projesihirbaziapp.Models

import com.google.gson.annotations.SerializedName
import java.util.UUID

/**
 * Swift'teki ChatMessage yapısının birebir Kotlin karşılığı.
 * JSON'da alan adları "Sender", "Text", "FileInfo" olduğu için @SerializedName kullanıyoruz.
 * localId yalnızca UI listeleri için stabil anahtar sağlamak amacıyla eklendi ve JSON'a dahil edilmez.
 */
data class ChatMessage(
    @SerializedName("Sender") val sender: String,
    @SerializedName("Text")   val text: String,
    @SerializedName("FileInfo") val fileInfo: String?

) {
    @Transient
    val localId: String = UUID.randomUUID().toString()
    val id: String = UUID.randomUUID().toString()
}
