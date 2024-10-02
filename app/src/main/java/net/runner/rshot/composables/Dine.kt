package net.runner.rshot.composables

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.runner.rshot.BuildConfig
import net.runner.rshot.R
import net.runner.rshot.addJsonToFirebase
import net.runner.rshot.deletecacheDine
import net.runner.rshot.saveImageUrlToFirestoreDine
import net.runner.rshot.uploadImageToFirebaseStorageDine
import org.json.JSONArray
import org.json.JSONException
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


val generativeModel = GenerativeModel(
    modelName = "gemini-1.5-flash",
    apiKey = BuildConfig.GENERATIVE_API_KEY
)

@Composable
fun dine(dineData:String) {
    var DineData = rememberSaveable {
        mutableStateOf(dineData)
    }
    var DineDay = rememberSaveable {
        mutableStateOf(0)
    }
    if(DineData.value.isEmpty()){
        return
    }
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 70.dp)
    ) {
        rail(
            DineData.value,
            modifier = Modifier
                .weight(0.15f)
        ){day->
            DineDay.value=day
        }

        DineUi(
            DineData.value,
            modifier = Modifier
                .weight(0.85f)
                .fillMaxWidth(),
            DineDay.value
        )
    }
}

data class Meal(val Day:String,val Lunch: String, val Dinner: String, val BreakFast: String)
@Composable
fun DineUi(data:String,modifier: Modifier,CURRENTDAY:Int){
    val datajson = JSONArray(data)
    val Dinemeal = mutableListOf<Meal>()
    var DailyLunch =""
    var DailyDinner =""
    var DailyBreakFast =""
    for(i in 0 until datajson.length()){
        val Dineobject =  datajson.getJSONObject(i)
        val day = Dineobject.getString("Day")
        val Lunch = Dineobject.getString("Lunch")
        val BreakFast = Dineobject.getString("Breakfast")
        val Dinner= Dineobject.getString("Dinner")

        if(day=="Daily"){
            DailyLunch=Dineobject.getString("Lunch")
            DailyDinner=Dineobject.getString("Dinner")
            DailyBreakFast=Dineobject.getString("Breakfast")
        }

        Dinemeal.add(Meal(day,Lunch,Dinner,BreakFast))
    }


    Column (
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        LazyColumn {
                item{
                    Spacer(modifier = Modifier.height(30.dp))
                    DineElement( text = "BreakFast", Dinemeal[CURRENTDAY].BreakFast,DailyBreakFast)
                    Spacer(modifier = Modifier.height(30.dp))
                    DineElement( text = "Lunch",Dinemeal[CURRENTDAY].Lunch,DailyLunch)
                    Spacer(modifier = Modifier.height(30.dp))
                    DineElement( text = "Dinner",Dinemeal[CURRENTDAY].Dinner,DailyDinner)
                    Spacer(modifier = Modifier.height(30.dp))

                }
        }
    }

}

