package week11.st9464.finalproject.data

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import week11.st9464.finalproject.model.MangaInfo
import week11.st9464.finalproject.model.PublicWishlistSummary
import kotlin.text.set

// Created the MangaRepository - Jadah C (sID #991612594)
class MangaRepository {
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(auth.currentUser?.email ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String): Result<String> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(auth.currentUser?.email ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUserEmail(): String? = auth.currentUser?.email

    // Working on the private and public wishlists - Mihai Panait (991622264)
    suspend fun addToPrivate(uid: String, manga: MangaInfo) {
        db.collection("users")
            .document(uid)
            .collection("privateWishlist")
            .document(manga.id)
            .set(
                mapOf(
                    "id" to manga.id,
                    "title" to manga.title,
                    "author" to manga.author,
                    "imageUrl" to manga.imageUrl,
                    "volume" to manga.volume,
                    "genres" to manga.genres
                )
            )
            .await()
    }

    suspend fun addToPublic(uid: String, manga: MangaInfo, wishlistName: String = "Default") {
        db.collection("publicWishlist")
            .document("${uid}_${wishlistName}_${manga.id}")
            .set(
                mapOf(
                    "id" to manga.id,
                    "uid" to uid,
                    "email" to auth.currentUser?.email.orEmpty(),
                    "title" to manga.title,
                    "author" to manga.author,
                    "imageUrl" to manga.imageUrl,
                    "volume" to manga.volume,
                    "genres" to manga.genres,
                    "wishlistName" to wishlistName,
                    "comment" to manga.comment.orEmpty()
                )
            )
            .await()
    }

    // Giving the user option to remove from their wishlist - Mihai Panait (991622264)
    suspend fun removeFromPrivate(uid: String, mangaId: String) {
        db.collection("users")
            .document(uid)
            .collection("privateWishlist")
            .document(mangaId)
            .delete()
            .await()
    }

    suspend fun removeFromPublic(uid: String, wishlistName: String, mangaId: String) {
        val docId = "${uid}_${wishlistName}_$mangaId"

        db.collection("publicWishlist")
            .document(docId)
            .delete()
            .await()
    }

    // Fetching the wishlists to display them later - Mihai Panait (991622264)
    suspend fun getPrivateWishlist(uid: String): List<MangaInfo> {
        val snapshot = db.collection("users")
            .document(uid)
            .collection("privateWishlist")
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                MangaInfo(
                    id = doc.getString("id") ?: doc.getString("title") ?: "Unknown",
                    title = doc.getString("title") ?: "Unknown",
                    author = doc.getString("author") ?: "Unknown",
                    imageUrl = doc.getString("imageUrl") ?: "",
                    volume = doc.getString("volume") ?: "",
                    genres = doc.get("genres") as? List<String> ?: emptyList(),
                    comment = doc.getString("comment")
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getPublicWishlist(uid: String): List<MangaInfo> {
        val snapshot = db.collection("publicWishlist")
            .whereEqualTo("uid", uid)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                MangaInfo(
                    id = doc.getString("id") ?: "Unknown",
                    title = doc.getString("title") ?: "Unknown",
                    author = doc.getString("author") ?: "Unknown",
                    imageUrl = doc.getString("imageUrl") ?: "",
                    volume = doc.getString("volume") ?: "",
                    genres = doc.get("genres") as? List<String> ?: emptyList(),
                    comment = doc.getString("comment")
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    // For all public wishlists - Mihai Panait (991622264)
    suspend fun getAllPublicWishlists(): List<Pair<MangaInfo, String>> {
        val snapshot = db.collection("publicWishlist").get().await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                val manga = MangaInfo(
                    id = doc.getString("id") ?: doc.getString("title") ?: "Unknown",
                    title = doc.getString("title") ?: "Unknown",
                    author = doc.getString("author") ?: "Unknown",
                    imageUrl = doc.getString("imageUrl") ?: "",
                    volume = doc.getString("volume") ?: "",
                    genres = doc.get("genres") as? List<String> ?: emptyList(),
                    comment = doc.getString("comment")
                )
                val ownerUid = doc.getString("uid") ?: ""
                manga to ownerUid
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getAllPublicWishlistSummaries(): List<PublicWishlistSummary> {
        val snapshot = db.collection("publicWishlist").get().await()
        val grouped = snapshot.documents.groupBy { doc ->
            val uid = doc.getString("uid") ?: ""
            val wishlistName = doc.getString("wishlistName") ?: "Default"
            uid to wishlistName
        }

        return grouped.map { (key, docs) ->
            val (uid, wishlistName) = key
            val email = docs.firstOrNull()?.getString("email") ?: "Unknown"
            PublicWishlistSummary(
                uid = uid,
                email = email,
                wishlistName = wishlistName,
                mangaCount = docs.size
            )
        }
    }

    // After selecting a public wishlist to view - Mihai Panait (991622264)
    suspend fun getPublicWishlistByUidAndName(uid: String, wishlistName: String): List<MangaInfo> {
        val snapshot = db.collection("publicWishlist")
            .whereEqualTo("uid", uid)
            .whereEqualTo("wishlistName", wishlistName)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                MangaInfo(
                    id = doc.getString("id") ?: "Unknown",
                    title = doc.getString("title") ?: "Unknown",
                    author = doc.getString("author") ?: "Unknown",
                    imageUrl = doc.getString("imageUrl") ?: "",
                    volume = doc.getString("volume") ?: "",
                    genres = doc.get("genres") as? List<String> ?: emptyList(),
                    comment = doc.getString("comment")
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    // Fetch only the public wishlist summaries for a specific user - Mihai Panait (991622264)
    suspend fun getPublicWishlistSummariesByUid(uid: String): List<PublicWishlistSummary> {
        val snapshot = db.collection("publicWishlist")
            .whereEqualTo("uid", uid)
            .get()
            .await()

        val grouped = snapshot.documents.groupBy { doc ->
            doc.getString("wishlistName") ?: "Default"
        }

        return grouped.map { (wishlistName, docs) ->
            val email = docs.firstOrNull()?.getString("email") ?: "Unknown"
            PublicWishlistSummary(
                uid = uid,
                email = email,
                wishlistName = wishlistName,
                mangaCount = docs.size
            )
        }
    }

    // Updating Manga Comments - Mihai Panait (991622264)
    suspend fun updatePrivateMangaComment(uid: String, mangaId: String, comment: String) {
        db.collection("users")
            .document(uid)
            .collection("privateWishlist")
            .document(mangaId)
            .update("comment", comment)
            .await()
    }

    suspend fun updatePublicMangaComment(
        uid: String,
        wishlistName: String,
        mangaId: String,
        comment: String
    ) {
        db.collection("publicWishlist")
            .document("${uid}_${wishlistName}_$mangaId")
            .update("comment", comment)
            .await()
    }
}

