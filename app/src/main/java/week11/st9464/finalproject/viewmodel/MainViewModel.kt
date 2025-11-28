package week11.st9464.finalproject.viewmodel

import android.R.attr.text
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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
import week11.st9464.finalproject.model.WishlistMangaKey
import week11.st9464.finalproject.ui.manga.fetchRecommendations
import week11.st9464.finalproject.util.UiState

// Created MainViewModel class - Jadah C (sID #991612594)
// Added a few functions for Scan, ScanResult and MangaDetail screens - Mihai Panait (#991622264)
class MainViewModel : ViewModel() {
    private val repo by lazy { MangaRepository() }

    var currentUserEmail by mutableStateOf("")
    var currentUserUid by mutableStateOf("")

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

    fun goToLogin() {
        _uiState.value = UiState.Login
    }

    fun goToHome() {
        _uiState.value = UiState.Home
    }

    fun goToSplash() {
        _uiState.value = UiState.Splash
    }

    // Function for scan page navigation
    fun goToScan() {
        _uiState.value = UiState.Scan
    }

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


    // Manga / Wishlist functions start - Mihai Panait (991622264)
    fun loadRecommendations(title: String, genres: List<String>) {
        viewModelScope.launch {
            isLoadingRecs = true
            recommendations = fetchRecommendations(title, genres)
            isLoadingRecs = false
        }
    }

    fun goToPrivateWishlist() {
        _uiState.value = UiState.PrivateWishlist
    }

    fun goToPublicWishlist() {
        _uiState.value = UiState.PublicWishlist
    }

    fun goToGlobalWishBoard() {
        _uiState.value = UiState.GlobalWishBoard
    }

    // Adding comments - Mihai Panait (991622264)
    val wishlistComments = mutableStateMapOf<WishlistMangaKey, String>()


    fun setMangaComment(wishlistName: String, manga: MangaInfo, comment: String) {
        wishlistComments[WishlistMangaKey(wishlistName, manga)] = comment
    }

    fun getMangaComment(wishlistName: String, manga: MangaInfo): String? {
        return wishlistComments[WishlistMangaKey(wishlistName, manga)]
    }

    // Update comment for private wishlist - Mihai Panait (991622264)
    fun updatePrivateMangaComment(manga: MangaInfo, comment: String) {
        val uid = currentUser.value?.uid ?: return
        val wishlistName = "Private"
        viewModelScope.launch {
            try {
                repo.updatePrivateMangaComment(uid, manga.id, comment)
                val index = privateWishlist.indexOfFirst { it.id == manga.id }
                if (index != -1) privateWishlist[index].comment = comment
                setMangaComment(wishlistName, manga, comment) // <-- pass manga, not manga.id
                showWishlistMessage("Comment saved!")
            } catch (e: Exception) {
                showWishlistMessage("Failed to save comment.")
            }
        }
    }

