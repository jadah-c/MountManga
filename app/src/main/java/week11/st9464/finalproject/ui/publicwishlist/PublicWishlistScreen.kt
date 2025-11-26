package week11.st9464.finalproject.ui.publicwishlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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

import week11.st9464.finalproject.viewmodel.MainViewModel


// Created PublicWishlist Screen - Jadah C (sID #991612594)
// Made edits to the Public Screen and display manga info - Mihai Panait (991622264)
@Composable
fun PublicWishlistScreen(vm: MainViewModel) {
    // Load wishlist when screen appears - Mihai Panait (991622264)
    LaunchedEffect(Unit) {
        vm.loadPublicWishlist()
    }

    val wishlistName = vm.publicWishlistName.ifEmpty { "Public Wishlist" }


        
    WishlistScreen(
        title = "Public Wishlist",
        subtitle = wishlistName,
        mangaList = vm.publicWishlist,
        onDelete = { vm.removeFromPublic(it) },
        onEdit = { /* Optional edit functionality */ },
        onHome = { vm.goToHome() }
    )
}


@Composable
fun WishlistCard(
    manga: MangaInfo,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberAsyncImagePainter(manga.imageUrl),
            contentDescription = manga.title,
            modifier = Modifier
                .height(120.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            manga.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2
        )
        Spacer(modifier = Modifier.height(4.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = onEdit, content = { Text("Edit") })
            TextButton(onClick = onDelete, content = { Text("Delete") })
        }
    }
}