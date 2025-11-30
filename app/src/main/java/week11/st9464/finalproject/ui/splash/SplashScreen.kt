package week11.st9464.finalproject.ui.splash

import android.R.attr.maxHeight
import android.R.attr.maxWidth
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import week11.st9464.finalproject.viewmodel.MainViewModel

// Created Splash Screen - Jadah C (sID #991612594)
@Composable
fun SplashScreen(vm: MainViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEEEEE))
    ) {
        MangaDiagonalBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Mount Manga",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = Color(0xFF111111),
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "by Jadah Charan and Mihai Panait",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF555555)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Scan, Discover, Connect",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF555555)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { vm.goToLogin() },
                modifier = Modifier
                    .width(160.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Start",
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun MangaDiagonalBackground() {
        val imageUrls = listOf(
            "https://cdn.myanimelist.net/images/anime/1015/138006.jpg", // Frieren
            "https://cdn.myanimelist.net/images/anime/11/50453.jpg", // Kuroko's Basketball
            "https://cdn.myanimelist.net/images/anime/1171/109222.jpg", // JJK
            "https://cdn.myanimelist.net/images/manga/2/258225.jpg", // Haikyuu
            "https://cdn.myanimelist.net/images/manga/3/120337.jpg", // Mushoku Tensei
            "https://cdn.myanimelist.net/images/manga/1/129447.jpg", // Re: Zero
            "https://cdn.myanimelist.net/images/manga/2/253119.jpg", // Hunter x Hunter
            "https://cdn.myanimelist.net/images/manga/3/80661.jpg", // One Punch Man
            "https://cdn.myanimelist.net/images/manga/3/216464.jpg", // Chainsaw Man
            "https://cdn.myanimelist.net/images/manga/1/264496.jpg", // Pluto
            "https://cdn.myanimelist.net/images/manga/3/222295.jpg", // Solo Levelling
            "https://cdn.myanimelist.net/images/manga/3/186073.jpg", // Fate
            "https://cdn.myanimelist.net/images/manga/2/288894.jpg", // Bungo Stray Dogs
            "https://cdn.myanimelist.net/images/manga/3/219741.jpg", // Spy x Family
            "https://cdn.myanimelist.net/images/manga/2/215054.jpg" // My Dress Up Darling
        )

    val density = LocalDensity.current
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val boxSize = 140.dp
        val padding = 8.dp
        val boxPx = with(density) { boxSize.toPx() }
        val padPx = with(density) { padding.toPx() }

        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        val columns = (widthPx / (boxPx + padPx)).toInt().coerceAtLeast(3) + 2
        val rows = (heightPx / (boxPx + padPx)).toInt().coerceAtLeast(3) + 2

        val gridWidth = columns * (boxPx + padPx)
        val gridHeight = rows * (boxPx + padPx)

        val infiniteTransition = rememberInfiniteTransition()
        val offset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                tween(18000, easing = LinearEasing),
                RepeatMode.Restart
            )
        )

        val translationX = lerp(gridWidth, -gridWidth, offset)
        val translationY = lerp(gridHeight, -gridHeight, offset)

        @Composable
        fun Grid(offsetX: Float, offsetY: Float) {
            Column(
                modifier = Modifier
                    .graphicsLayer {
                        this.translationX = offsetX
                        this.translationY = offsetY
                    }
            ) {
                repeat(rows) { r ->
                    Row {
                        repeat(columns) { c ->
                            val index = (r * columns + c) % imageUrls.size
                            Box(
                                modifier = Modifier
                                    .padding(padding)
                                    .size(boxSize)
                                    .clip(RoundedCornerShape(10.dp))
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(imageUrls[index]),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize() 
                                )
                            }
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Grid(translationX, translationY)
            Grid(translationX + gridWidth, translationY + gridHeight)

            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.10f))
            )
        }
    }
}
fun lerp(start: Float, stop: Float, amount: Float): Float {
    return start + (stop - start) * amount
}