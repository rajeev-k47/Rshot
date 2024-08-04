package net.runner.rshot

import LoginScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import net.runner.rshot.composables.MainScreen
import net.runner.rshot.ui.theme.RshotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RshotTheme {

                val systemUiController = rememberSystemUiController()
                if(isSystemInDarkTheme()){
                    systemUiController.setSystemBarsColor(
                        color = MaterialTheme.colorScheme.background
                    )
                }else{
                    systemUiController.setSystemBarsColor(
                        color = MaterialTheme.colorScheme.background
                    )
                }
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "LoginScreen"
                ) {
                    composable(route = "LoginScreen") {
                       LoginScreen(navController)
                    }
                    composable(route = "MainScreen") {
                        MainScreen()
                    }
                }
            }

         }
    }
}

