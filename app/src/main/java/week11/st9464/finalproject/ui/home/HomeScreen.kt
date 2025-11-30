package week11.st9464.finalproject.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import week11.st9464.finalproject.ui.theme.Cream
import week11.st9464.finalproject.ui.theme.Golden
import week11.st9464.finalproject.ui.theme.Lavender
import week11.st9464.finalproject.ui.theme.Slate
import week11.st9464.finalproject.viewmodel.MainViewModel

// Created Home Screen - Jadah C (sID #991612594)
@Composable
fun HomeScreen(vm: MainViewModel) {
    val currentUser by vm.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Home",
            style = MaterialTheme.typography.headlineMedium,
            color = Golden
        )

        Spacer(modifier = Modifier.height(24.dp))

        currentUser?.let { user ->
            Text("Logged in as: ${user.email}", color = Golden)
            Spacer(modifier = Modifier.height(24.dp))

            val buttonModifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(vertical = 6.dp)

            // List of buttons with label and action - Jadah C (sID #991612594)
            val buttons = listOf(
                "Scan" to { vm.goToScan() },
                "My Private Wishlist" to { vm.goToPrivateWishlist() },
                "My Public Wishlist" to { vm.goToPublicWishlistSelector() },
                "Global Wishlist Board" to { vm.goToGlobalWishBoard() },
                "Logout" to { vm.logout() }
            )

            buttons.forEach { (label, action) ->
                Button(
                    onClick = action,
                    modifier = buttonModifier,
                    colors = ButtonDefaults.buttonColors(containerColor = Cream)
                ) {
                    Text(label, color = Slate)
                }
            }
        } ?: run {
            Text("User not available", color = Color.Red)
        }
    }
}