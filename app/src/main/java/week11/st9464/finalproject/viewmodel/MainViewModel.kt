package week11.st9464.finalproject.viewmodel

import android.R.attr.text
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.BlendMode.Companion.Screen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import week11.st9464.finalproject.data.MangaRepository
import week11.st9464.finalproject.model.MangaInfo
import week11.st9464.finalproject.model.MangaReader
import week11.st9464.finalproject.model.PublicWishlistSummary
import week11.st9464.finalproject.ui.manga.fetchRecommendations
import week11.st9464.finalproject.util.UiState

// Created MainViewModel class - Jadah C (sID #991612594)
// Added a few functions for Scan, ScanResult and MangaDetail screens - Mihai Panait (#991622264)
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

    // Function for scan page navigation
    fun goToScan() { _uiState.value = UiState.Scan }

    // Going to scan screen
    var scannedText by mutableStateOf("")

    fun goToScanResult(text: String) {
        scannedText = text
        _uiState.value = UiState.ScanResult
    }

    fun goToMangaDetails() {
        _uiState.value = UiState.MangaDetails
    }

    fun scanAgain() {
        scannedText = ""
        _uiState.value = UiState.Scan
    }

    // I wanted to incorporate some loading indication - Mihai Panait (991622264)

    var isLoadingRecs by mutableStateOf(false)
    var recommendations by mutableStateOf(listOf<MangaInfo>())




    fun loadRecommendations(title: String, genres: List<String>) {
        viewModelScope.launch {
            isLoadingRecs = true
            recommendations = fetchRecommendations(title, genres)
            isLoadingRecs = false
        }
    }
    fun goToPrivateWishlist() { _uiState.value = UiState.PrivateWishlist }

    fun goToPublicWishlist() { _uiState.value = UiState.PublicWishlist }

    fun goToGlobalWishBoard() { _uiState.value = UiState.GlobalWishBoard }

    // Working on the Private and Public wishlists - Mihai Panait (991622264)
    // Tracks which manga are checked by the user
    var selectedManga = mutableStateListOf<MangaInfo>()
        private set

    fun toggleMangaSelection(manga: MangaInfo) {
        if (selectedManga.contains(manga)) {
            selectedManga.remove(manga)
        } else {
            selectedManga.add(manga)
        }
    }

    var privateWishlist = mutableStateListOf<MangaInfo>()
        private set

    var publicWishlist = mutableStateListOf<MangaInfo>()
        private set

    fun addSelectedToPrivate() {
        val uid = currentUser.value?.uid ?: return

        if (selectedManga.isEmpty()) {
            showWishlistMessage("No manga selected.")
            return
        }

        viewModelScope.launch {
            try {
                selectedManga.forEach { manga ->
                    repo.addToPrivate(uid, manga)
                }
                privateWishlist.addAll(selectedManga) // update state
                showWishlistMessage("Saved to Private Wishlist!")
                selectedManga.clear()
            } catch (e: Exception) {
                showWishlistMessage("Failed to save to private list.")
            }
        }
    }


    var publicWishlistName by mutableStateOf("Default")
    fun addSelectedToPublic() {
        val uid = currentUser.value?.uid ?: return

        if (selectedManga.isEmpty()) {
            showWishlistMessage("No manga selected.")
            return
        }

        viewModelScope.launch {
            try {
                selectedManga.forEach { manga ->
                    repo.addToPublic(uid, manga, publicWishlistName)
                }
                publicWishlist.addAll(selectedManga)
                showWishlistMessage("Saved to Public Wishlist '$publicWishlistName'!")
                selectedManga.clear()
            } catch (e: Exception) {
                showWishlistMessage("Failed to save to public list.")
            }
        }
    }

    // Fetching the wishlists - Mihai Panait (991622264)
    fun loadPrivateWishlist() {
        val uid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            try {
                val list = repo.getPrivateWishlist(uid)
                privateWishlist.clear()
                privateWishlist.addAll(list)
            } catch (e: Exception) {
                showWishlistMessage("Failed to load private wishlist.")
            }
        }
    }

    fun loadPublicWishlist() {
        val uid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            try {
                val list = repo.getPublicWishlist(uid)
                publicWishlist.clear()
                publicWishlist.addAll(list)
            } catch (e: Exception) {
                showWishlistMessage("Failed to load public wishlist.")
            }
        }
    }

    // Global wishboard - Mihai Panait (991622264)
    var globalPublicWishlist = mutableStateListOf<Pair<MangaInfo, String>>()
        private set

    fun loadGlobalWishBoard() {
        viewModelScope.launch {
            try {
                val list = repo.getAllPublicWishlists()
                globalPublicWishlist.clear()
                globalPublicWishlist.addAll(list)
            } catch (e: Exception) {
                showWishlistMessage("Failed to load global wishlist.")
            }
        }
    }

    // Global wishlist summary - Mihai Panait (991622264)
    var globalWishlistSummaries = mutableStateListOf<PublicWishlistSummary>()
        private set

    var selectedGlobalWishlistUid by mutableStateOf<String?>(null)

    fun loadGlobalWishlistSummaries() {
        viewModelScope.launch {
            try {
                val list = repo.getAllPublicWishlistSummaries()
                globalWishlistSummaries.clear()
                globalWishlistSummaries.addAll(list)
            } catch (e: Exception) {
                showWishlistMessage("Failed to load global wishlists.")
            }
        }
    }

    fun selectGlobalWishlist(uid: String) {
        selectedGlobalWishlistUid = if (selectedGlobalWishlistUid == uid) null else uid
    }

    var selectedGlobalWishlist by mutableStateOf<Pair<String, String>?>(null)
    fun selectGlobalWishlist(summaryUid: String, wishlistName: String) {
        selectedGlobalWishlist = if (selectedGlobalWishlist?.first == summaryUid &&
            selectedGlobalWishlist?.second == wishlistName) null
        else summaryUid to wishlistName
    }

    // For viewing selected global wishlist - Mihai Panait (991622264)
    var selectedGlobalWishlistManga = mutableStateListOf<MangaInfo>()
        private set

    fun loadGlobalWishlistContent(uid: String, wishlistName: String) {
        viewModelScope.launch {
            try {
                val list = repo.getPublicWishlistByUidAndName(uid, wishlistName)
                selectedGlobalWishlistManga.clear()
                selectedGlobalWishlistManga.addAll(list)
                _uiState.value = UiState.GlobalWishlistContent // new UI state for viewing
            } catch (e: Exception) {
                showWishlistMessage("Failed to load wishlist content.")
            }
        }
    }

    // Remove from wishlist - Mihai Panait (991622264)
    fun removeFromPrivate(manga: MangaInfo) {
        val uid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            try {
                repo.removeFromPrivate(uid, manga.title)
                privateWishlist.remove(manga)
                showWishlistMessage("Removed from Private Wishlist.")
            } catch (e: Exception) {
                showWishlistMessage("Failed to remove from private list.")
            }
        }
    }

    fun removeFromPublic(manga: MangaInfo) {
        val uid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            try {
                repo.removeFromPublic(uid, manga.title)
                publicWishlist.remove(manga)
                showWishlistMessage("Removed from Public Wishlist.")
            } catch (e: Exception) {
                showWishlistMessage("Failed to remove from public list.")
            }
        }
    }
    // Error messages for wishlists - Mihai Panait (991622264)
    var wishlistMessage by mutableStateOf<String?>(null)

    fun showWishlistMessage(text: String) {
        wishlistMessage = text
        viewModelScope.launch {
            kotlinx.coroutines.delay(2500)
            wishlistMessage = null
        }
    }
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