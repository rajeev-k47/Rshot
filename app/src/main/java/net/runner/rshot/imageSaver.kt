package net.runner.rshot

import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.io.File

fun imageSaver(){
    val storage = Firebase.storage

}
@Composable
fun CameraView(
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().build() }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { context ->
            val previewView = PreviewView(context)
            val preview = Preview.Builder().build()
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                preview.setSurfaceProvider(previewView.surfaceProvider)

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, preview, imageCapture
                    )
                } catch (exc: Exception) {
                    onError(ImageCaptureException(ImageCapture.ERROR_UNKNOWN, "Failed to bind camera use cases", exc))
                }
            }, ContextCompat.getMainExecutor(context))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Button(onClick = {
            val photoFile = File(context.filesDir, "photo.jpg")
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
            imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        onImageCaptured(Uri.fromFile(photoFile))
                    }
                    override fun onError(exc: ImageCaptureException) {
                        onError(exc)
                    }
                })
        },
        ) {
            Text("Capture")
        }
    }
}


@Composable
fun CaptureImageScreen() {
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

    var imageFile = remember {
        File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "captured_image.jpg")
    }

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            takePictureLauncher.launch(uri)
            imageUri = uri
        }) {
            Text("Capture Image")
        }
        Button(onClick = {
                uploadImageToFirebaseStorage(uri, onUploadSuccess = { downloadUrl ->
                    saveImageUrlToFirestore(downloadUrl, onSuccess = {
                    }, onError = { exception ->

                    })
                }, onError = { exception ->
                })
            }) {
                Text("Upload Image")
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
fun saveImageUrlToFirestore(downloadUrl: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val data = hashMapOf("imageUrl" to downloadUrl)

    firestore.collection("images").add(data)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onError(exception) }
}

@Composable
fun showPopup(onImageCaptured: (String) -> Unit){
    val onDismiss = {  }
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
                    onImageCaptured(imageName)
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
                    Button(onClick = onDismiss) {
                        Text("Save")
                    }
                }
            }
        }
    }
}