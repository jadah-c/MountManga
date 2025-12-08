package week11.st9464.finalproject.ui.privatewishlist

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
import androidx.compose.ui.text.font.FontWeight
import week11.st9464.finalproject.model.WishlistMangaKey
import week11.st9464.finalproject.ui.theme.BurntOrange
import week11.st9464.finalproject.ui.theme.Lavender
import week11.st9464.finalproject.ui.theme.Slate
import week11.st9464.finalproject.ui.theme.parisFontFamily
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

    // Populate comments - Mihai Panait (991622264)
    val commentMap by derivedStateOf {
        mutableStateMapOf<WishlistMangaKey, String>().apply {
            vm.privateWishlist.forEach { manga ->
                this[WishlistMangaKey(wishlistName, manga)] =
                    vm.getMangaComment(wishlistName, manga).orEmpty()
            }
        }
    }

    var editingMangaKey by remember { mutableStateOf<WishlistMangaKey?>(null) }

    WishlistScreen(
        title = "Private Wishlist",
        wishlistName = wishlistName,
        mangaList = vm.privateWishlist,
        selectedManga = vm.selectedManga,
        onSelectManga = { key ->
            vm.selectedManga.clear()
            vm.selectedManga.add(key)
        },
        onDeleteSelected = {
            vm.selectedManga.forEach { key ->
                vm.privateWishlist.find { it.id == key.manga.id }?.let { manga ->
                    vm.removeFromPrivate(manga)
                    vm.privateWishlist.remove(manga)
                    vm.removeLocalComment(wishlistName, manga)
                }
            }
            vm.selectedManga.clear()
        },
        onEditSelected = {
            if (vm.selectedManga.size == 1) editingMangaKey = vm.selectedManga.first()
        },
        onHome = { vm.goToHome() },
        showEditDelete = true,
        homeButtonText = "Back",
        commentMap = commentMap,
    )

    // Edit dialog - Mihai Panait (991622264)
    editingMangaKey?.let { key ->
        var comment by remember { mutableStateOf(commentMap[key].orEmpty()) }

        AlertDialog(
            onDismissRequest = { editingMangaKey = null },
            title = { Text("Edit Comment", color = Slate, fontWeight = FontWeight.Bold) },
            text = { // Added Slate color to Comment text field - Jadah C (sID #991612594)
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
                // Row to hold both Save and Clear - Mihai Panait (991622264)
                androidx.compose.foundation.layout.Row {
                    TextButton(
                        onClick = {
                            // Save comment to Firebase - Mihai Panait (991622264)
                            vm.updatePrivateMangaComment(key.manga, comment)
                            // Save locally - Mihai Panait (991622264)
                            vm.setLocalComment(wishlistName, key.manga, comment)
                            editingMangaKey = null
                        }, // Added Slate color to Save button - Jadah C (sID #991612594)
                        colors = ButtonDefaults.textButtonColors(contentColor = Slate)
                    )
                    {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = {
                            // Clear comment on Firebase - Mihai Panait (991622264)
                            vm.updatePrivateMangaComment(key.manga, "")
                            // Remove locally - Mihai Panait (991622264)
                            vm.removeLocalComment(wishlistName, key.manga)
                            editingMangaKey = null
                        }, // Added Lavender color to Clear button - Jadah C (sID #991612594)
                        colors = ButtonDefaults.textButtonColors(contentColor = Lavender)
                    )
                    {
                        Text("Clear", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { editingMangaKey = null },
                    // Added BurntOrange color to Cancel button - Jadah C (sID #991612594)
                    colors = ButtonDefaults.textButtonColors(contentColor = BurntOrange)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}