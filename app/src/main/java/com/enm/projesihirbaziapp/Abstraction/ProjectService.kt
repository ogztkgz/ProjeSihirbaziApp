package com.enm.projesihirbaziapp.Abstraction

import com.enm.projesihirbaziapp.Models.Projects

data class ProjectPage(
    val items: List<Projects>,
    val totalPages: Int
)

interface ProjectService {
    suspend fun getProject(
        tur: String,
        page: Int,
        sector: String,
        search: String,
        status: String,
        company: String,
        sortOrder: String
    ): Result<ProjectPage>
}
