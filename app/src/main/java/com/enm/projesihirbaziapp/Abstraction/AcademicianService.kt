package com.enm.projesihirbaziapp.Abstraction

import com.enm.projesihirbaziapp.Models.Academician

interface AcademicianService {
    suspend fun getAcademics(
        currentPage: Int,
        selectedName: String,
        selectedProvince: String,
        selectedUniversity: String,
        selectedKeywords: String
    ): Result<List<Academician>>
}
