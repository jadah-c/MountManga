package week11.st9464.finalproject.ui.splash

import android.R.attr.maxHeight
import android.R.attr.maxWidth
import android.R.attr.text
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import week11.st9464.finalproject.ui.theme.Cream
import week11.st9464.finalproject.ui.theme.EarthBrown
import week11.st9464.finalproject.ui.theme.Golden
import week11.st9464.finalproject.ui.theme.Lavender
import week11.st9464.finalproject.ui.theme.parisFontFamily
import week11.st9464.finalproject.viewmodel.MainViewModel

// Created Splash Screen - Jadah C (sID #991612594)
@Composable
fun SplashScreen(vm: MainViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Lavender)
    ) {
        MangaDiagonalBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Box(
                modifier = Modifier
                    .background(Cream.copy(alpha = 0.92f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Mount Manga",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = EarthBrown,
                            fontWeight = FontWeight.Bold,
                            fontFamily = parisFontFamily
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "by Jadah Charan and Mihai Panait",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = EarthBrown
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Scan, Discover, Connect",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = EarthBrown
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { vm.goToLogin() },
                modifier = Modifier
                    .width(160.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Golden,
                    contentColor = EarthBrown
                )
            ) {
                Text(
                    "Start",
                    color = EarthBrown,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MangaDiagonalBackground() {

    val imageUrls = listOf(
        "https://cdn.myanimelist.net/images/anime/1015/138006.jpg",
        "https://cdn.myanimelist.net/images/anime/11/50453.jpg",
        "https://cdn.myanimelist.net/images/anime/1171/109222.jpg",
        "https://cdn.myanimelist.net/images/manga/2/258225.jpg",
        "https://cdn.myanimelist.net/images/manga/3/120337.jpg",
        "https://cdn.myanimelist.net/images/manga/1/129447.jpg",
        "https://cdn.myanimelist.net/images/manga/2/253119.jpg",
        "https://cdn.myanimelist.net/images/manga/3/80661.jpg",
        "https://cdn.myanimelist.net/images/manga/3/216464.jpg",
        "https://cdn.myanimelist.net/images/manga/1/264496.jpg",
        "https://cdn.myanimelist.net/images/manga/3/222295.jpg",
        "https://cdn.myanimelist.net/images/manga/3/186073.jpg",
        "https://cdn.myanimelist.net/images/manga/2/288894.jpg",
        "https://cdn.myanimelist.net/images/manga/3/219741.jpg",
        "https://cdn.myanimelist.net/images/manga/2/215054.jpg"
    )

    val boxSize = 130.dp
    val padding = 10.dp

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val density = LocalDensity.current
        val boxPx = with(density) { (boxSize + padding).toPx() }
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }

        val columns = (screenWidthPx / boxPx).toInt() + 4
        val rows = (screenHeightPx / boxPx).toInt() + 6

        val infiniteTransition = rememberInfiniteTransition()

        val offset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = boxPx,
            animationSpec = infiniteRepeatable(
                animation = tween(8000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        val grid = @Composable { startIndex: Int, shift: Float ->
            Column(
                modifier = Modifier.graphicsLayer {
                    translationX = -shift
                    translationY = -shift
                }
            ) {
                var index = startIndex
                repeat(rows) {
                    Row {
                        repeat(columns) {
                            val url = imageUrls[index % imageUrls.size]
                            Image(
                                painter = rememberAsyncImagePainter(url),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .padding(padding)
                                    .size(boxSize)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            index++
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            grid(0, offset)
            grid(imageUrls.size / 2, offset - boxPx)

            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.12f))
            )
        }
    }
}