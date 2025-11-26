package week11.st9464.finalproject.ui.privatewishlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import week11.st9464.finalproject.ui.globalwishboard.WishlistScreen
import week11.st9464.finalproject.ui.publicwishlist.WishlistCard
import week11.st9464.finalproject.viewmodel.MainViewModel

// Created PrivateWishlist Screen - Jadah C (sID #991612594)
// Made edits to the Private Screen and display manga info - Mihai Panait (991622264)
@Composable
fun PrivateWishlistScreen(vm: MainViewModel) {
    // Load wishlist when screen appears - Mihai Panait (991622264)
    LaunchedEffect(Unit) {
        vm.loadPrivateWishlist()
    }

    WishlistScreen(
        title = "Private Wishlist",
        mangaList = vm.privateWishlist,
        onDelete = { vm.removeFromPrivate(it) },
        onEdit = { /* Optional edit functionality */ },
        onHome = { vm.goToHome() }
    )
}

//@Composable
//fun WishlistScreen(
//    title: String,
//    subtitle: String? = null,
//    mangaList: List<MangaInfo>,
//    onDelete: (MangaInfo) -> Unit,
//    onEdit: (MangaInfo) -> Unit,
//    onHome: () -> Unit
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(title, style = MaterialTheme.typography.headlineMedium)
//        Spacer(modifier = Modifier.height(16.dp))
//
//        if (mangaList.isEmpty()) {
//            Text("No manga in this wishlist.", style = MaterialTheme.typography.bodyLarge)
//        } else {
//            LazyVerticalGrid(
//                columns = GridCells.Fixed(3),
//                verticalArrangement = Arrangement.spacedBy(12.dp),
//                horizontalArrangement = Arrangement.spacedBy(12.dp),
//                modifier = Modifier.weight(1f)
//            ) {
//                items(mangaList) { manga ->
//                    WishlistCard(
//                        manga = manga,
//                        onDelete = { onDelete(manga) },
//                        onEdit = { onEdit(manga) }
//                    )
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceEvenly
//        ) {
//            Button(onClick = { /* Optional global edit */ }) { Text("Edit") }
//            Button(onClick = { /* Optional global delete */ }) { Text("Delete") }
//            Button(onClick = onHome) { Text("Home") }
//        }
//    }
//}

