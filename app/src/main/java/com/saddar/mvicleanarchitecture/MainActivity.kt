package com.saddar.mvicleanarchitecture

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.saddar.mvicleanarchitecture.presentation.PostScreen
import com.saddar.mvicleanarchitecture.ui.theme.MVICleanArchitectureTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint          // Required by Hilt on every Activity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MVICleanArchitectureTheme {
                PostScreen()
            }
        }
    }
}