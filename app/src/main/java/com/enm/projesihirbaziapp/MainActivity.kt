package com.enm.projesihirbaziapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.enm.projesihirbaziapp.ui.theme.ProjeSihirbaziAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjeSihirbaziAppTheme {
                ProjeSihirbaziApp()   // <<< Artık NavGraph'ı başlatıyoruz
            }
        }
    }
}
