package com.nogeass.passalarm.presentation.settings

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nogeass.passalarm.data.remote.FeedbackApi
import com.nogeass.passalarm.presentation.designsystem.MapBackdrop
import com.nogeass.passalarm.presentation.designsystem.PassButton
import com.nogeass.passalarm.presentation.designsystem.PassButtonSize
import com.nogeass.passalarm.presentation.designsystem.PassColors
import com.nogeass.passalarm.presentation.designsystem.PassHapticType
import com.nogeass.passalarm.presentation.designsystem.PassSpacing
import com.nogeass.passalarm.presentation.designsystem.PraiseToast
import com.nogeass.passalarm.presentation.designsystem.TimeOfDay
import kotlinx.coroutines.launch

private const val MAX_LENGTH = 2000

@Composable
fun FeedbackScreen(navController: NavController) {
    var message by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showToast by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(modifier = Modifier.fillMaxSize().imePadding()) {
        MapBackdrop(timeOfDay = TimeOfDay.Evening)

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = PassSpacing.sm, vertical = PassSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "戻る",
                        tint = Color.White,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "改善要望",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(modifier = Modifier.size(48.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = PassSpacing.md),
                verticalArrangement = Arrangement.spacedBy(PassSpacing.md),
            ) {
                Text(
                    text = "どんな機能がほしいですか？\n不便なところはありますか？",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )

                Surface(
                    shape = RoundedCornerShape(PassSpacing.cardCorner),
                    color = Color.White.copy(alpha = 0.08f),
                ) {
                    TextField(
                        value = message,
                        onValueChange = { if (it.length <= MAX_LENGTH) message = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .focusRequester(focusRequester),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = PassColors.brand,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        placeholder = {
                            Text(
                                "例: スヌーズの長さを変えたい",
                                color = Color.White.copy(alpha = 0.3f),
                            )
                        },
                    )
                }

                Text(
                    text = "${message.length} / $MAX_LENGTH",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.3f),
                )

                errorMessage?.let {
                    Text(text = it, fontSize = 12.sp, color = Color.Red.copy(alpha = 0.8f))
                }

                PassButton(
                    title = if (isSending) "送信中…" else "送信する",
                    size = PassButtonSize.Medium,
                    color = PassColors.brand,
                    hapticType = PassHapticType.Tap,
                    enabled = message.isNotBlank() && !isSending,
                    onClick = {
                        scope.launch {
                            isSending = true
                            errorMessage = null
                            val appVersion = try {
                                context.packageManager
                                    .getPackageInfo(context.packageName, 0).versionName ?: "unknown"
                            } catch (_: Exception) { "unknown" }
                            val result = FeedbackApi.send(
                                message = message,
                                appVersion = appVersion,
                                device = "${Build.MANUFACTURER} ${Build.MODEL}",
                                osVersion = "Android ${Build.VERSION.RELEASE}",
                                platform = "Android",
                            )
                            isSending = false
                            result.onSuccess {
                                showToast = true
                            }.onFailure {
                                errorMessage = "送信に失敗しました。通信環境をご確認ください。"
                            }
                        }
                    },
                )

                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally),
                        color = PassColors.brand,
                        strokeWidth = 2.dp,
                    )
                }
            }
        }

        PraiseToast(
            message = "ご要望を送信しました！",
            isVisible = showToast,
            onDismiss = {
                showToast = false
                navController.popBackStack()
            },
        )
    }
}
