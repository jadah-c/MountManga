package week11.st9464.finalproject.util

// Created UiState class - Jadah C (sID #991612594)
sealed class UiState {
    object Loading : UiState()
    object Splash : UiState()
    object Login : UiState()
    object Home : UiState()
    //object Scan : UiState()
    object PrivateWishlist : UiState()
    object PublicWishlist : UiState()
    object GlobalWishBoard : UiState()
}