package net.runner.rshot

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun CaptureImageScreen(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                Toast.makeText(context, "Image captured!", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val imageFile = remember {
        File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "captured_image_${System.currentTimeMillis()}.jpg")
    }

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
    var imageName by remember { mutableStateOf("") }
    var imageSubject by remember { mutableStateOf("") }
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
                OutlinedTextField(
                    value = imageSubject,
                    onValueChange = { imageSubject = it },
                    label = { Text("Course") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    takePictureLauncher.launch(uri)
                    imageUri = uri
                }) {
                    Text("Capture Image")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(onClick = {
                        onDismiss()
                        uploadImageToFirebaseStorage(uri, onUploadSuccess = { downloadUrl ->
                            saveImageUrlToFirestore(imageName,imageSubject,downloadUrl, onSuccess = {
                                imageUri=null
                            }, onError = { exception ->
                                    onDismiss()
                            })
                        }, onError = { exception ->
                            onDismiss()
                        })
                    }
                    ) {
                        Text("Save")
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
fun saveImageUrlToFirestore(imageName: String,imageSubject: String,downloadUrl: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    Log.d("hype",userId.toString())
    val data = hashMapOf(
        "imageName" to imageName,
        "imageSubject" to imageSubject,
        "imageTime"  to Instant.now().toString(),
        "imageUrl" to downloadUrl
    )

    firestore.collection("users").document(userId!!).collection("data").add(data)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onError(exception) }
}
fun formatUploadTime(uploadTimeString: String): String {
    val uploadInstant = Instant.parse(uploadTimeString)
    val uploadDate = LocalDate.ofInstant(uploadInstant, ZoneId.systemDefault())
    val today = LocalDate.now()

    return when {
        uploadDate.isEqual(today.minus(1, ChronoUnit.DAYS)) -> "Yesterday"
        uploadDate.isEqual(today) -> "Today"
        else -> uploadDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    }
}