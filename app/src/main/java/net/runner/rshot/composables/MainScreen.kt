package net.runner.rshot.composables

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import net.runner.rshot.CaptureImageScreen
import net.runner.rshot.DataClass
import net.runner.rshot.DataLoaderViewModel
import net.runner.rshot.MainViewModel
import net.runner.rshot.R
import net.runner.rshot.formatUploadTime
import net.runner.rshot.ui.theme.imageTint
import net.runner.rshot.ui.theme.imageTintLight
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(dataviewModel: DataLoaderViewModel = viewModel(),navController: NavController){
    val viewModel : MainViewModel= viewModel()
    val dataLoaded by dataviewModel.dataLoaded.observeAsState(false)
    val fetchedData by dataviewModel.fetchedData.observeAsState(emptyList())
    val searchText by viewModel.searchText.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var cameraPermission by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        cameraPermission = isGranted
        if (isGranted) {
            showDialog = true
        } else if (!ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, android.Manifest.permission.CAMERA)) {
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "Camera permission is needed. Go to settings to enable it.",
                    actionLabel = "Settings"
                )
                if (result == SnackbarResult.ActionPerformed) {
                    openAppSettings(context)
                }
            }
        }
    }

    val filteredData = viewModel.filterData(fetchedData, searchText)

    Box (
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ){

        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            topBar = {
                SearchBar(
                    query = searchText,
                    onQueryChange = {query->
                        viewModel.onSearchTextChange(query) },
                    onSearch = { query ->
                        viewModel.onSearchTextChange(query)
                    },
                    active = isSearching,
                    onActiveChange = { active ->
                        viewModel.onToggleSearch(active)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .padding(bottom = 5.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search, // You can use any icon here
                            contentDescription = "Search Icon"
                        )
                    },
                    trailingIcon = {
                        if (isSearching){
                        Icon(
                            imageVector = Icons.Default.Close, // This could be a clear or close icon
                            contentDescription = "Clear Icon",
                            modifier = Modifier.clickable {
                                viewModel.onSearchTextChange("")
                            }
                        )
                            }
                    },
                    tonalElevation = 0.dp,
                    placeholder = { Text(text = "Search...")},
                    colors = SearchBarDefaults.colors(
                        containerColor = if(!isSearching)MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.background// Set your desired background color here
                    ),
                ) {
                    if (isSearching) {
                        ListX(modifier = Modifier.padding(1.dp), filteredData, navController)
                    }
                }
            }
            ,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {

                            requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                    ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(30.dp))
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            if (!dataLoaded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                ListX(modifier = Modifier.padding(innerPadding),filteredData, navController)
            }

            if (showDialog) {
                CaptureImageScreen(
                    onDismiss = {showDialog=false}
                )
            }

        }
    }
}

fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}

@Composable
fun ListX(modifier: Modifier,data: List<DataClass>,navController: NavController){
    Box(modifier = modifier.fillMaxSize()){
        LazyColumn {
            items(data){single->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                            .clickable {
                                val encodedUrl = URLEncoder.encode(single.image, "UTF-8")
                                navController.navigate("image_viewer/$encodedUrl")

                            }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.image),
                        contentDescription = "Image",
                        modifier = Modifier
                            .padding(10.dp)
                            .size(60.dp),
                        tint = if(isSystemInDarkTheme())imageTint else imageTintLight
                    )
                    Column (
                        modifier=Modifier.weight(0.7f)
                    ){
                        Text(text = single.tag, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = single.subject, color = MaterialTheme.colorScheme.secondary)

                    }
                    Text(
                        text = formatUploadTime(single.time),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(end = 10.dp)
                        )
                }
                HorizontalDivider(thickness = 0.2.dp)
            }

        }

    }
}

