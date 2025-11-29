package week11.st9464.finalproject.ui.manga

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import week11.st9464.finalproject.model.MangaInfo
import week11.st9464.finalproject.model.WishlistMangaKey
import week11.st9464.finalproject.ui.scan.fetchMangaFromJikan
import week11.st9464.finalproject.viewmodel.MainViewModel
import java.net.URL
import java.net.URLEncoder



// Created the MangaDetails screen - Mihai Panait (#991622264)
// API, ML Kit - Mihai Panait (#991622264)

// Made some updates to the Ui of this. Added a loading effect and improved the recommendations - Mihai Panait (#991622264)
// Added a dropdown menu selector that shows already created public wishlists by the auth user - Mihai Panait (991622264)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaDetails(vm: MainViewModel) {
    val scannedText = vm.scannedText

    var manga by remember { mutableStateOf<MangaInfo?>(null) }
    var fetchState by remember { mutableStateOf("Loading...") }

    // Dropdown state - Mihai Panait (991622264)
    var selectedExistingWishlist by remember { mutableStateOf("") }
    var newWishlistName by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.loadMyPublicWishlistSummaries() }

    LaunchedEffect(scannedText) {
        val result = fetchMangaFromJikan(scannedText)
        if (result != null) {
            manga = result
            fetchState = "Success"

            // Load recommendations with shimmer - Mihai Panait (991622264)
            vm.isLoadingRecs = true
            vm.recommendations = fetchRecommendations(result.title, result.genres)
            vm.isLoadingRecs = false
        } else {
            fetchState = "Failed"
        }
    }

    val existingWishlistNames by remember {
        derivedStateOf { vm.myPublicWishlistSummaries.map { it.wishlistName } }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionHeader("Manga Details")

        // Calling the wishlist message - Mihai Panait (991622264)
        vm.wishlistMessage?.let { WishlistMessageBox(it) }

        if (manga != null) {
            MangaInfoSection(manga!!, vm)
            Spacer(Modifier.height(12.dp))
            RecommendationRow(vm)
        } else {
            Text(fetchState)
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                val allSelected = mutableListOf<MangaInfo>()
                manga?.let { allSelected.add(it) }
                allSelected.addAll(
                    vm.selectedManga.mapNotNull { key ->
                        vm.recommendations.find { it.id == key.manga.id }
                    }
                )
                vm.addSelectedToPrivate(allSelected)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Selected to Private Wishlist")
        }

        Spacer(Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.weight(1f)
            ) {
                TextField(
                    value = selectedExistingWishlist,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Existing Wishlist") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (existingWishlistNames.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No wishlists available") },
                            onClick = { expanded = false }
                        )
                    } else {
                        existingWishlistNames.forEach { name ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    selectedExistingWishlist = name
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = newWishlistName,
                onValueChange = { newWishlistName = it },
                label = { Text("New Wishlist Name") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(4.dp))

        Button(
            onClick = {
                val allSelected = mutableListOf<MangaInfo>()
                manga?.let { allSelected.add(it) }
                allSelected.addAll(
                    vm.selectedManga.mapNotNull { key ->
                        vm.recommendations.find { it.id == key.manga.id }
                    }
                )

                val targetWishlist = if (newWishlistName.isNotBlank()) newWishlistName
                else selectedExistingWishlist

                if (targetWishlist.isNotBlank()) {
                    vm.publicWishlistName = targetWishlist
                    vm.addSelectedToPublic(allSelected)
                } else {
                    vm.showWishlistMessage("Please select or enter a wishlist name.")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Selected to Public Wishlist")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { vm.goToScan() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Return to Scan")
        }

        Spacer(Modifier.height(4.dp))

        Button(onClick = { vm.goToHome() }, modifier = Modifier.fillMaxWidth()) {
            Text("Home")
        }
    }
}


// Adding a wishlist message box that tells the user if their manga was added successfully - Mihai Panait (991622264)
@Composable
fun WishlistMessageBox(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (message.contains("Failed", ignoreCase = true)) Color(0xFFFFDAD6)
                    else Color(0xFFDCFCE7)
                )
                .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            Text(
                text = message,
                color = if (message.contains("Failed", ignoreCase = true)) Color(0xFF8A1C1C)
                else Color(0xFF065F46)
            )
        }
    }
}

// Custom headers - Mihai Panait (991622264)
@Composable
fun SectionHeader(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.titleLarge)
    }
}


