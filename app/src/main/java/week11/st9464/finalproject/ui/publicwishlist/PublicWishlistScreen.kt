package week11.st9464.finalproject.ui.publicwishlist


import android.annotation.SuppressLint
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import week11.st9464.finalproject.model.WishlistMangaKey
import week11.st9464.finalproject.ui.theme.BurntOrange
import week11.st9464.finalproject.ui.theme.Lavender
import week11.st9464.finalproject.ui.theme.Slate
import week11.st9464.finalproject.ui.wishlistui.WishlistScreen


import week11.st9464.finalproject.viewmodel.MainViewModel


// Created PublicWishlist Screen - Jadah C (sID #991612594)
// Made edits to the Public Screen and display manga info - Mihai Panait (991622264)
@SuppressLint("UnrememberedMutableState")
@Composable
fun PublicWishlistScreen(vm: MainViewModel) {
    // Load wishlist when screen appears - Mihai Panait (991622264)
    LaunchedEffect(vm.selectedPublicWishlistName) {
        if (vm.selectedPublicWishlistName.isNotEmpty())
            vm.loadUserPublicWishlist(vm.selectedPublicWishlistName)
    }

    val wishlistName = vm.selectedPublicWishlistName.ifEmpty { "Public Wishlist" }

    // Prepopulate comments - Mihai Panait (991622264)
    val commentMap by derivedStateOf {
        mutableStateMapOf<WishlistMangaKey, String>().apply {
            vm.userPublicWishlist.forEach { manga ->
                this[WishlistMangaKey(wishlistName, manga)] =
                    vm.publicWishlistComments[manga.id].orEmpty()
            }
        }
    }

    var editingMangaKey by remember { mutableStateOf<WishlistMangaKey?>(null) }


    WishlistScreen(
        title = "Public Wishlist",
        subtitle = wishlistName,
        wishlistName = wishlistName,
        mangaList = vm.userPublicWishlist,
        selectedManga = vm.selectedManga,
        onSelectManga = { key ->
            vm.selectedManga.clear()
            vm.selectedManga.add(key)
        },
        onDeleteSelected = {
            val keysToDelete = vm.selectedManga.filter { it.wishlistName == wishlistName }
            keysToDelete.forEach { key ->
                vm.removeFromPublic(wishlistName, key.manga)
                vm.removeLocalCommentPublic(wishlistName, key.manga)
            }
            vm.selectedManga.removeAll { it.wishlistName == wishlistName }
        },
        onEditSelected = {
            if (vm.selectedManga.size == 1) editingMangaKey = vm.selectedManga.first()
        },
        onHome = { vm.goToPublicWishlistSelector() },
        showEditDelete = true,
        homeButtonText = "Back",
        commentMap = commentMap
    )

    // Edit dialog - Mihai Panait (991622264)
    editingMangaKey?.let { key ->
        var comment by remember { mutableStateOf(commentMap[key].orEmpty()) }

        AlertDialog(
            onDismissRequest = { editingMangaKey = null },
            title = { Text("Edit Comment") },
            text = {
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment") },
                    singleLine = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Slate,
                        unfocusedBorderColor = Slate.copy(alpha = 0.4f),
                        cursorColor = Slate
                )
                )
            },
            confirmButton = {
                // Row to hold both Save and Clear buttons - Mihai Panait (991622264)
                androidx.compose.foundation.layout.Row {
                    TextButton(onClick = {
                        // Save comment to Firebase - Mihai Panait (991622264)
                        vm.updatePublicMangaComment(key.wishlistName, key.manga, comment)
                        // Save locally - Mihai Panait (991622264)
                        vm.setLocalCommentPublic(key.wishlistName, key.manga, comment)
                        editingMangaKey = null
                    },
                        colors = ButtonDefaults.textButtonColors(contentColor = Slate)
                    ) {
                        Text("Save")
                    }

                    TextButton(onClick = {
                        // Clear comment on Firebase - Mihai Panait (991622264)
                        vm.updatePublicMangaComment(key.wishlistName, key.manga, "")
                        // Remove locally - Mihai Panait (991622264)
                        vm.removeLocalCommentPublic(key.wishlistName, key.manga)
                        editingMangaKey = null
                    },
                        colors = ButtonDefaults.textButtonColors(contentColor = Lavender)
                    ) {
                        Text("Clear")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { editingMangaKey = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = BurntOrange)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}