package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.i18n.LocalAppLanguage
import com.example.ui.screens.WorkspaceMainScaffold
import com.example.ui.theme.AiWorkspaceTheme
import com.example.ui.viewmodel.WorkspaceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: WorkspaceViewModel = viewModel()
            val themePreference by viewModel.selectedTheme.collectAsState()
            val selectedLanguage by viewModel.selectedLanguage.collectAsState()

            CompositionLocalProvider(LocalAppLanguage provides selectedLanguage) {
                AiWorkspaceTheme(themePreference = themePreference) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        WorkspaceMainScaffold(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
