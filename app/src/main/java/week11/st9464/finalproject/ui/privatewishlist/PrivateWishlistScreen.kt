package week11.st9464.finalproject.ui.privatewishlist

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import week11.st9464.finalproject.model.WishlistMangaKey
import week11.st9464.finalproject.ui.wishlistui.WishlistScreen
import week11.st9464.finalproject.viewmodel.MainViewModel

// Created PrivateWishlist Screen - Jadah C (sID #991612594)
// Made edits to the Private Screen and display manga info - Mihai Panait (991622264)
@SuppressLint("UnrememberedMutableState")
@Composable
fun PrivateWishlistScreen(vm: MainViewModel) {
    // Load wishlist when screen appears - Mihai Panait (991622264)
    LaunchedEffect(Unit) {
        vm.loadPrivateWishlist()
    }

    val wishlistName = "Private Wishlist"

    // Comment map that updates whenever privateWishlist or wishlistComments change
    val commentMap by derivedStateOf {
        mutableStateMapOf<WishlistMangaKey, String>().apply {
            vm.privateWishlist.forEach { manga ->
                vm.getMangaComment(wishlistName, manga)?.let { comment ->
                    this[WishlistMangaKey(wishlistName, manga)] = comment
                }
            }
        }
    }

    WishlistScreen(
        title = "Private Wishlist",
        subtitle = "Private Wishlist",
        wishlistName = wishlistName,
        mangaList = vm.privateWishlist,
        selectedManga = vm.selectedManga,
        onSelectManga = { key -> vm.toggleMangaSelection(key) },
        onDeleteSelected = {
            vm.selectedManga.forEach { key ->
                vm.privateWishlist.find { it.id == key.manga.id }?.let { manga ->
                    vm.removeFromPrivate(manga)
                    vm.privateWishlist.remove(manga) // triggers recomposition
                }
            }
            vm.selectedManga.clear()
        },
        onEditSelected = { editedComments ->
            editedComments.forEach { (key, comment) ->
                vm.privateWishlist.find { it.id == key.manga.id }?.let { manga ->
                    vm.updatePrivateMangaComment(manga, comment)
                }
            }
        },
        onHome = { vm.goToHome() },
        showEditDelete = true,
        homeButtonText = "Back",
        commentMap = commentMap
    )
}