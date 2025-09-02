package com.enm.projesihirbaziapp.Abstraction

import com.enm.projesihirbaziapp.Models.Academician

interface AcademicianService {
    fun getAcademics(
        currentPage: Int,
        selectedName: String,
        selectedProvince: String,
        selectedUniversity: String,
        selectedKeywords: String,
        completion: (Result<Pair<List<Academician>, Int>>) -> Unit
    )
}

