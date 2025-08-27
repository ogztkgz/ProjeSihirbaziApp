package com.enm.projesihirbaziapp.Abstraction

import com.enm.projesihirbaziapp.Models.Dashboard

interface DashboardService {
    suspend fun fetchDashboardData(): Result<Dashboard>
}