@Composable
fun MangaInfoSection(manga: MangaInfo, vm: MainViewModel) {
    val key = WishlistMangaKey(
        wishlistName = vm.publicWishlistName,
        manga = manga
    )

    val selected = vm.selectedManga.contains(key)

    Row(
        Modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = { vm.toggleMangaSelection(key) }
        )

        AsyncImage(
            model = manga.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .height(150.dp)
                .width(100.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(8.dp))

        Column {
            Text("Title: ${manga.title}")
            Text("Author: ${manga.author}")
            Text("Genre: ${if (manga.genres.isEmpty()) "TBD" else manga.genres.joinToString(", ")}")
        }
    }
}

@Composable
fun RecommendationRow(vm: MainViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader("Recommendations")

        LazyRow(
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (vm.isLoadingRecs) {
                items(3) { ShimmerLoadingCard() }
            } else {
                items(vm.recommendations) { manga ->
                    RecommendationCard(manga, vm)
                }
            }
        }
    }
}


@Composable
fun RecommendationCard(manga: MangaInfo, vm: MainViewModel) {
    val key = WishlistMangaKey(
        wishlistName = vm.publicWishlistName,
        manga = manga
    )

    val selected = vm.selectedManga.contains(key)


    Column(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = { vm.toggleMangaSelection(key) }
        )

        Image(
            painter = rememberAsyncImagePainter(manga.imageUrl),
            contentDescription = manga.title,
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Text(manga.title, maxLines = 2)

        if (manga.genres.isNotEmpty()) {
            Text(
                manga.genres.joinToString(", "),
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun ShimmerLoadingCard() {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition()
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200)
        )
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(0f, 0f),
        end = Offset(translateAnim.value, translateAnim.value)
    )

    Box(
        modifier = Modifier
            .size(width = 140.dp, height = 220.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(brush)
    )
}

// Making a map of different genres to match with Jikan - Mihai Panait (#991622264)
val genreIdMap = mapOf(
    "Action" to 1,
    "Adventure" to 2,
    "Comedy" to 4,
    "Drama" to 8,
    "Ecchi" to 9,
    "Fantasy" to 10,
    "Horror" to 14,
    "Mystery" to 7,
    "Romance" to 22,
    "Sci-Fi" to 24,
    "Slice of Life" to 36,
    "Sports" to 30,
    "Supernatural" to 37
)

suspend fun fetchRecommendations(title: String, genres1: List<String>): List<MangaInfo> {
    return withContext(Dispatchers.IO) {

        fun fetchJson(url: String): JSONObject? {
            return try {
                val text = URL(url).readText()
                JSONObject(text)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        try {
            val encoded = URLEncoder.encode(title, "UTF-8")
            val searchUrl = "https://api.jikan.moe/v4/manga?q=$encoded&limit=1"
            val searchObj = fetchJson(searchUrl) ?: return@withContext emptyList()
            val data = searchObj.getJSONArray("data")
            if (data.length() == 0) return@withContext emptyList()

            val malId = data.getJSONObject(0).getInt("mal_id")

            val recUrl = "https://api.jikan.moe/v4/manga/$malId/recommendations"
            val recObj = fetchJson(recUrl) ?: return@withContext emptyList()
            val recData = recObj.getJSONArray("data")

            (0 until minOf(3, recData.length())).map { i ->
                val entry = recData.getJSONObject(i).getJSONObject("entry")
                val recId = entry.getInt("mal_id")

                Thread.sleep(1000)

                val detailsUrl = "https://api.jikan.moe/v4/manga/$recId"
                val detailObj = fetchJson(detailsUrl)?.optJSONObject("data")

                if (detailObj != null) {
                    val recTitle = detailObj.getString("title")

                    val authorsArray = detailObj.optJSONArray("authors")
                    val author =
                        if (authorsArray != null && authorsArray.length() > 0)
                            authorsArray.getJSONObject(0).getString("name")
                        else "Unknown"

                    val image = detailObj
                        .getJSONObject("images")
                        .getJSONObject("jpg")
                        .getString("image_url")

                    val genresArr = detailObj.optJSONArray("genres")
                    val genres = mutableListOf<String>()
                    if (genresArr != null) {
                        for (g in 0 until genresArr.length()) {
                            genres.add(genresArr.getJSONObject(g).getString("name"))
                        }
                    }

                    MangaInfo(
                        id = recId.toString(),
                        title = recTitle,
                        author = author,
                        imageUrl = image,
                        volume = "",
                        genres = genres
                    )
                } else {
                    MangaInfo(
                        id = recId.toString(),
                        title = entry.getString("title"),
                        author = "Unknown",
                        imageUrl = entry.getJSONObject("images").getJSONObject("jpg")
                            .getString("image_url"),
                        volume = "",
                        genres = emptyList()
                    )
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}



