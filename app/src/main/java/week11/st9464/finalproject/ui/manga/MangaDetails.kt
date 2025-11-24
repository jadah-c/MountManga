package week11.st9464.finalproject.ui.manga

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import week11.st9464.finalproject.model.MangaInfo
import week11.st9464.finalproject.ui.scan.fetchMangaFromJikan
import week11.st9464.finalproject.viewmodel.MainViewModel
import java.net.URL

// Created the MangaDetails screen - Mihai Panait (#991622264)
// API, ML Kit - Mihai Panait (#991622264)
@Composable
fun MangaDetails(vm: MainViewModel) {
    val scannedText = vm.scannedText

    var manga by remember { mutableStateOf<MangaInfo?>(null) }
    var recommendations by remember { mutableStateOf<List<MangaInfo>>(emptyList()) }
    var fetchState by remember { mutableStateOf("Loading...") }

    LaunchedEffect(scannedText) {
        val result = fetchMangaFromJikan(scannedText)
        if (result != null) {
            manga = result
            fetchState = "Success"

            // Fetch recommendations - Mihai Panait (#991622264)
            recommendations = fetchRecommendations(result.title, result.genres)
        } else {
            fetchState = "Failed"
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Manga Details", modifier = Modifier.padding(8.dp))

        if (manga != null) {

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                AsyncImage(
                    model = manga!!.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .height(150.dp)
                        .width(100.dp),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.width(16.dp))

                Column {
                    Text("Title: ${manga!!.title}")
                    Text("Author: ${manga!!.author}")
                    Text(
                        "Genre: ${if (manga!!.genres.isEmpty()) "TBD" else manga!!.genres.joinToString(", ")}"
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {  },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add to my Private Wishlist")
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {  },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add to my Public Wishlist")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Showing the Recommendations - Mihai Panait (#991622264)
            Text("Recommendations", modifier = Modifier.padding(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(recommendations) { rec ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(120.dp)
                    ) {
                        AsyncImage(
                            model = rec.imageUrl,
                            contentDescription = rec.title,
                            modifier = Modifier
                                .height(150.dp)
                                .width(100.dp),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = rec.title,
                            maxLines = 2,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        Text(
                            text = rec.author,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        if (rec.genres.isNotEmpty()) {
                            Text(
                                text = rec.genres.joinToString(", "),
                                maxLines = 2,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }

        } else {
            Text(fetchState)
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

// Getting the recommendations - Mihai Panait (#991622264)
suspend fun fetchRecommendations(title: String, genres: List<String>): List<MangaInfo> =
    withContext(Dispatchers.IO) {
        try {
            if (genres.isEmpty()) return@withContext emptyList()

            val genreName = genres.first()
            val genreId = genreIdMap[genreName] ?: return@withContext emptyList()

            val url =
                "https://api.jikan.moe/v4/manga?genres=$genreId&order_by=score&sort=desc&limit=3"
            val json = URL(url).readText()
            val obj = JSONObject(json)
            val data = obj.getJSONArray("data")

            (0 until data.length()).map { i ->
                val manga = data.getJSONObject(i)

                val recTitle = manga.getString("title")

                val authorsArr = manga.optJSONArray("authors")
                val recAuthor = if (authorsArr != null && authorsArr.length() > 0)
                    authorsArr.getJSONObject(0).getString("name")
                else "Unknown"

                val imageUrl = manga.getJSONObject("images")
                    .getJSONObject("jpg")
                    .getString("image_url")

                MangaInfo(
                    title = recTitle,
                    author = recAuthor,
                    imageUrl = imageUrl,
                    volume = "",
                    genres = listOf(genreName)
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }