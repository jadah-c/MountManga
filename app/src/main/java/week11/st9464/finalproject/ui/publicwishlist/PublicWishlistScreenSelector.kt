package week11.st9464.finalproject.ui.publicwishlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import week11.st9464.finalproject.model.PublicWishlistSummary
import week11.st9464.finalproject.viewmodel.MainViewModel

// Currently, the user does not see their public wishlists as separate entities. I made this page
// To give the user the choice of selecting the public wishlist they created that they'd like to view
// - Mihai Panait (991622264)
@Composable
fun PublicWishlistSelectorScreen(vm: MainViewModel) {
    // Load the list of *your* public wishlist summaries
    LaunchedEffect(Unit) {
        vm.loadMyPublicWishlistSummaries()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "My Public Wishlists",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(vm.myPublicWishlistSummaries) { summary ->
                PublicWishlistRow(
                    summary = summary,
                    onClick = {
                        vm.selectPublicWishlist(summary.wishlistName)
                        vm.loadUserPublicWishlist(summary.wishlistName)
                        vm.goToPublicWishlist()
                    }
                )
            }
        }

        Button(
            onClick = { vm.goToHome() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Back to Home")
        }
    }
}

@Composable
fun PublicWishlistRow(
    summary: PublicWishlistSummary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(summary.wishlistName, style = MaterialTheme.typography.titleMedium)
            Text("Manga: ${summary.mangaCount}", style = MaterialTheme.typography.bodyMedium)
        }
        Text("View", style = MaterialTheme.typography.bodyLarge)
    }
}