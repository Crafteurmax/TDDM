package user

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.compose.material3.Button
import coil3.compose.AsyncImage
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.material.snackbar.Snackbar
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class UserActivity : ComponentActivity() {
    private lateinit var pickPhoto: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var requestPermission: ActivityResultLauncher<String>
    private val userViewModel : UserViewModel by viewModels();

    private val captureUri by lazy {
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        userViewModel.fetchUser()
        setContent {
            val context = LocalContext.current

            val user by userViewModel.user.collectAsState()
            var uri: Uri? by remember { mutableStateOf(user.avatar?.let { Uri.parse(it) }) }


            val takePicture = rememberLauncherForActivityResult(TakePicture()) { success ->
                if (success && captureUri != null) {
                    uri = captureUri
                    userViewModel.editAvatar(captureUri!!.toRequestBody(context))
                }
            }

            pickPhoto = rememberLauncherForActivityResult(PickVisualMedia()) { uriResult ->
                // Callback is invoked after the user selects a media item or closes the
                // photo picker.
                if (uriResult != null) {
                    uri = uriResult
                    userViewModel.editAvatar(uriResult.toRequestBody(context))
                }
            }

            requestPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) {
                    showMessage("Permission granted")
                    pickPhoto.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                } else {
                    showMessage("Permission denied")
                }
            }

            LaunchedEffect(user.avatar) {
                uri = user.avatar?.let { Uri.parse(it) }
            }

            Column {
                AsyncImage(
                    modifier = Modifier.fillMaxHeight(.2f),
                    model = uri,
                    contentDescription = null
                )
                Button(
                    onClick = {
                        if (captureUri != null) {
                            takePicture.launch(captureUri!!)
                        }
                    },
                    content = { Text("Take picture") }
                )
                Button(
                    onClick = {
                        pickPhotoWithPermission()
                    },
                    content = { Text("Pick photo") }
                )
                OutlinedTextField(
                    user.name,
                    onValueChange = { newUserName ->
                        userViewModel.editUserName(newUserName)
                    },
                    label = { Text("Name") },
                )
            }
        }
    }

    private fun pickPhotoWithPermission() {
        val storagePermission = Manifest.permission.READ_EXTERNAL_STORAGE
        val permissionStatus = checkSelfPermission(storagePermission)
        val isAlreadyAccepted = permissionStatus == PackageManager.PERMISSION_GRANTED
        val isExplanationNeeded = shouldShowRequestPermissionRationale(storagePermission)
        when {
            isAlreadyAccepted -> {// lancer l'action souhaitée
                pickPhoto.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
            }
                isExplanationNeeded -> {// afficher une explication
                    showMessage("Permission required")
                    //requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            else -> {// lancer la demande de permission et afficher une explication en cas de refus
                requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun showMessage(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
    }
}

private fun Uri.toRequestBody(context: Context): MultipartBody.Part {
    val fileInputStream = context.contentResolver.openInputStream(this)!!
    val fileBody = fileInputStream.readBytes().toRequestBody()
    return MultipartBody.Part.createFormData(
        name = "avatar",
        filename = "avatar.jpg",
        body = fileBody
    )
}