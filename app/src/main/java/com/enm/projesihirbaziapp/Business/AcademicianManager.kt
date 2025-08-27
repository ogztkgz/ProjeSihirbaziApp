package com.enm.projesihirbaziapp.Business

import com.enm.projesihirbaziapp.Abstraction.AcademicianService
import com.enm.projesihirbaziapp.DataAccess.AcademicianDataAccess
import com.enm.projesihirbaziapp.Models.Academician

class AcademicianManager(
    private val academicianDataAccess: AcademicianDataAccess = AcademicianDataAccess()
) : AcademicianService {

    override suspend fun getAcademics(
        currentPage: Int,
        selectedName: String,
        selectedProvince: String,
        selectedUniversity: String,
        selectedKeywords: String
    ): Result<List<Academician>> {
        return academicianDataAccess.getAcademics(
            currentPage = currentPage,
            selectedName = selectedName,
            selectedProvince = selectedProvince,
            selectedUniversity = selectedUniversity,
            selectedKeywords = selectedKeywords
        )
    }
}
