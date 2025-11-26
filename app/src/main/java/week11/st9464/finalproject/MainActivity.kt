package week11.st9464.finalproject
// Mount Manga Application
// Created By: Jadah C (sID #991612594) & Mihai P (sID #991622264)
// Updated the UiState with the new screens - Mihai Panait (#991622264)
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import week11.st9464.finalproject.ui.globalwishboard.GlobalWishBoardScreen
import week11.st9464.finalproject.ui.globalwishboard.GlobalWishlistContentScreen
import week11.st9464.finalproject.ui.home.HomeScreen
import week11.st9464.finalproject.ui.login.LoginScreen
import week11.st9464.finalproject.ui.manga.MangaDetails
import week11.st9464.finalproject.ui.privatewishlist.PrivateWishlistScreen
import week11.st9464.finalproject.ui.publicwishlist.PublicWishlistScreen
import week11.st9464.finalproject.ui.scan.Scan
import week11.st9464.finalproject.ui.scan.ScanResultScreen
import week11.st9464.finalproject.ui.splash.SplashScreen
import week11.st9464.finalproject.ui.theme.MountMangaTheme
import week11.st9464.finalproject.util.UiState
import week11.st9464.finalproject.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val vm: MainViewModel = viewModel()
            val uiState by vm.uiState.collectAsState()

            when (uiState) {
                UiState.Loading -> Text("Loading...")
                UiState.Splash -> SplashScreen(vm)
                UiState.Login -> LoginScreen(vm)
                UiState.Home -> HomeScreen(vm)
                UiState.Scan -> Scan(vm)
                UiState.ScanResult -> ScanResultScreen(vm)
                UiState.MangaDetails -> MangaDetails(vm)
                UiState.PrivateWishlist -> PrivateWishlistScreen(vm)
                UiState.PublicWishlist -> PublicWishlistScreen(vm)
                UiState.GlobalWishBoard -> GlobalWishBoardScreen(vm)
                UiState.GlobalWishlistContent -> GlobalWishlistContentScreen(vm)
            }
        }
    }
}