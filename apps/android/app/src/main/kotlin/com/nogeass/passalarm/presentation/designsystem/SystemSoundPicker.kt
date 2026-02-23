package com.nogeass.passalarm.presentation.designsystem

import android.media.RingtoneManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SoundItem(
    val id: String,
    val title: String,
)

/**
 * Lists system alarm ringtones using [RingtoneManager].
 *
 * Tapping a row plays a short preview. The currently selected sound shows a
 * checkmark.
 */
@Composable
fun SystemSoundPicker(
    selectedSoundId: String,
    onSoundSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val sounds = remember {
        buildList {
            add(SoundItem("default", "デフォルト"))
            val manager = RingtoneManager(context)
            manager.setType(RingtoneManager.TYPE_ALARM)
            val cursor = manager.cursor
            while (cursor.moveToNext()) {
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                val uri = manager.getRingtoneUri(cursor.position).toString()
                add(SoundItem(uri, title))
            }
        }
    }

    // Track playing ringtone to stop it on dispose
    val playingRingtone = remember { arrayOfNulls<android.media.Ringtone>(1) }
    DisposableEffect(Unit) {
        onDispose { playingRingtone[0]?.stop() }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = Color(0xFF1A1A2E),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
                .padding(vertical = PassSpacing.md),
        ) {
            item {
                Text(
                    text = "アラーム音",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(
                        horizontal = PassSpacing.lg,
                        vertical = PassSpacing.sm,
                    ),
                )
            }
            items(sounds) { sound ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSoundSelected(sound.id)
                            // Preview
                            playingRingtone[0]?.stop()
                            val uri = if (sound.id == "default") {
                                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                            } else {
                                android.net.Uri.parse(sound.id)
                            }
                            val ringtone = RingtoneManager.getRingtone(context, uri)
                            ringtone?.play()
                            playingRingtone[0] = ringtone
                        }
                        .padding(
                            horizontal = PassSpacing.lg,
                            vertical = PassSpacing.md,
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = sound.title,
                        color = Color.White,
                        fontSize = 16.sp,
                    )
                    if (selectedSoundId == sound.id) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "選択中",
                            tint = PassColors.brand,
                        )
                    }
                }
            }
        }
    }
}
