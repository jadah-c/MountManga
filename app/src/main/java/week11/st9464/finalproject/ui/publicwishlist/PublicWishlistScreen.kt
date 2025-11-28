package week11.st9464.finalproject.ui.publicwishlist


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import week11.st9464.finalproject.model.WishlistMangaKey
import week11.st9464.finalproject.ui.wishlistui.WishlistScreen


import week11.st9464.finalproject.viewmodel.MainViewModel


// Created PublicWishlist Screen - Jadah C (sID #991612594)
// Made edits to the Public Screen and display manga info - Mihai Panait (991622264)
@Composable
fun PublicWishlistScreen(vm: MainViewModel) {
    // Load wishlist when screen appears - Mihai Panait (991622264)
    LaunchedEffect(vm.selectedPublicWishlistName) {
        val wishlistName = vm.selectedPublicWishlistName
        if (wishlistName.isNotEmpty()) vm.loadUserPublicWishlist(wishlistName)
    }

    val wishlistName = vm.selectedPublicWishlistName.ifEmpty { "Public Wishlist" }

    // Prepopulate comments - Mihai Panait (991622264)
    val commentMap by remember {
        derivedStateOf {
            mutableStateMapOf<WishlistMangaKey, String>().apply {
                vm.userPublicWishlist.forEach { manga ->
                    vm.getMangaComment(wishlistName, manga)?.let { comment ->
                        this[WishlistMangaKey(wishlistName, manga)] = comment
                    }
                }
            }
        }
    }



    WishlistScreen(
        title = "Public Wishlist",
        subtitle = wishlistName,
        wishlistName = wishlistName,
        mangaList = vm.userPublicWishlist,
        selectedManga = vm.selectedManga,
        onSelectManga = { key -> vm.toggleMangaSelection(key) },
        onDeleteSelected = {
            val currentWishlistName = wishlistName

            // Only delete keys that belong to the current wishlist
            val keysToDelete = vm.selectedManga.filter { it.wishlistName == currentWishlistName }

            keysToDelete.forEach { key ->
                vm.removeFromPublic(currentWishlistName, key.manga)
            }

            // Remove only deleted keys from selectedManga
            vm.selectedManga.removeAll { it.wishlistName == currentWishlistName }
        },
        onEditSelected = { editedComments ->
            editedComments.forEach { (key, comment) ->
                vm.updatePublicMangaComment(key.wishlistName, key.manga, comment)
            }
        },
        onHome = { vm.goToPublicWishlistSelector() },
        showEditDelete = true,
        homeButtonText = "Back",
        commentMap = commentMap
    )
}