package com.enm.projesihirbaziapp.Business

import com.enm.projesihirbaziapp.Abstraction.FiltreService
import com.enm.projesihirbaziapp.DataAccess.FiltreDataAccess

class FiltreManager(
    private val filtreDataAccess: FiltreDataAccess = FiltreDataAccess()
) : FiltreService {

    override suspend fun getKurumlar(tur: String): Result<List<String>> =
        filtreDataAccess.getKurumlar(tur)

    override suspend fun getSektorler(): Result<List<String>> =
        filtreDataAccess.getSektorler()

    override suspend fun getIl(): Result<List<String>> =
        filtreDataAccess.getIl()

    override suspend fun getUni(): Result<List<String>> =
        filtreDataAccess.getUni()

    override suspend fun getKeyword(): Result<List<String>> =
        filtreDataAccess.getKeyword()
}
