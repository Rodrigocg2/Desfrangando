package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GymAppRoot
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.WorkoutViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: com.google.android.gms.auth.api.signin.GoogleSignInClient
    private var mainViewModel: WorkoutViewModel? = null

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                val name = account.displayName ?: "Rodrigo"
                val email = account.email ?: "Rodrigocg2@gmail.com"
                val photoUrl = account.photoUrl?.toString() ?: ""
                
                val vm = mainViewModel ?: androidx.lifecycle.ViewModelProvider(this)[WorkoutViewModel::class.java]
                vm.loginWithGoogle(name, email, photoUrl)
                Toast.makeText(this, "Conectado como: $email", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Throwable) {
            // If Google login fails (e.g., config error / missing client id on sandbox),
            // tell the viewModel to allow seamless visual dialog fallback
            val vm = mainViewModel ?: androidx.lifecycle.ViewModelProvider(this)[WorkoutViewModel::class.java]
            vm.triggerGoogleLoginError(e.localizedMessage ?: "Erro na autenticação oficial Google")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
            
        var lastAccount: com.google.android.gms.auth.api.signin.GoogleSignInAccount? = null
        var startupError: Throwable? = null
        try {
            googleSignInClient = GoogleSignIn.getClient(this, gso)
            lastAccount = GoogleSignIn.getLastSignedInAccount(this)
        } catch (e: Throwable) {
            android.util.Log.e("MainActivity", "Failed to initialize standard Google Sign In Client", e)
        }

        try {
            mainViewModel = androidx.lifecycle.ViewModelProvider(this)[WorkoutViewModel::class.java]
        } catch (e: Throwable) {
            startupError = e
            android.util.Log.e("MainActivity", "Failed to initialize WorkoutViewModel", e)
        }
        
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val file = java.io.File(cacheDir, "crash_log.txt")
                file.appendText("CRASH in ${thread.name}: ${throwable.stackTraceToString()}\n")
            } catch (e: Exception) {}
            defaultHandler?.uncaughtException(thread, throwable)
        }
        
        try {
            enableEdgeToEdge()
            setContent {
                val errState = startupError
                if (errState != null) {
                    MaterialTheme {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF131215))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "STARTUP ERROR:\n\n${errState.stackTraceToString()}",
                                color = Color.Red,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                modifier = Modifier.verticalScroll(rememberScrollState())
                            )
                        }
                    }
                } else {
                    val viewModel = mainViewModel ?: androidx.lifecycle.ViewModelProvider(this)[WorkoutViewModel::class.java]
                    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
                    
                    androidx.compose.runtime.LaunchedEffect(lastAccount) {
                        if (lastAccount != null && !viewModel.isGoogleLoggedIn.value) {
                            try {
                                val name = lastAccount.displayName ?: "Rodrigo"
                                val email = lastAccount.email ?: "Rodrigocg2@gmail.com"
                                val photoUrl = lastAccount.photoUrl?.toString() ?: ""
                                viewModel.loginWithGoogle(name, email, photoUrl)
                            } catch (e: Throwable) {
                                android.util.Log.e("MainActivity", "Safely skipped auto-login account property verification: ", e)
                            }
                        }
                    }
                    
                    val triggerLoginEvent by viewModel.triggerGoogleLoginEvent.collectAsState()
                    androidx.compose.runtime.LaunchedEffect(triggerLoginEvent) {
                        if (triggerLoginEvent) {
                            try {
                                if (::googleSignInClient.isInitialized) {
                                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                                } else {
                                    viewModel.triggerGoogleLoginError("Google Sign-In não inicializado neste dispositivo.")
                                }
                            } catch (e: Throwable) {
                                viewModel.triggerGoogleLoginError(e.localizedMessage ?: "Erro ao iniciar Login com Google")
                            } finally {
                                viewModel.resetGoogleLoginTrigger()
                            }
                        }
                    }
                    
                    MyApplicationTheme(darkTheme = isDarkTheme) {
                        GymAppRoot(viewModel = viewModel)
                    }
                }
            }
        } catch (t: Throwable) {
            android.util.Log.e("MainActivity", "Failed to set Content", t)
            val tv = android.widget.TextView(this)
            tv.text = "CRITICAL BOOTSTRAP ERROR:\n\n${t.stackTraceToString()}"
            tv.setTextColor(android.graphics.Color.RED)
            tv.setPadding(32, 32, 32, 32)
            tv.movementMethod = android.text.method.ScrollingMovementMethod()
            setContentView(tv)
        }
    }
}
