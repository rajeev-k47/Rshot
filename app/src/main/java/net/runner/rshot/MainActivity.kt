package net.runner.rshot

import LoginScreen
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.onesignal.OneSignal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.runner.rshot.composables.MainScreen
import net.runner.rshot.ui.theme.RshotTheme

lateinit var dineData:String
class MainActivity : ComponentActivity() {
    private val viewModel: dineLoaderViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dineData=""
        val ONESIGNAL_APP_ID =BuildConfig.ONESIGNAL_API
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID)
        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(false)
            jsonExists { con->
                if(con=="null"){
                    launch(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Error Loading Dine !! Please try to Upload Dine Again !", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    val firestore = FirebaseFirestore.getInstance()
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    firestore.collection("users").document(userId!!).collection("DineJson")
                        .get()
                        .addOnSuccessListener { result ->
                            for (document in result) {
                                dineData = document.data["dine"].toString()
                                viewModel.setvar(dineData)
                            }
                        }
                        .addOnFailureListener { exception ->
                        }
                }
            }
        }

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
                        MainScreen(viewModel(),viewModel(),navController)
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
    val offsetState = remember { mutableStateOf(Offset(0f, 0f)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomState.value = (zoomState.value * zoom).coerceIn(1f, 5f)
                    offsetState.value = Offset(
                        offsetState.value.x + pan.x / zoomState.value,
                        offsetState.value.y + pan.y / zoomState.value
                    )
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
                    scaleY = zoomState.value,
                    translationX = offsetState.value.x,
                    translationY = offsetState.value.y
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
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
    }
}

@Composable
fun BottomBar(selectedItem: MutableState<Int>, onItemSelected: (Int) -> Unit) {
    val items = listOf("Roll", "Reminders", "Dine")
    val selectedIcons = listOf(Icons.Filled.Home, Icons.Filled.Info, Icons.Filled.DateRange)
    val unselectedIcons = listOf(Icons.Outlined.Home, Icons.Outlined.Info, Icons.Outlined.DateRange)

    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        if (selectedItem.value == index) selectedIcons[index] else unselectedIcons[index],
                        contentDescription = item
                    )
                },
                label = { Text(item) },
                selected = selectedItem.value == index,
                onClick = {
                    onItemSelected(index)
                }
            )
        }
    }
}
