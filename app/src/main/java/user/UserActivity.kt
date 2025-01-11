package user

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.compose.material3.Button
import coil3.compose.AsyncImage
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class UserActivity : ComponentActivity() {
    private val userViewModel : UserViewModel by viewModels();

    private val captureUri by lazy {
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        userViewModel.fetchUser()
        setContent {
            UserProfile(userViewModel = userViewModel, captureUri = captureUri)
        }
    }
}

@Composable
fun UserProfile(userViewModel : UserViewModel, captureUri: Uri?) {
    var uri: Uri? by remember { mutableStateOf(null) }
    val context = LocalContext.current
    val user by userViewModel.user.collectAsState()

    val takePicture = rememberLauncherForActivityResult(TakePicture()) { success ->
        if (success && captureUri != null) {
            uri = captureUri
            userViewModel.editAvatar(captureUri.toRequestBody(context))
        }
    }

    val pickPhoto = rememberLauncherForActivityResult(PickVisualMedia()) { uriResult ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uriResult != null) {
            uri = uriResult
            userViewModel.editAvatar(uriResult.toRequestBody(context))
        }
    }

    val requestPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
        if (result) {
            Log.d("Permissions", "Permission granted")
            pickPhoto.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        } else {
            Log.e("Permissions", "Permission denied")
        }
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
                    takePicture.launch(captureUri)
                }
            },
            content = { Text("Take picture") }
        )
        Button(
            onClick = {
                // Vérifier la version et demander la permission si nécessaire
                if (Build.VERSION.SDK_INT >= 29) {
                    pickPhoto.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                } else {
                    requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
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

private fun Uri.toRequestBody(context: Context): MultipartBody.Part {
    val fileInputStream = context.contentResolver.openInputStream(this)!!
    val fileBody = fileInputStream.readBytes().toRequestBody()
    return MultipartBody.Part.createFormData(
        name = "avatar",
        filename = "avatar.jpg",
        body = fileBody
    )
}