package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.GymAppRoot
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.WorkoutViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val file = java.io.File(cacheDir, "crash_log.txt")
                file.appendText("CRASH in ${thread.name}: ${throwable.stackTraceToString()}\n")
            } catch (e: Exception) {}
            defaultHandler?.uncaughtException(thread, throwable)
        }
        
        enableEdgeToEdge()
        setContent {
            val viewModel: WorkoutViewModel = viewModel()
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            MyApplicationTheme(darkTheme = isDarkTheme) {
                // Initialize modern State ViewModel
                GymAppRoot(viewModel = viewModel)
            }
        }
    }
}
