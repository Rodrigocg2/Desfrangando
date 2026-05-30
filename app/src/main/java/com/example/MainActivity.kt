package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
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
        try {
            googleSignInClient = GoogleSignIn.getClient(this, gso)
            lastAccount = GoogleSignIn.getLastSignedInAccount(this)
        } catch (e: Throwable) {
            android.util.Log.e("MainActivity", "Failed to initialize standard Google Sign In Client", e)
        }
        
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
            mainViewModel = viewModel
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
}
