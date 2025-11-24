package week11.st9464.finalproject.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import week11.st9464.finalproject.data.MangaRepository
import week11.st9464.finalproject.model.MangaReader
import week11.st9464.finalproject.util.UiState

// Created MainViewModel class - Jadah C (sID #991612594)
class MainViewModel : ViewModel() {
    private val repo by lazy { MangaRepository() }

    private val _uiState = MutableStateFlow<UiState>(UiState.Splash)
    val uiState = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<MangaReader?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError = _loginError.asStateFlow()

    // To avoid firebase crash - Jadah C (sID #991612594)
    fun checkAuth() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            _currentUser.value = MangaReader(uid = user.uid, email = user.email ?: "")
            _uiState.value = UiState.Home
        } else {
            _uiState.value = UiState.Login
        }
    }

    fun goToLogin() { _uiState.value = UiState.Login }

    fun goToHome() { _uiState.value = UiState.Home }

    fun goToSplash() {
        _uiState.value = UiState.Splash
    }

    //fun goToScan() { _uiState.value = UiState.Scan }

    fun goToPrivateWishlist() { _uiState.value = UiState.PrivateWishlist }

    fun goToPublicWishlist() { _uiState.value = UiState.PublicWishlist }

    fun goToGlobalWishBoard() { _uiState.value = UiState.GlobalWishBoard }
    /*
        fun saveToPrivateWishlist(text: String) {
            if (text.isNotBlank()) {
                repo.savePrivate(text)
            }
        }

        fun saveToPublicWishlist(text: String) {
            if (text.isNotBlank()) {
                repo.savePublic(text)
            }
        }
    */
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _loginError.value = null
            repo.signIn(email, password).onSuccess {
                val user = FirebaseAuth.getInstance().currentUser
                _currentUser.value = MangaReader(uid = user?.uid ?: "", email = user?.email ?: "")
                _uiState.value = UiState.Home
            }.onFailure {
                _loginError.value = it.message ?: "Login failed"
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _loginError.value = null

            try {
                val result = repo.signUp(email, password)

                result.onSuccess {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        _currentUser.value = MangaReader(
                            uid = user.uid,
                            email = user.email ?: ""
                        )
                        Log.d("SIGNUP", "Signup successful: ${user.email}")
                        _uiState.value = UiState.Home
                    } else {
                        Log.w("SIGNUP", "User is null after signup")
                        _loginError.value = "Sign up successful but user not ready. Try signing in."
                        _uiState.value = UiState.Login
                    }
                }.onFailure { exception ->
                    Log.e("SIGNUP", "Signup failed", exception)
                    _loginError.value = exception.message ?: "Sign up failed"
                }
            } catch (e: Exception) {
                Log.e("SIGNUP", "Exception during signup", e)
                _loginError.value = e.message ?: "Sign up failed"
            }
        }
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        _currentUser.value = null
        _uiState.value = UiState.Login
    }
}