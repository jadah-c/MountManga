package week11.st9464.finalproject.ui.manga

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import week11.st9464.finalproject.ui.scan.fetchMangaFromJikan
import week11.st9464.finalproject.viewmodel.MainViewModel
import java.net.URL
import java.net.URLEncoder

// Created the MangaDetails screen - Mihai Panait (#991622264)
// API, ML Kit - Mihai Panait (#991622264)

// Made some updates to the Ui of this. Added a loading effect and improved the recommendations - Mihai Panait (#991622264)
@Composable
fun MangaDetails(vm: MainViewModel) {
    val scannedText = vm.scannedText

    var manga by remember { mutableStateOf<MangaInfo?>(null) }
    var fetchState by remember { mutableStateOf("Loading...") }

    LaunchedEffect(scannedText) {
        val result = fetchMangaFromJikan(scannedText)
        if (result != null) {
            manga = result
            fetchState = "Success"

            // Load recommendations with shimmer
            vm.isLoadingRecs = true
            vm.recommendations = fetchRecommendations(result.title, result.genres)
            vm.isLoadingRecs = false
        } else {
            fetchState = "Failed"
        }
    }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionHeader("Manga Details")

        // Calling the wishlist message - Mihai Panait (991622264)
        vm.wishlistMessage?.let { WishlistMessageBox(it) }

        if (manga != null) {
            MangaInfoSection(manga!!, vm)
            Spacer(Modifier.height(24.dp))
            RecommendationRow(vm)
        } else {
            Text(fetchState)
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = { vm.addSelectedToPrivate() }, modifier = Modifier.fillMaxWidth()) {
            Text("Add Selected to Private Wishlist")
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = vm.publicWishlistName,
            onValueChange = { vm.publicWishlistName = it },
            label = { Text("Public Wishlist Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = { vm.addSelectedToPublic() }, modifier = Modifier.fillMaxWidth()) {
            Text("Add Selected to Public Wishlist")
        }

        Spacer(Modifier.height(24.dp))

        Button(onClick = { vm.goToScan() }, modifier = Modifier.fillMaxWidth()) {
            Text("Return to Scan")
        }

        Spacer(Modifier.height(8.dp))

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
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (message.contains("Failed", ignoreCase = true)) Color(0xFFFFDAD6)
                    else Color(0xFFDCFCE7)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
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
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.titleLarge)
    }
}


