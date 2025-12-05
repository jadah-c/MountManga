package week11.st9464.finalproject.ui.wishlistui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import week11.st9464.finalproject.model.MangaInfo
import week11.st9464.finalproject.model.WishlistMangaKey
import week11.st9464.finalproject.ui.theme.BurntOrange
import week11.st9464.finalproject.ui.theme.Cream
import week11.st9464.finalproject.ui.theme.EarthBrown
import week11.st9464.finalproject.ui.theme.Lavender
import week11.st9464.finalproject.ui.theme.Slate
import week11.st9464.finalproject.ui.theme.parisFontFamily

// Moved the WishlistScreen into it's own file for easier access - Mihai Panait (991622264)
// Function that I also use in PrivateWishlistScreen and PublicWishlistScreen - Mihai Panait (991622264)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    title: String,
    subtitle: String? = null,
    wishlistName: String,
    mangaList: List<MangaInfo>,
    selectedManga: List<WishlistMangaKey>,
    onSelectManga: (WishlistMangaKey) -> Unit,
    onDeleteSelected: () -> Unit,
    onEditSelected: (Map<WishlistMangaKey, String>) -> Unit,
    onHome: () -> Unit,
    showEditDelete: Boolean = true,
    homeButtonText: String = "Home",
    commentMap: MutableMap<WishlistMangaKey, String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Lavender)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CenterAlignedTopAppBar(
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = EarthBrown,
                        fontFamily = parisFontFamily
                    )
                    subtitle?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 4.dp),
                            fontWeight = FontWeight.Bold,
                            color = EarthBrown
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
                navigationIconContentColor = Lavender,
                titleContentColor = Lavender,
                actionIconContentColor = Lavender
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (mangaList.isEmpty()) {
            Text(
                "No manga in this wishlist.",
                style = MaterialTheme.typography.bodyLarge,
                color = EarthBrown
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(mangaList) { manga ->
                    val key = WishlistMangaKey(wishlistName, manga)
                    WishlistCard(
                        manga = manga,
                        isSelected = selectedManga.contains(key),
                        onSelect = { onSelectManga(key) },
                        comment = commentMap[key],
                        onCommentChange = null,
                        isEditing = false
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
                Button(
                    onClick = { onEditSelected(commentMap) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Slate,
                        contentColor = Cream
                    )
                ) {
                    Text("Edit Selected")
                }

                Button(
                    onClick = onDeleteSelected,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BurntOrange,
                        contentColor = Cream
                    )
                ) {
                    Text("Delete Selected")
                }

                Button(
                    onClick = onHome,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EarthBrown,
                        contentColor = Cream
                    )
                ) {
                    Text(homeButtonText)
                }
            }
        }
    }
}