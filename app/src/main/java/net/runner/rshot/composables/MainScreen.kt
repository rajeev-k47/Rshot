package net.runner.rshot.composables

import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import net.runner.rshot.DataClass
import net.runner.rshot.MainViewModel
import net.runner.rshot.DataLoaderViewModel
import net.runner.rshot.R
import net.runner.rshot.ui.theme.imageTint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(dataviewModel: DataLoaderViewModel = viewModel()){
    val viewModel = MainViewModel()
    val dataLoaded by dataviewModel.dataLoaded.observeAsState(false)
    val fetchedData by dataviewModel.fetchedData.observeAsState(emptyList())

    val searchText by viewModel.searchText.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    Box (
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ){

        Scaffold(
            topBar = {
                SearchBar(
                    query = searchText,
                    onQueryChange =viewModel::onSearchTextChange ,
                    onSearch = viewModel::onSearchTextChange ,
                    active = isSearching,
                    onActiveChange =  { viewModel.onToogleSearch() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                ) {

                }
            }
            ,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {  },
                    containerColor = MaterialTheme.colorScheme.primary
                    ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
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
                ListX(modifier = Modifier.padding(innerPadding),fetchedData)
            }

        }
    }
}

@Composable
fun ListX(modifier: Modifier,data: List<DataClass>){
    Box(modifier = modifier.fillMaxSize()){
        LazyColumn {
            items(data){single->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.image),
                        contentDescription = "Image",
                        modifier = Modifier
                            .padding(10.dp)
                            .size(60.dp),
                        tint = imageTint
                    )
                    Column {
                        Text(text = single.tag, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = single.image, color = MaterialTheme.colorScheme.secondary)

                    }
                }
                HorizontalDivider(thickness = 0.2.dp)
            }

        }

    }
}