@Composable
fun MangaInfoSection(manga: MangaInfo, vm: MainViewModel) {
    val selected = vm.selectedManga.contains(manga)

    Row(
        Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = { vm.toggleMangaSelection(manga) }
        )

        AsyncImage(
            model = manga.imageUrl,
            contentDescription = null,
            modifier = Modifier.height(150.dp).width(100.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(16.dp))

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
    val selected = vm.selectedManga.contains(manga)

    Column(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = { vm.toggleMangaSelection(manga) }
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
                null
            }
        }

        try {
            //  Search manga → get ID
            val encoded = URLEncoder.encode(title, "UTF-8")
            val searchUrl = "https://api.jikan.moe/v4/manga?q=$encoded&limit=1"
            val searchObj = fetchJson(searchUrl) ?: return@withContext emptyList()

            val data = searchObj.getJSONArray("data")
            if (data.length() == 0) return@withContext emptyList()

            val malId = data.getJSONObject(0).getInt("mal_id")

            // Get recommendations
            val recUrl = "https://api.jikan.moe/v4/manga/$malId/recommendations"
            val recObj = fetchJson(recUrl) ?: return@withContext emptyList()
            val recData = recObj.getJSONArray("data")

            (0 until minOf(3, recData.length())).map { i ->
                val entry = recData.getJSONObject(i).getJSONObject("entry")
                val recId = entry.getInt("mal_id")

                // MUST WAIT 1 second or Jikan rejects
                Thread.sleep(1000)

                //  Fetch full details
                val detailsUrl = "https://api.jikan.moe/v4/manga/$recId"
                val detailObj = fetchJson(detailsUrl)?.optJSONObject("data")
                    ?: return@map MangaInfo(entry.getString("title"), "Unknown", entry.getJSONObject("images").getJSONObject("jpg").getString("image_url"), "", emptyList())

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

                MangaInfo(recTitle, author, image, "", genres)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

// Getting the recommendations - Mihai Panait (#991622264) (This version didn't get the right recommendations
// However the formatting was great and it showed popular recommendations based on the scanned manga's genre
//suspend fun fetchRecommendations(title: String, genres: List<String>): List<MangaInfo> =
//    withContext(Dispatchers.IO) {
//        try {
//            if (genres.isEmpty()) return@withContext emptyList()
//
//            val genreName = genres.first()
//            val genreId = genreIdMap[genreName] ?: return@withContext emptyList()
//
//            val url =
//                "https://api.jikan.moe/v4/manga?genres=$genreId&order_by=score&sort=desc&limit=3"
//            val json = URL(url).readText()
//            val obj = JSONObject(json)
//            val data = obj.getJSONArray("data")
//
//            (0 until data.length()).map { i ->
//                val manga = data.getJSONObject(i)
//
//                val recTitle = manga.getString("title")
//
//                val authorsArr = manga.optJSONArray("authors")
//                val recAuthor = if (authorsArr != null && authorsArr.length() > 0)
//                    authorsArr.getJSONObject(0).getString("name")
//                else "Unknown"
//
//                val imageUrl = manga.getJSONObject("images")
//                    .getJSONObject("jpg")
//                    .getString("image_url")
//
//                MangaInfo(
//                    title = recTitle,
//                    author = recAuthor,
//                    imageUrl = imageUrl,
//                    volume = "",
//                    genres = listOf(genreName)
//                )
//            }
//
//        } catch (e: Exception) {
//            e.printStackTrace()
//            emptyList()
//        }
//    }

// This below version shows the right recommendations, however the genre and formatting isn't showing - Mihai Panait (#991622264)
//suspend fun fetchRecommendations(title: String, genres1: List<String>): List<MangaInfo> {
//    return withContext(Dispatchers.IO) {
//        try {
//
//            val encoded = URLEncoder.encode(title, "UTF-8")
//            val searchUrl = "https://api.jikan.moe/v4/manga?q=$encoded&limit=1"
//            val searchJson = URL(searchUrl).readText()
//            val searchObj = JSONObject(searchJson)
//            val searchData = searchObj.getJSONArray("data")
//            if (searchData.length() == 0) return@withContext emptyList()
//
//            val mangaObj = searchData.getJSONObject(0)
//            val malId = mangaObj.getInt("mal_id")
//
//
//            val recUrl = "https://api.jikan.moe/v4/manga/$malId/recommendations"
//            val recJson = URL(recUrl).readText()
//            val recObj = JSONObject(recJson)
//            val recData = recObj.getJSONArray("data")
//
//
//            (0 until minOf(3, recData.length())).map { i ->
//                val rec = recData.getJSONObject(i).getJSONObject("entry")
//                val recTitle = rec.getString("title")
//                val recImage = rec.getJSONObject("images").getJSONObject("jpg").getString("image_url")
//                val recAuthorArr = rec.optJSONArray("authors")
//                val recAuthor = if (recAuthorArr != null && recAuthorArr.length() > 0)
//                    recAuthorArr.getJSONObject(0).getString("name") else "Unknown"
//
//
//                val genresArr = rec.optJSONArray("genres")
//                val genres = mutableListOf<String>()
//                if (genresArr != null) {
//                    for (j in 0 until genresArr.length()) {
//                        genres.add(genresArr.getJSONObject(j).getString("name"))
//                    }
//                }
//
//                MangaInfo(recTitle, recAuthor, recImage, "", genres)
//            }
//
//        } catch (e: Exception) {
//            e.printStackTrace()
//            emptyList()
//        }
//    }
//}



