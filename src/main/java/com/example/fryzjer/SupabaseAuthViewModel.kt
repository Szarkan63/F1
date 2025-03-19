package com.example.fryzjer

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fryzjer.data.model.User
import com.example.fryzjer.data.model.UserState
import com.example.fryzjer.data.network.SupabaseClient
import com.example.fryzjer.utils.SharedPreferenceHelper
import kotlinx.coroutines.launch
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put


class SupabaseAuthViewModel : ViewModel() {
    private val _userState = mutableStateOf<UserState>(UserState.Loading)
    val userState: State<UserState> = _userState

    private val client = SupabaseClient.auth
    private val supabase = SupabaseClient.supabase

    // Funkcja do walidacji emaila
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        return email.matches(emailRegex.toRegex())
    }

    fun signUp(
        context: Context,
        userEmail: String,
        userPassword: String,
        userFirstName: String,
        userLastName: String,
    ) {
        viewModelScope.launch {
            _userState.value = UserState.Loading

            if (!isValidEmail(userEmail)) {
                _userState.value = UserState.Error("Nieprawidłowy format adresu email.")
                return@launch
            }

            try {
                val result = client.signUpWith(Email) {
                    email = userEmail
                    password = userPassword
                    data = buildJsonObject {
                        put("first_name", userFirstName)
                        put("last_name", userLastName)
                    }
                }

                if (result != null) {
                    _userState.value = UserState.Error("Wystąpił błąd podczas rejestracji.")
                } else {
                    saveToken(context)
                    // Przekazujemy flagę `isRegistration = true`, aby wyzwolić nawigację w MainScreen
                    _userState.value = UserState.Success("Rejestracja zakończona pomyślnie!", isRegistration = true)
                }
            } catch (e: Exception) {
                _userState.value = UserState.Error("Błąd: ${e.message}")
            }
        }
    }

    // Zapis tokena do SharedPreferences
    private fun saveToken(context: Context) {
        viewModelScope.launch {
            try {
                val session = client.currentSessionOrNull()

                if (session?.accessToken != null) {
                    val accessToken = session.accessToken
                    val sharedPref = SharedPreferenceHelper(context)
                    sharedPref.saveStringData("accessToken", accessToken)
                } else {
                    _userState.value = UserState.Error("Błąd: Nie znaleziono tokena dostępu.")
                }
            } catch (e: Exception) {
                _userState.value = UserState.Error("Błąd: ${e.message}")
            }
        }
    }

    fun login(
        context: Context,
        userEmail: String,
        userPassword: String
    ) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                client.signInWith(Email) {
                    email = userEmail
                    password = userPassword
                }
                saveToken(context)
                _userState.value = UserState.Success("Zalogowano pomyślnie!", isRegistration = false)
            } catch (e: Exception) {
                _userState.value = UserState.Error("Błąd: ${e.message}")
            }
        }
    }

    // Wylogowanie
    fun logout(context: Context) {
        val sharedPref = SharedPreferenceHelper(context)
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                // Sprawdź, czy użytkownik jest zalogowany
                val token = getToken(context)
                if (token.isNullOrEmpty()) {
                    _userState.value = UserState.Error("Nie możesz się wylogować, ponieważ nie jesteś zalogowany!")
                    return@launch
                }

                // Wyloguj użytkownika
                client.signOut()
                sharedPref.clearPreferences()
                _userState.value = UserState.Success("Wylogowano pomyślnie!")
            } catch (e: Exception) {
                _userState.value = UserState.Error("Błąd: ${e.message}")
            }
        }
    }

    // Sprawdź, czy użytkownik jest zalogowany
    fun isUserLoggedIn(
        context: Context
    ) {
        viewModelScope.launch {
            try {
                val token = getToken(context)
                if (token.isNullOrEmpty()) {
                    _userState.value = UserState.Error("Użytkownik nie jest zalogowany!")
                } else {
                    client.retrieveUser(token)
                    client.refreshCurrentSession()
                    saveToken(context)
                    _userState.value = UserState.Success("Użytkownik jest już zalogowany!")
                }
            } catch (e: Exception) {
                _userState.value = UserState.Error("Błąd: ${e.message}")
            }
        }
    }

    // Pobierz token z SharedPreferences
    private fun getToken(context: Context): String? {
        val sharedPref = SharedPreferenceHelper(context)
        return sharedPref.getStringData("accessToken")
    }
}







