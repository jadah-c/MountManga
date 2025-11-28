package week11.st9464.finalproject.ui.wishlistui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import week11.st9464.finalproject.model.MangaInfo

// Moved the WishlistCard into it's own file for easier access - Mihai Panait (991622264)
@Composable
fun WishlistCard(
    manga: MangaInfo,
    isSelected: Boolean,
    onSelect: (Boolean) -> Unit,
    comment: String? = null,
    onCommentChange: ((String) -> Unit)? = null,
    isEditing: Boolean = false
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            manga.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2
        )

        Image(
            painter = rememberAsyncImagePainter(manga.imageUrl),
            contentDescription = manga.title,
            modifier = Modifier
                .height(120.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelect(it) }
            )
            Text("Select", modifier = Modifier.padding(start = 4.dp))
        }

        if (comment != null && !isEditing) {
            Text(
                comment,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                ),
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        onCommentChange?.let {
            if (isEditing && isSelected) {
                var textState = remember { mutableStateOf(comment ?: "") }
                androidx.compose.material3.OutlinedTextField(
                    value = textState.value,
                    onValueChange = { newText ->
                        textState.value = newText
                        onCommentChange(newText) // update the shared state map
                    },
                    placeholder = { Text("Add a comment") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}