@Composable
fun DineElement(text:String,data: String,Daily:String){
    Text(
        text = text,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp) ,
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Text(
            data,
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Bold,
            color =MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 2.dp) ,
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Text(
            "Daily : \n$Daily",
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Bold,
            color =MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun rail(data: String,modifier: Modifier,updateSelected:(Int)->Unit){
    var selectedItem by rememberSaveable { mutableIntStateOf(-1) }
    val todayDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    val datajson = JSONArray(data)
    val items = mutableListOf<Pair<String, String>>()
    for (i in 0 until datajson.length()) {
            val item = datajson.getJSONObject(i)

            val dateStr = item.optString("Date", "")
            if (dateStr.isNotEmpty()) {
                try {
                    val date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(dateStr)
                    val itemDate = SimpleDateFormat("dd", Locale.getDefault()).format(date).toInt()
                    val itemDay = SimpleDateFormat("EEEE", Locale.getDefault()).format(date)

                    items.add(Pair(itemDate.toString(), itemDay))

                    if (selectedItem == -1 && itemDate == todayDay) {
                        selectedItem = i
                        updateSelected(selectedItem)
                    }
                } catch (e: ParseException) {
                    println("Failed to parse date: ${e.message}")
                }
            }
        }
    LazyColumn {
        item {

            NavigationRail(modifier=modifier) {
                items.forEachIndexed { index, (date, day) ->
                    NavigationRailItem(
                        icon = {
                            Text(
                                text = date,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        label = {
                            Text(
                                text = day,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index
                            updateSelected(selectedItem)
                        }
                    )
                }

            }
        }
    }



}

@Composable
fun getImage(setdata:(String)->Unit,onDismiss: () -> Unit){
    val context = LocalContext.current
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var imageCaptured by rememberSaveable { mutableStateOf(false) }


    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                Toast.makeText(context, "Image captured!", Toast.LENGTH_SHORT).show()
                imageCaptured=true
            }
        }
    )

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            imageUri = uri
            if (uri != null) {
                Toast.makeText(context, "Image selected!", Toast.LENGTH_SHORT).show()
                imageCaptured = true
            }
        }
    )
    val imageFile = rememberSaveable {
        File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "captured_image_${System.currentTimeMillis()}.jpg")
    }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
    var extractedText by rememberSaveable { mutableStateOf("") }



    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!imageCaptured) {
                    Row {

                        IconButton(
                            onClick = {
                                takePictureLauncher.launch(uri)
                                imageUri = uri
                            },
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.capture),
                                contentDescription = "imageCapture",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = {
                                pickImageLauncher.launch("image/*"
                                )
                            },
                            modifier = Modifier.size(50.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.gallery),
                                contentDescription = "Pick from Gallery",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.checked),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.padding(start = 15.dp)
                    ) {
                        Text("Cancel", fontSize = 15.sp)
                    }
                    Button(onClick = {
                        onDismiss()

                        imageUri?.let { uri ->
                            deletecacheDine()

                            uploadImageToFirebaseStorageDine(uri, onUploadSuccess = { downloadUrl ->
                                saveImageUrlToFirestoreDine(downloadUrl, onSuccess = {
                                    imageUri=null
                                    Toast.makeText(
                                        context,
                                        "Dine Updated Successfully!!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    imageCaptured=false
                                    loadDineDataFromDatabase(context)
                                }, onError = { exception ->
                                    onDismiss()
                                })
                            }, onError = { exception ->
                                onDismiss()
                            })
                        }
                    }

                    ) {
                        Text("Upload", fontSize = 15.sp)
                    }
                }
            }
        }

    }
}

fun loadDineDataFromDatabase(context: Context) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    db.collection("users").document(userId!!).collection("Dine")
        .get()
        .addOnSuccessListener { result ->
            for (document in result) {
                val data = document.data
                val imageUrl = data["imageUrl"] as? String ?: ""

                if (imageUrl.isNotEmpty()) {
                    Glide.with(context)
                        .asBitmap()
                        .load(imageUrl)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                // Successfully loaded the bitmap
                                Log.d("Bitmap Loaded", "Successfully loaded bitmap")

                                CoroutineScope(Dispatchers.IO).launch {
                                    val extractedText = extractTextFromImage(resource)
                                        .replace("```", "")
                                        .replace("json", "")
//                                        .replace("\\n","")
                                        .trim()

                                    Log.d("Extracted Text", extractedText)
                                    try {
                                        val json = JSONArray(extractedText)
                                        addJsonToFirebase(json, onSuccess = {
                                            Toast.makeText(context,"Dine success !!",Toast.LENGTH_SHORT).show()
                                        }, onError = {
                                            Toast.makeText(context,"Dine Failed Upload Again !!",Toast.LENGTH_SHORT).show()

                                        })
                                    }catch (e:JSONException){
                                        Log.d("json","Jsonerror")
                                    }
                                }
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                            }

                            override fun onLoadFailed(errorDrawable: Drawable?) {
                                Log.e("Bitmap Error", "Failed to load bitmap from URL: $imageUrl")
                            }
                        })
                } else {
                    Log.e("Image Error", "Image URL is empty for document: ${document.id}")
                }
            }
        }
        .addOnFailureListener { exception ->
            Log.w("TAG", "Error getting Dine", exception)
        }
}

suspend fun extractTextFromImage(imageUri: Bitmap): String {

    val inputContent = content {
        image(imageUri)
        text("convert all data of week to a json array and return lunch ,dinner and breakfast with proper comma separated meal")
    }

    val response = generativeModel.generateContent(inputContent)
    return response.text?.trimIndent() ?: "No response text"
}