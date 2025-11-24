package week11.st9464.finalproject.model

// Created the MangaInfo - Mihai Panait (#991622264)

data class MangaInfo(
    val title: String,
    val author: String,
    val imageUrl: String?,
    val volume: String,
    val genres: List<String> = emptyList()
)