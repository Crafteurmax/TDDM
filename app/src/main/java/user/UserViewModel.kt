package user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.Api.userWebService
import data.Commands
import data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.util.UUID

class UserViewModel : ViewModel(){
    private val webService = userWebService

    var user = MutableStateFlow<User>(
        value = User(
            name = "",
            email = "",
            avatar = "",
        )
    )

    fun fetchUser() {
        viewModelScope.launch {
            val response = webService.fetchUser()
            if (!response.isSuccessful) {
                Log.e("Network", "Error: ${response.message()}")
                return@launch
            }

            user.value = response.body()!!
        }
    }

    fun editUserName(newUsername : String) {

        println(newUsername)

        viewModelScope.launch {
            val command = Commands(
                type = "user_update",
                uuid = UUID.randomUUID().toString(),
                args = mapOf("name" to newUsername)
            )

            val response = webService.update(listOf(command))
            if (!response.isSuccessful) {
                Log.e("Network", "Error: ${response.message()}")
                return@launch
            }

            //user.value = user.value.copy(name = newUsername)
        }
    }

    fun editAvatar(avatar: MultipartBody.Part) {
        viewModelScope.launch {
            val response = userWebService.updateAvatar(avatar) // Call HTTP (opération longue)
            if (!response.isSuccessful) { // à cette ligne, on a reçu la réponse de l'API
                Log.e("Network", "Error: ${response.message()}")
                return@launch
            }
        }
    }
}