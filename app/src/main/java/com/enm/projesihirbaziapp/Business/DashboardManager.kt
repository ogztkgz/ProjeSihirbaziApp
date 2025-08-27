package com.enm.projesihirbaziapp.Business

import com.enm.projesihirbaziapp.Abstraction.DashboardService
import com.enm.projesihirbaziapp.DataAccess.DashboardDataAccess
import com.enm.projesihirbaziapp.Models.Dashboard

class DashboardManager(
    private val dashboardDataAccess: DashboardDataAccess = DashboardDataAccess()
) : DashboardService {

    override suspend fun fetchDashboardData(): Result<Dashboard> {
        return dashboardDataAccess.fetchDashboardData()
    }
}
