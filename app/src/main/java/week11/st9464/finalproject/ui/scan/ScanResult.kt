package week11.st9464.finalproject.ui.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import week11.st9464.finalproject.model.MangaInfo
import week11.st9464.finalproject.ui.theme.Cream
import week11.st9464.finalproject.ui.theme.EarthBrown
import week11.st9464.finalproject.ui.theme.Golden
import week11.st9464.finalproject.ui.theme.Slate
import week11.st9464.finalproject.ui.theme.parisFontFamily
import week11.st9464.finalproject.viewmodel.MainViewModel
import java.net.URLEncoder
import java.net.URL

// Created the Scan Result screen - Mihai Panait (#991622264)
// API, ML Kit - Mihai Panait (#991622264)

@Composable
fun ScanResultScreen(vm: MainViewModel) {
    val scannedText = vm.scannedText

    var mangaTitle by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var volume by remember { mutableStateOf("") }
    var fetchState by remember { mutableStateOf("Loading...") }

    LaunchedEffect(scannedText) {
        val result = fetchMangaFromJikan(scannedText)
        if (result == null) {
            fetchState = "Capture Failed"
        } else {
            mangaTitle = result.title
            author = result.author
            imageUrl = result.imageUrl
            volume = result.volume
            fetchState = "Capture Success"
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Scan Result",
            color = EarthBrown,
            fontFamily = parisFontFamily,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )

        Spacer(Modifier.height(20.dp))

        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.height(250.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            Text("No Image Found", color = Slate, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        Text("Capture: $fetchState")

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { vm.goToMangaDetails() },
            modifier = Modifier.fillMaxWidth(),
            enabled = fetchState == "Capture Success",
            colors = ButtonDefaults.buttonColors(
                containerColor = EarthBrown,
                contentColor = Golden
            )
        ) {
            Text("View Manga Details", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))

        if (fetchState == "Capture Success") {
            Text("Title: $mangaTitle")
            Text("Author: $author")
            if (volume.isNotBlank()) Text("Volume: $volume")
        }

        Spacer(Modifier.height(30.dp))

        Button(
            onClick = { vm.scanAgain() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Slate,
                contentColor = Color.White
            )
        ) {
            Text("Scan Again", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = { vm.goToHome() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Slate,
                contentColor = Color.White
            )
        ) {
            Text("Home", fontWeight = FontWeight.Bold)
        }
    }
}

// Making sure the text recognition is cleared up - Mihai Panait (#991622264)
fun cleanQuery(raw: String): String {
    return raw.lowercase()
        .replace("[^a-z0-9 ]".toRegex(), "")
        .replace("manga", "")
        .replace("issue", "")
        .replace("volume", "")
        .replace("""vol\.?\s*\d+""".toRegex(), "")
        .replace("""\d+""".toRegex(), "")
        .replace("\\s+".toRegex(), " ")
        .trim()
}

// Returns how many edits (insert, delete, substitute) are needed to turn string a into string b - Mihai Panait (#991622264)
fun levenshtein(a: String, b: String): Int {
    val dp = Array(a.length + 1) { IntArray(b.length + 1) }
    for (i in 0..a.length) dp[i][0] = i
    for (j in 0..b.length) dp[0][j] = j
    for (i in 1..a.length) {
        for (j in 1..b.length) {
            dp[i][j] = minOf(
                dp[i - 1][j] + 1,
                dp[i][j - 1] + 1,
                dp[i - 1][j - 1] + if (a[i - 1] == b[j - 1]) 0 else 1
            )
        }
    }
    return dp[a.length][b.length]
}

fun isCloseMatch(a: String, b: String, thresholdPercent: Int = 40): Boolean {
    val distance = levenshtein(a, b)
    val maxLen = maxOf(a.length, b.length)
    return distance <= maxLen * thresholdPercent / 100
}

// Fetching the query from Jikan - Mihai Panait (#991622264)
suspend fun fetchMangaFromJikan(query: String): MangaInfo? {
    return withContext(Dispatchers.IO) {
        try {
            val cleaned = cleanQuery(query)
            if (cleaned.isBlank()) return@withContext null

            val encoded = URLEncoder.encode(cleaned, "UTF-8")
            val url = "https://api.jikan.moe/v4/manga?q=$encoded&limit=10"
            val json = URL(url).readText()
            val obj = JSONObject(json)
            val data = obj.getJSONArray("data")
            if (data.length() == 0) return@withContext null

            val results = (0 until data.length()).map { data.getJSONObject(it) }

            val best = results.firstOrNull { manga ->
                val titles = listOf(
                    manga.getString("title"),
                    manga.optString("title_english", ""),
                    manga.optString("title_japanese", "")
                ).map { it.lowercase().trim() }
                titles.any { it.contains(cleaned) || isCloseMatch(cleaned, it) }
            } ?: results.first()

            val title = best.getString("title")
            val authorsArr = best.optJSONArray("authors")
            val author = if (authorsArr != null && authorsArr.length() > 0)
                authorsArr.getJSONObject(0).getString("name") else "Unknown"
            val volumes = if (!best.isNull("volumes")) best.getInt("volumes") else null
            val image = best.getJSONObject("images").getJSONObject("jpg").getString("image_url")

            // Extract genres - Mihai Panait (#991622264)
            val genresArr = best.optJSONArray("genres")
            val genres = mutableListOf<String>()
            if (genresArr != null) {
                for (i in 0 until genresArr.length()) {
                    genres.add(genresArr.getJSONObject(i).getString("name"))
                }
            }

            MangaInfo(
                id = best.getInt("mal_id").toString(),
                title,
                author,
                image,
                volumes?.toString() ?: "",
                genres
            )

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}