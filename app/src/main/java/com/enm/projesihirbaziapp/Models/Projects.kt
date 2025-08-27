package com.enm.projesihirbaziapp.Models

data class Projects(
    val id: Int,               // Projenin benzersiz ID'si
    val ad: String,            // Projenin adı
    val resim: String,         // Görsel URL'si
    val kurum: String,         // Kurum adı
    val basvuruDurumu: String, // Başvuru durumu
    val basvuruLinki: String,  // Başvuru linki
    val sektorler: String,     // Sektörler
    val eklenmeTarihi: String, // Eklenme tarihi
    val tur: String            // Proje türü
)
