package com.enm.projesihirbaziapp.Business

import com.enm.projesihirbaziapp.Abstraction.ProjectPage
import com.enm.projesihirbaziapp.Abstraction.ProjectService
import com.enm.projesihirbaziapp.DataAccess.ProjectDataAccess

class ProjectManager(
    private val projectDataAccess: ProjectDataAccess = ProjectDataAccess()
) : ProjectService {

    override suspend fun getProject(
        tur: String,
        page: Int,
        sector: String,
        search: String,
        status: String,
        company: String,
        sortOrder: String
    ) = projectDataAccess.getProject(
        tur = tur,
        page = page,
        sector = sector,
        search = search,
        status = status,
        company = company,
        sortOrder = sortOrder
    )
}
