package net.runner.rshot

import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONArray
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureImageScreen(viewModel: DataLoaderViewModel,onDismiss: () -> Unit) {
    val context = LocalContext.current
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var imageCaptured by rememberSaveable { mutableStateOf(false) }

    var imageName by rememberSaveable { mutableStateOf("") }
    var imageSubject by rememberSaveable { mutableStateOf("") }

    var expanded by rememberSaveable { mutableStateOf(false) }
    val options = listOf("Manufacturing", "Fluid", "Numerical","Drawing","Oec","Data Science")



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
        contract = ActivityResultContracts.PickVisualMedia(),
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
//                Text("Set Image Name", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = imageName,
                    onValueChange = { imageName = it },
                    label = { Text("Image Name") },
                    modifier = Modifier.fillMaxWidth()
                )
//
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = {
                        expanded = !expanded
                    }
                ) {
                    OutlinedTextField(
                        value = imageSubject,
                        onValueChange = { imageSubject = it },
                        label = { Text("Course") },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        readOnly = true
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        options.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    imageSubject = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if(!imageCaptured){
                    Row {

                        IconButton(
                            onClick = {
                                takePictureLauncher.launch(uri)
                                imageUri = uri
                            },
                            modifier = Modifier.padding(horizontal = 10.dp)
                            ){
                            Icon(painter = painterResource(id = R.drawable.capture),
                                contentDescription = "imageCapture",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = {  pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            modifier = Modifier.size(50.dp)
                            ) {
                            Icon(painter = painterResource(id = R.drawable.gallery),
                                contentDescription = "Pick from Gallery",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }
                }
                else{
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
                    Button(onClick = onDismiss,
                        modifier = Modifier.padding(start = 15.dp)
                    ) {
                        Text("Cancel", fontSize = 15.sp)
                    }
                    Button(onClick = {
                        onDismiss()
                        imageUri?.let { uri ->
                            uploadImageToFirebaseStorage(uri, onUploadSuccess = { downloadUrl ->
                                saveImageUrlToFirestore(imageName,imageSubject,viewModel,downloadUrl, onSuccess = {
                                    imageUri=null
                                    Toast.makeText(
                                        context,
                                        "Image Saved Successfully!!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    viewModel.addData(DataClass(imageName,downloadUrl,Instant.now().toString(),imageSubject))
                                    imageCaptured=false
                                }, onError = { exception ->
                                    onDismiss()
                                })
                            }, onError = { exception ->
                                onDismiss()
                            })
                        }
                    },
                        modifier = Modifier.padding(end = 15.dp)
                    ) {
                        Text("Save", fontSize = 15.sp)
                    }
                }
            }
        }
    }


}


fun uploadImageToFirebaseStorage(uri: Uri, onUploadSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
    val storageReference = FirebaseStorage.getInstance().reference
    val fileReference = storageReference.child("images/${System.currentTimeMillis()}.jpg")

    fileReference.putFile(uri)
        .addOnSuccessListener { taskSnapshot ->
            fileReference.downloadUrl.addOnSuccessListener { downloadUri ->
                onUploadSuccess(downloadUri.toString())
            }
        }
        .addOnFailureListener { exception ->
            onError(exception)
        }
}
fun saveImageUrlToFirestore(imageName: String,imageSubject: String, viewModel: DataLoaderViewModel,downloadUrl: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val data = hashMapOf(
        "imageName" to imageName,
        "imageSubject" to imageSubject,
        "imageTime"  to Instant.now().toString(),
        "imageUrl" to downloadUrl
    )


    firestore.collection("users").document(userId!!).collection("data").add(data)
        .addOnSuccessListener {
            val newData = DataClass(imageName, downloadUrl, Instant.now().toString(), imageSubject)
            viewModel.addData(newData)
            onSuccess() }
        .addOnFailureListener { exception -> onError(exception) }
}
fun addJsonToFirebase(data: JSONArray,onSuccess: () -> Unit, onError: (Exception) -> Unit){
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val dine = hashMapOf(
        "dine" to data.toString()
    )
    firestore.collection("users").document(userId!!).collection("DineJson").add(dine)
        .addOnSuccessListener {
            onSuccess() }
        .addOnFailureListener { exception -> onError(exception) }
}
fun saveImageUrlToFirestoreDine(downloadUrl: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val data = hashMapOf(
        "imageUrl" to downloadUrl
    )


    firestore.collection("users").document(userId!!).collection("Dine").add(data)
        .addOnSuccessListener {
            onSuccess() }
        .addOnFailureListener { exception -> onError(exception) }
}
fun uploadImageToFirebaseStorageDine(uri: Uri, onUploadSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
    val storageReference = FirebaseStorage.getInstance().reference
    val fileReference = storageReference.child("Dine/${System.currentTimeMillis()}.jpg")

    fileReference.putFile(uri)
        .addOnSuccessListener { taskSnapshot ->
            fileReference.downloadUrl.addOnSuccessListener { downloadUri ->
                onUploadSuccess(downloadUri.toString())
            }
        }
        .addOnFailureListener { exception ->
            onError(exception)
        }
}

fun jsonExists(onResult: (String) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    if (userId == null) {
        onResult("null")
        return
    }

    db.collection("users").document(userId).collection("DineJson")
        .get()
        .addOnSuccessListener { result ->
            if (result.isEmpty) {
                onResult("null")
            } else {
                val document = result.documents.first()
                onResult(document.data.toString())
            }
        }
        .addOnFailureListener { exception ->
            Log.w("TAG", "Error getting documents.", exception)
            onResult("null")
        }
}

fun deletecacheDine(){
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var uri =""
    firestore.collection("users").document(userId!!).collection("Dine")
        .get()
        .addOnSuccessListener { result ->
            for (document in result) {
                Log.d("data5656",document.data["imageUrl"].toString())
                uri= document.data["imageUrl"].toString()
                val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(uri)

                storageReference.delete()
                    .addOnSuccessListener {
                        firestore.collection("users").document(userId).collection("Dine").document(document.id).delete()
                            .addOnSuccessListener {
                            }
                            .addOnFailureListener { exception ->
                            }
                    }
                    .addOnFailureListener { exception ->
                    }
            }
        }
        .addOnFailureListener { exception ->
        }
    firestore.collection("users").document(userId!!).collection("DineJson")
        .get()
        .addOnSuccessListener { result ->
            for (document in result) {
                firestore.collection("users").document(userId).collection("DineJson").document(document.id).delete()
                    .addOnSuccessListener {
                    }
                    .addOnFailureListener { exception ->
                    }
            }
        }
        .addOnFailureListener { exception ->
        }


}
fun formatUploadTime(uploadTimeString: String): String {
    val uploadInstant = Instant.parse(uploadTimeString)
    val uploadDate = uploadInstant.atZone(ZoneId.systemDefault()).toLocalDate()
    val today = LocalDate.now()

    return when {
        uploadDate.isEqual(today.minus(1, ChronoUnit.DAYS)) -> "Yesterday"
        uploadDate.isEqual(today) -> "Today"
        else -> uploadDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    }
}
fun deleteDataFromFirestore(imageUrl: String, viewModel: DataLoaderViewModel, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    Log.d("data",imageUrl)
    firestore.collection("users").document(userId!!).collection("data")
        .whereEqualTo("imageUrl", imageUrl)
        .get()
        .addOnSuccessListener { result ->
            for (document in result) {
                firestore.collection("users").document(userId).collection("data").document(document.id).delete()
                    .addOnSuccessListener {
                        viewModel.removeDataByImageUrl(imageUrl)
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        onError(exception)
                    }
            }
        }
        .addOnFailureListener { exception ->
            onError(exception)
        }
}
fun deleteImageFromFirebaseStorage(imageUrl: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
    val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)

    storageReference.delete()
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { exception ->
            onError(exception)
        }
}
