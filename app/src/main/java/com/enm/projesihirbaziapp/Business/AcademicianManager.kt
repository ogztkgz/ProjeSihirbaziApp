package com.enm.projesihirbaziapp.Business

import com.enm.projesihirbaziapp.Models.Academician
import com.enm.projesihirbaziapp.Abstraction.AcademicianService
import com.enm.projesihirbaziapp.DataAccess.AcademicianDataAccess

class AcademicianManager : AcademicianService {

    private val academicianDataAccess = AcademicianDataAccess() // Data Access katmanı

    override fun getAcademics(
        currentPage: Int,
        selectedName: String,
        selectedProvince: String,
        selectedUniversity: String,
        selectedKeywords: String,
        completion: (Result<Pair<List<Academician>, Int>>) -> Unit
    ) {
        academicianDataAccess.getAcademics(
            currentPage = currentPage,
            selectedName = selectedName,
            selectedProvince = selectedProvince,
            selectedUniversity = selectedUniversity,
            selectedKeywords = selectedKeywords
        ) { result ->
            // Kotlin Result için onSuccess / onFailure extension'larını kullan
            result
                .onSuccess { pair ->
                    completion(Result.success(pair))   // başarıyı forward et
                }
                .onFailure { e ->
                    completion(Result.failure(e))      // hatayı forward et
                }
        }
    }
}
