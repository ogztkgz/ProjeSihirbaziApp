package com.enm.projesihirbaziapp.Abstraction

interface FiltreService {
    suspend fun getKurumlar(tur: String): Result<List<String>>
    suspend fun getSektorler(): Result<List<String>>
    suspend fun getIl(): Result<List<String>>
    suspend fun getUni(): Result<List<String>>
    suspend fun getKeyword(): Result<List<String>>
}
