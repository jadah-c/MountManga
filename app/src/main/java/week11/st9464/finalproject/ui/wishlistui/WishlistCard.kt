package week11.st9464.finalproject.ui.wishlistui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import week11.st9464.finalproject.model.MangaInfo
import week11.st9464.finalproject.ui.theme.Cream
import week11.st9464.finalproject.ui.theme.EarthBrown
import week11.st9464.finalproject.ui.theme.Lavender
import week11.st9464.finalproject.ui.theme.Slate
import week11.st9464.finalproject.ui.theme.parisFontFamily

// Moved the WishlistCard into it's own file for easier access - Mihai Panait (991622264)
@Composable
fun WishlistCard(
    manga: MangaInfo,
    isSelected: Boolean,
    onSelect: (Boolean) -> Unit,
    comment: String? = null,
    onCommentChange: ((String) -> Unit)? = null,
    isEditing: Boolean = false
) { // Highlight border Slate when card is selected - Jadah Charan (sID #991612594)
    val borderColor = if (isSelected) Slate else Color.Transparent

    Column(
        modifier = Modifier
            .width(120.dp)
            // Rounded card shape and Lavender background added - Jadah Charan (sID #991612594)
            .clip(RoundedCornerShape(12.dp))
            .background(Lavender)
            // Border for selected state - Jadah Charan (sID #991612594)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            // Tap anywhere on card to toggle Select checkbox - Jadah Charan (sID #991612594)
            .clickable { onSelect(!isSelected) }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) { // Manga title styled with EarthBrown for readability on Lavender cards - Jadah Charan (sID #991612594)
        Text(
            manga.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = EarthBrown,
                fontWeight = FontWeight.SemiBold
            ),
            maxLines = 2,
            textAlign = TextAlign.Center
        )
        // Manga cover image with rounded corners and a subtle Slate color outline - Jadah Charan (sID #991612594)
        Image(
            painter = rememberAsyncImagePainter(manga.imageUrl),
            contentDescription = manga.title,
            modifier = Modifier
                .height(120.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = 1.dp,
                    color = Slate.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                )
        )

        Spacer(modifier = Modifier.height(4.dp))
        // Checkbox row for selecting a manga - Jadah Charan (sID #991612594)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelect(it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = Slate,  // Slate checkmark background - Jadah Charan (sID #991612594)
                    uncheckedColor = EarthBrown, // EarthBrown border when unchecked - Jadah Charan (sID #991612594)
                    checkmarkColor = Cream // Cream checkmark for contrast - Jadah Charan (sID #991612594)
                )
            )
            Text("Select", color = EarthBrown, modifier = Modifier.padding(start = 4.dp))
        }
        // Display existing comments in italic (read-only) - Jadah Charan (sID #991612594)
        if (comment != null && !isEditing) {
            Text(
                comment,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = EarthBrown.copy(alpha = 0.7f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                ),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        // Show edit comment field only when editing and the item is selected - Jadah Charan (sID #991612594)
        onCommentChange?.let {
            if (isEditing && isSelected) {
                var textState = remember { mutableStateOf(comment ?: "") }
                androidx.compose.material3.OutlinedTextField(
                    value = textState.value,
                    onValueChange = { newText ->
                        textState.value = newText
                        onCommentChange(newText)
                    },
                    placeholder = { Text("Add a comment") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Slate,
                        unfocusedBorderColor = Slate.copy(alpha = 0.4f),
                        cursorColor = Slate
                    )
                )
            }
        }
    }
}