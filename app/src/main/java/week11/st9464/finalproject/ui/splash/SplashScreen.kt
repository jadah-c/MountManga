package week11.st9464.finalproject.ui.splash

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
    val boxSize = 120.dp
    val boxPadding = 4.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEEEEE))
    ) {
        // Created a diagonal grid - Jadah C (sID #991612594)
        MangaDiagonalBackground(boxSize = boxSize, boxPadding = boxPadding)

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
fun MangaDiagonalBackground(
    boxSize: Dp = 120.dp,
    boxPadding: Dp = 4.dp
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val boxSizePx = with(density) { boxSize.toPx() }
    val paddingPx = with(density) { boxPadding.toPx() }

    val rows = 6
    val columns = 3

    val imageUrls = listOf(
        "https://cdn.myanimelist.net/images/anime/1015/138006.jpg",
        "https://cdn.myanimelist.net/images/anime/1015/138006.jpg",
        "https://cdn.myanimelist.net/images/anime/1015/138006.jpg",
        "https://cdn.myanimelist.net/images/anime/1015/138006.jpg",
        "https://cdn.myanimelist.net/images/anime/1015/138006.jpg",
        "https://cdn.myanimelist.net/images/anime/1015/138006.jpg",
        "https://cdn.myanimelist.net/images/anime/1015/138006.jpg",
        "https://cdn.myanimelist.net/images/anime/1015/138006.jpg",
        "https://cdn.myanimelist.net/images/anime/1015/138006.jpg",
        "https://cdn.myanimelist.net/images/anime/1015/138006.jpg",
        "https://cdn.myanimelist.net/images/anime/1015/138006.jpg",
        "https://cdn.myanimelist.net/images/anime/1015/138006.jpg"
    )

    val gridWidth = columns * boxSizePx + (columns - 1) * paddingPx
    val gridHeight = rows * boxSizePx + (rows - 1) * paddingPx

    val startX = screenWidthPx + gridWidth
    val startY = screenHeightPx + gridHeight
    val endX = -gridWidth / 2
    val endY = -gridHeight / 2

    val infiniteTransition = rememberInfiniteTransition()
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val mappedProgress = if (animProgress <= 0.85f) {
        animProgress / 0.85f * 0.85f
    } else {
        0.85f + (animProgress - 0.85f) / 0.15f * 0.15f
    }

    val currentX = startX + (endX - startX) * mappedProgress
    val currentY = startY + (endY - startY) * mappedProgress

    var imageIndex = 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = currentX - gridWidth / 2
                translationY = currentY - gridHeight / 2
            }
    ) {
        Column {
            repeat(rows) {
                Row {
                    repeat(columns) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUrls[imageIndex % imageUrls.size]),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(boxPadding)
                                .size(boxSize)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        imageIndex++
                    }
                }
            }
        }
    }
}