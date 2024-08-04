package net.runner.rshot

import LoginScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
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
                        MainScreen(viewModel(),navController)
                    }
                    composable(route = "image_viewer/{imageUrl}", arguments = listOf(navArgument("imageUrl") { type = NavType.StringType })) {backStackEntry ->
                        val imageUrl = backStackEntry.arguments?.getString("imageUrl")

                        ImageViewerScreen(imageUrl = imageUrl ?: "") {
                            navController.popBackStack()
                        }
                    }
                }
            }

         }
    }
}

@Composable
fun ImageViewerScreen(imageUrl: String, onBack: () -> Unit) {
    val zoomState = remember { mutableStateOf(1f) }
    val isLoading = remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    zoomState.value *= zoom
                }
            }
    ) {
        if (isLoading.value) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = zoomState.value,
                    scaleY = zoomState.value
                ),
            onSuccess = {
                isLoading.value = false
            },
            onError = {
                isLoading.value = false
            }
        )
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
    }
}
