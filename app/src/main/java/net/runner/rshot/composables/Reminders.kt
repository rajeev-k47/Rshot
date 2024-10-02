package net.runner.rshot.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.runner.rshot.ui.theme.bg

@Composable
fun reminders(){
    Box(modifier = Modifier.fillMaxSize().padding(bottom = 70.dp)){
        cardW()
    }
}

@Composable
fun cardW(){
    val list = (1..10).map { it.toString() }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(180.dp),
        contentPadding = PaddingValues(
            start = 12.dp,
            top = 16.dp,
            end = 12.dp,
            bottom = 16.dp
        ),
        content = {
            items(list.size) { index ->
                Card(
                    modifier = Modifier
                        .padding(10.dp)
                        .aspectRatio(1f)
                    ,
                    colors = CardColors(containerColor =
                        MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        disabledContentColor = Color.Black
                    ),
                    elevation = CardDefaults.cardElevation(10.dp),
                ) {
                    Text(
                        text = list[index],
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp).align(Alignment.CenterHorizontally),
                    )

                    Text(
                        text = list[index],
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(0.dp).align(Alignment.CenterHorizontally),
                    )
                }
            }
        }
    )
}