    // Update comment for public wishlist - Mihai Panait (991622264)
    fun updatePublicMangaComment(wishlistName: String, manga: MangaInfo, comment: String) {
        val uid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            try {
                repo.updatePublicMangaComment(uid, wishlistName, manga.id, comment)
                val index = userPublicWishlist.indexOfFirst { it.id == manga.id }
                if (index != -1) userPublicWishlist[index].comment = comment
                setMangaComment(wishlistName, manga, comment) // <-- pass manga, not mangaId
                showWishlistMessage("Comment saved!")
            } catch (e: Exception) {
                showWishlistMessage("Failed to save comment.")
            }
        }
    }

    // Working on the Private and Public wishlists - Mihai Panait (991622264)
    // Tracks which manga are checked by the user
    var selectedManga = mutableStateListOf<WishlistMangaKey>()
        private set

    fun toggleMangaSelection(key: WishlistMangaKey) {
        if (selectedManga.contains(key)) {
            selectedManga.remove(key)
        } else {
            selectedManga.add(key)
        }
    }

    var privateWishlist = mutableStateListOf<MangaInfo>()
        private set

    var publicWishlist = mutableStateListOf<MangaInfo>()
        private set

    fun addSelectedToPrivate(selectedMangaInfos: List<MangaInfo>) {
        val uid = currentUser.value?.uid ?: return
        if (selectedMangaInfos.isEmpty()) {
            showWishlistMessage("No manga selected.")
            return
        }

        viewModelScope.launch {
            try {
                selectedMangaInfos.forEach { manga ->
                    repo.addToPrivate(uid, manga)
                    if (!privateWishlist.contains(manga)) privateWishlist.add(manga)
                }
                showWishlistMessage("Saved to Private Wishlist!")
            } catch (e: Exception) {
                showWishlistMessage("Failed to save to private list.")
                Log.e("WISHLIST", "Error adding to private wishlist", e)
            } finally {
                selectedManga.clear()
            }
        }
    }


    var publicWishlistName by mutableStateOf("Default")


    fun addSelectedToPublicFromCurrentWishlist() {
        val uid = currentUser.value?.uid ?: return
        val wishlistName = selectedPublicWishlistName.ifEmpty { publicWishlistName }

        val selectedForCurrent = selectedManga.filter { it.wishlistName == wishlistName }
            .mapNotNull { key ->
                userPublicWishlist.find { it.id == key.manga.id }?.let { it to wishlistName }
            }

        if (selectedForCurrent.isEmpty()) {
            showWishlistMessage("No manga selected for '$wishlistName'.")
            return
        }

        viewModelScope.launch {
            try {
                selectedForCurrent.forEach { (manga, _) ->
                    repo.addToPublic(uid, manga, wishlistName)
                    if (!publicWishlist.contains(manga)) publicWishlist.add(manga)
                }
                showWishlistMessage("Saved to Public Wishlist '$wishlistName'!")
            } catch (e: Exception) {
                showWishlistMessage("Failed to save to public list.")
                Log.e("WISHLIST", "Error adding to public wishlist", e)
            } finally {
                selectedManga.removeAll { it.wishlistName == wishlistName }
            }
        }
    }

    fun removeSelectedFromCurrentPublicWishlist() {
        val uid = currentUser.value?.uid ?: return
        val wishlistName = selectedPublicWishlistName.ifEmpty { publicWishlistName }

        val selectedForCurrent = selectedManga.filter { it.wishlistName == wishlistName }

        if (selectedForCurrent.isEmpty()) {
            showWishlistMessage("No manga selected for deletion from '$wishlistName'.")
            return
        }

        viewModelScope.launch {
            try {
                selectedForCurrent.forEach { key ->
                    repo.removeFromPublic(uid, wishlistName, key.manga.id)
                    userPublicWishlist.removeIf { it.id == key.manga.id }
                }
                showWishlistMessage("Removed from Public Wishlist '$wishlistName'.")
            } catch (e: Exception) {
                showWishlistMessage("Failed to remove from public list.")
                Log.e("WISHLIST", "Error removing from public wishlist", e)
            } finally {
                selectedManga.removeAll { it.wishlistName == wishlistName }
            }
        }
    }

    fun addSelectedToPublic(selectedMangaInfos: List<MangaInfo>) {
        val uid = currentUser.value?.uid ?: return
        if (selectedMangaInfos.isEmpty()) {
            showWishlistMessage("No manga selected.")
            return
        }

        val currentWishlistName = publicWishlistName.ifEmpty { "Public Wishlist" }

        viewModelScope.launch {
            try {
                selectedMangaInfos.forEach { manga ->
                    repo.addToPublic(uid, manga, currentWishlistName)
                    if (!publicWishlist.contains(manga)) publicWishlist.add(manga)
                }
                showWishlistMessage("Saved to Public Wishlist '$currentWishlistName'!")
            } catch (e: Exception) {
                showWishlistMessage("Failed to save to public list.")
                Log.e("WISHLIST", "Error adding to public wishlist", e)
            } finally {
                // Remove only selected keys from the current public wishlist
                selectedManga.removeAll { it.wishlistName == currentWishlistName }
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

    // Public wishlist selector - Mihai Panait (991622264)
    var myPublicWishlistSummaries = mutableStateListOf<PublicWishlistSummary>()
        private set

    fun loadMyPublicWishlistSummaries() {
        val uid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            try {
                val list = repo.getPublicWishlistSummariesByUid(uid)
                myPublicWishlistSummaries.clear()
                myPublicWishlistSummaries.addAll(list)
            } catch (e: Exception) {
                showWishlistMessage("Failed to load your public wishlists.")
            }
        }
    }

    var userPublicWishlist = mutableStateListOf<MangaInfo>()
        private set
    var currentPublicWishlistName by mutableStateOf("")
    var selectedPublicWishlistName by mutableStateOf("")
        private set


    fun loadUserPublicWishlist(wishlistName: String) {
        val uid = _currentUser.value?.uid ?: return

        viewModelScope.launch {
            val list = repo.getPublicWishlistByUidAndName(uid, wishlistName)
            userPublicWishlist.clear()
            userPublicWishlist.addAll(list)
            currentPublicWishlistName = wishlistName
        }
    }

    fun selectPublicWishlist(name: String) {
        selectedPublicWishlistName = name
    }

    fun goToPublicWishlistSelector() {
        _uiState.value = UiState.PublicWishlistSelector
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
            selectedGlobalWishlist?.second == wishlistName
        ) null
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
                repo.removeFromPrivate(uid, manga.id)
                privateWishlist.remove(manga)
                showWishlistMessage("Removed from Private Wishlist.")
            } catch (e: Exception) {
                showWishlistMessage("Failed to remove from private list.")
            }
        }
    }

    fun removeFromPublic(wishlistName: String, manga: MangaInfo) {
        val uid = currentUser.value?.uid ?: return

        viewModelScope.launch {
            try {
                // Remove from Firestore
                repo.removeFromPublic(uid, wishlistName, manga.id)

                // Remove locally from the current user's public wishlist
                userPublicWishlist.removeIf { it.id == manga.id && wishlistName == currentPublicWishlistName }

                showWishlistMessage("Removed '${manga.title}' from Public Wishlist '$wishlistName'.")
            } catch (e: Exception) {
                showWishlistMessage("Failed to remove '${manga.title}' from public list.")
                Log.e("WISHLIST", "Error removing from public wishlist", e)
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

    // Manga / Wishlist functions end  - Mihai Panait (991622264)
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