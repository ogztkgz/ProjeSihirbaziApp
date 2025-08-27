package com.enm.projesihirbaziapp

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.enm.projesihirbaziapp.navigation.NavGraph
import com.enm.projesihirbaziapp.ui.theme.ProjeSihirbaziAppTheme

@Composable
fun ProjeSihirbaziApp() {
    ProjeSihirbaziAppTheme {
        Surface {
            NavGraph()   // Login -> Home akışını başlatır
        }
    }
}
