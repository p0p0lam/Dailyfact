package com.popolam.app.dailyfact

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.popolam.app.dailyfact.ui.compose.FactScreen
import com.popolam.app.dailyfact.ui.theme.DailyFactTheme
import com.popolam.app.dailyfact.ui.viewmodel.FactViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val factViewModel: FactViewModel by viewModel() // Inject ViewModel via Koin
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyFactTheme {
                    FactScreen(viewModel = factViewModel)
            }
        }
    }
}
