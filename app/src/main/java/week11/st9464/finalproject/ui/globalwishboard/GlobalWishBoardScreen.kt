package week11.st9464.finalproject.ui.globalwishboard

import android.R
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import week11.st9464.finalproject.model.MangaInfo
import week11.st9464.finalproject.model.PublicWishlistSummary
import week11.st9464.finalproject.viewmodel.MainViewModel
import androidx.compose.runtime.collectAsState
import week11.st9464.finalproject.ui.publicwishlist.WishlistCard


// Created GlobalWishBoard Screen - Jadah C (sID #991612594)

@Composable
fun GlobalWishBoardScreen(vm: MainViewModel) {
    // Load summaries when screen is first displayed - Mihai Panait (991622264)
    LaunchedEffect(Unit) {
        vm.loadGlobalWishlistSummaries()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "Global Wishlist Board",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Users", modifier = Modifier.weight(2f))
            Text("Number of Manga", modifier = Modifier.weight(1f))
            Text("Wishlist Name", modifier = Modifier.weight(2f))
            Text("Select", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Scrollable list of wishlist summaries - Mihai Panait (991622264)
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(vm.globalWishlistSummaries) { summary ->
                WishlistSummaryRow(
                    summary = summary,
                    isSelected = vm.selectedGlobalWishlist?.first == summary.uid &&
                            vm.selectedGlobalWishlist?.second == summary.wishlistName,
                    onSelect = {
                        vm.selectGlobalWishlist(summary.uid, summary.wishlistName)
                    },
                    currentUserUid = vm.currentUser.collectAsState().value?.uid
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                vm.selectedGlobalWishlist?.let { (uid, wishlistName) ->
                    vm.loadGlobalWishlistContent(uid, wishlistName)
                }
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp)
        ) {
            Text("View Selected Wishlist")
        }

        Button(onClick = { vm.goToHome() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Back to Home")
        }
    }
}

@Composable
fun WishlistSummaryRow(
    summary: PublicWishlistSummary,
    isSelected: Boolean,
    onSelect: () -> Unit,
    currentUserUid: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (summary.uid == currentUserUid)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else
                    MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(vertical = 8.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(summary.email, modifier = Modifier.weight(2f))
        Text(summary.mangaCount.toString(), modifier = Modifier.weight(1f))
        Text(summary.wishlistName, modifier = Modifier.weight(2f))
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onSelect() },
            modifier = Modifier.weight(1f)
        )
    }
}

//Wishlist content - Mihai Panait (991622264)
@Composable
fun GlobalWishlistContentScreen(vm: MainViewModel) {
    val currentUserUid = vm.currentUser.collectAsState().value?.uid
    val (ownerUid, wishlistName) = vm.selectedGlobalWishlist ?: "" to ""

    val isOwnWishlist = ownerUid == currentUserUid

    WishlistScreen(
        title = wishlistName.ifEmpty { "Wishlist" },
        mangaList = vm.selectedGlobalWishlistManga,
        onDelete = { /* only for own */ },
        onEdit = { /* only for own */ },
        onHome = { vm.goToGlobalWishBoard() },
        showEditDelete = isOwnWishlist,
        homeButtonText = if (isOwnWishlist) "Home" else "Back to Global Wishboard"
    )
}

// Function that I also use in PrivateWishlistScreen and PublicWishlistScreen - Mihai Panait (991622264)
@Composable
fun WishlistScreen(
    title: String,
    subtitle: String? = null,
    mangaList: List<MangaInfo>,
    onDelete: (MangaInfo) -> Unit,
    onEdit: (MangaInfo) -> Unit,
    onHome: () -> Unit,
    showEditDelete: Boolean = true,
    homeButtonText: String = "Home"
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            title,
            style = MaterialTheme.typography.headlineLarge
        )

        if (!subtitle.isNullOrEmpty()) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (mangaList.isEmpty()) {
            Text(
                "No manga in this wishlist.",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(mangaList) { manga ->
                    WishlistCard(
                        manga = manga,
                        onDelete = { if (showEditDelete) onDelete(manga) },
                        onEdit = { if (showEditDelete) onEdit(manga) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (showEditDelete) {
                Button(onClick = { /* optional */ }) { Text("Edit") }
                Button(onClick = { /* optional */ }) { Text("Delete") }
            }
            Button(onClick = onHome) { Text(homeButtonText) }
        }
    }
}