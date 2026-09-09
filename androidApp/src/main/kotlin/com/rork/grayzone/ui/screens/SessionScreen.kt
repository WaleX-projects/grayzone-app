package com.rork.grayzone.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rork.grayzone.ui.GrayzoneViewModel
import com.rork.grayzone.ui.components.PrimaryButton
import com.rork.grayzone.ui.components.RefillChip
import com.rork.grayzone.ui.components.SecondaryButton
import com.rork.grayzone.ui.models.FrictionLevel
import com.rork.grayzone.ui.theme.GzColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Simulated protected-app session. Quota drains in real time (pacing is
 * compressed for the prototype: 2 quota-minutes per tick, 10 s grace period
 * instead of the production 30 s). Friction escalates by remaining quota.
 */
private const val GRACE_SECONDS = 10
private const val DRAIN_MINUTES = 2f
private const val TICK_MS = 1000L
private const val THROTTLE_TICK_MS = 1600L

private data class Emotion(val id: String, val label: String, val message: String, val action: String)

private val EMOTIONS = listOf(
    Emotion(
        "bored", "Bored",
        "Give yourself 60 seconds to make something ridiculous: describe your day using only three words.",
        "Try it"
    ),
    Emotion(
        "anxious", "Anxious",
        "You might be checking because you want certainty. Give yourself 30 seconds without checking for an answer.",
        "Take 30 seconds"
    ),
    Emotion(
        "avoiding", "Avoiding something",
        "What's the smallest next step you could take right now?",
        "Do that"
    ),
    Emotion(
        "lonely", "Lonely",
        "Send one message to someone you haven't talked to recently. It doesn't need to be important.",
        "Send a message"
    )
)

private val FeedShades = listOf(
    Color(0xFFE9E9E9), Color(0xFFDFDFDF), Color(0xFFF1F1F1), Color(0xFFD8D8D8)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(vm: GrayzoneViewModel, appId: String, onExit: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val app = state.apps.firstOrNull { it.id == appId }
    LaunchedEffect(appId) { vm.startSession(appId) }
    if (app == null) return

    val protectedActive = !state.paused && !state.disabled
    val level = if (protectedActive) state.level else FrictionLevel.NORMAL

    var graceLeft by rememberSaveable { mutableIntStateOf(GRACE_SECONDS) }
    var mildShown by rememberSaveable { mutableStateOf(false) }
    var grayShown by rememberSaveable { mutableStateOf(false) }
    var ivShown by rememberSaveable { mutableStateOf(false) }
    var showMild by remember { mutableStateOf(false) }
    var showGray by remember { mutableStateOf(false) }
    var showIntervention by remember { mutableStateOf(false) }
    var showEarn by remember { mutableStateOf(false) }
    var emotion by remember { mutableStateOf<Emotion?>(null) }
    var acted by remember { mutableStateOf(false) }
    var refillToast by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(level) {
        when (level) {
            FrictionLevel.MILD -> if (!mildShown) {
                mildShown = true; showMild = true; vm.markInterrupted()
            }
            FrictionLevel.GRAYSCALE -> if (!grayShown) {
                grayShown = true; showGray = true
            }
            FrictionLevel.INTERVENTION -> if (!ivShown) {
                ivShown = true; showIntervention = true
            }
            FrictionLevel.NORMAL -> {}
        }
    }

    LaunchedEffect(protectedActive, level) {
        if (!protectedActive) return@LaunchedEffect
        while (isActive) {
            if (graceLeft > 0) {
                delay(TICK_MS)
                graceLeft--
            } else {
                val throttled = level >= FrictionLevel.GRAYSCALE
                delay(if (throttled) THROTTLE_TICK_MS else TICK_MS)
                vm.sessionTick(DRAIN_MINUTES)
            }
        }
    }

    LaunchedEffect(refillToast) {
        if (refillToast != null) {
            delay(2200)
            refillToast = null
        }
    }

    val throttled = level >= FrictionLevel.GRAYSCALE

    Box(Modifier.fillMaxSize().background(GzColors.White)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f).padding(start = 4.dp)) {
                    Text(app.name, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = GzColors.Ink)
                    Text(
                        when {
                            !protectedActive -> "Protection is off"
                            graceLeft > 0 -> "Grace period — nothing counted yet"
                            level == FrictionLevel.INTERVENTION -> "Grayscale · slowed · audio muted"
                            throttled -> "Grayscale · slowed"
                            else -> "Protected session"
                        },
                        fontSize = 12.sp,
                        color = GzColors.Gray500
                    )
                }
                IconButton(onClick = { vm.endSession(false); onExit() }) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close session", tint = GzColors.Ink)
                }
            }

            if (throttled) {
                BufferingBar(Modifier.padding(horizontal = 16.dp))
            }

            FeedContent(modifier = Modifier.weight(1f), dimmed = throttled)
        }

        if (refillToast != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 64.dp),
                shape = RoundedCornerShape(50),
                color = GzColors.Black
            ) {
                Text(
                    "+${refillToast} min earned",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = GzColors.White
                )
            }
        }

        AnimatedVisibility(
            visible = showMild,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            FrictionCard {
                Text("Still want to keep going?", fontSize = 19.sp, fontWeight = FontWeight.SemiBold, color = GzColors.Ink)
                Spacer(Modifier.height(6.dp))
                Text("You've used about half of today's quota.", fontSize = 14.sp, color = GzColors.Gray500)
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryButton("I'm done", Modifier.weight(1f)) { vm.endSession(true); onExit() }
                    PrimaryButton("Continue", Modifier.weight(1f)) { showMild = false }
                }
            }
        }

        AnimatedVisibility(
            visible = showGray,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            FrictionCard {
                Text("This app is now grayscale.", fontSize = 19.sp, fontWeight = FontWeight.SemiBold, color = GzColors.Ink)
                Spacer(Modifier.height(6.dp))
                Text("You're running low on today's quota.", fontSize = 14.sp, color = GzColors.Gray500)
                Spacer(Modifier.height(18.dp))
                SecondaryButton("Continue anyway") { showGray = false }
            }
        }
    }

    if (showIntervention) {
        ModalBottomSheet(
            onDismissRequest = { showIntervention = false },
            containerColor = GzColors.White
        ) {
            val chosen = emotion
            Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 36.dp)) {
                if (chosen == null) {
                    Text("You've used today's quota.", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = GzColors.Ink)
                    Spacer(Modifier.height(8.dp))
                    Text("What brought you here?", fontSize = 15.sp, color = GzColors.Gray500)
                    Spacer(Modifier.height(16.dp))
                    EMOTIONS.forEach { e ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { emotion = e },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = GzColors.Gray700,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(14.dp))
                            Text(e.label, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = GzColors.Ink)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { showEarn = true }) {
                        Text("Earn more time", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = GzColors.Ink)
                    }
                    Spacer(Modifier.height(6.dp))
                    SecondaryButton("Continue anyway") { showIntervention = false }
                    Spacer(Modifier.height(6.dp))
                    TextButton(onClick = { showIntervention = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Skip", fontSize = 14.sp, color = GzColors.Gray500)
                    }
                } else {
                    Text(
                        chosen.label.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.5.sp,
                        color = GzColors.Gray500
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        chosen.message,
                        fontSize = 19.sp,
                        lineHeight = 27.sp,
                        color = GzColors.Ink
                    )
                    Spacer(Modifier.height(24.dp))
                    if (!acted) {
                        PrimaryButton(chosen.action) { acted = true }
                    } else {
                        Text(
                            "Nice. That's the whole idea — you chose instead of scrolled.",
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = GzColors.Gray500
                        )
                        Spacer(Modifier.height(16.dp))
                        PrimaryButton("Done") { vm.endSession(true); onExit() }
                    }
                    Spacer(Modifier.height(6.dp))
                    TextButton(onClick = { showIntervention = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Back to the app", fontSize = 14.sp, color = GzColors.Gray500)
                    }
                }
            }
        }
    }

    if (showEarn) {
        ModalBottomSheet(
            onDismissRequest = { showEarn = false },
            containerColor = GzColors.White
        ) {
            Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 36.dp)) {
                Text("Earn more time", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = GzColors.Ink)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Off-screen actions add minutes back. Refills diminish with each use and are capped per day.",
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = GzColors.Gray500
                )
                Spacer(Modifier.height(12.dp))
                state.refillSources.forEach { src ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(src.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = GzColors.Ink)
                            Text(
                                when {
                                    !src.enabled -> "Turned off in Settings"
                                    src.usedToday >= src.dailyCap -> "Cap reached — 0 min"
                                    else -> "${src.usedToday} of ${src.dailyCap} used today"
                                },
                                fontSize = 12.sp,
                                color = GzColors.Gray400
                            )
                        }
                        val available = src.enabled && src.minutesNow > 0
                        if (available) {
                            TextButton(onClick = {
                                vm.useRefill(src.id)
                                refillToast = src.minutesNow
                                showIntervention = false
                            }) {
                                Text(
                                    "+${src.minutesNow} min",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GzColors.Ink
                                )
                            }
                        } else {
                            Text("—", fontSize = 16.sp, color = GzColors.Gray300)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                SecondaryButton("Back") { showEarn = false }
            }
        }
    }
}

@Composable
private fun FrictionCard(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = GzColors.White,
        border = BorderStroke(1.dp, GzColors.Border),
        shadowElevation = 12.dp
    ) {
        Column(Modifier.padding(20.dp).fillMaxWidth(), content = { content() })
    }
}

@Composable
private fun BufferingBar(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "buffer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing)),
        label = "progress"
    )
    Box(
        modifier
            .fillMaxWidth()
            .height(2.dp)
            .clip(RoundedCornerShape(1.dp))
            .background(GzColors.Track)
    ) {
        Box(
            Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(2.dp)
                .background(GzColors.Gray300)
        )
    }
}

@Composable
private fun FeedContent(modifier: Modifier = Modifier, dimmed: Boolean) {
    Box(modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize()) {
            items(14) { index ->
                FeedItem(FeedShades[index % FeedShades.size])
            }
        }
        if (dimmed) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.06f))
            )
        }
    }
}

@Composable
private fun FeedItem(shade: Color) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFCFCFCF))
            )
            Spacer(Modifier.width(10.dp))
            Column {
                PlaceholderLine(0.35f, 9.dp)
                Spacer(Modifier.height(5.dp))
                PlaceholderLine(0.22f, 9.dp)
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(shade)
        )
        Spacer(Modifier.height(12.dp))
        PlaceholderLine(0.85f, 10.dp)
        Spacer(Modifier.height(6.dp))
        PlaceholderLine(0.55f, 10.dp)
    }
}

@Composable
private fun PlaceholderLine(widthFraction: Float, height: androidx.compose.ui.unit.Dp) {
    Box(
        Modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFFE3E3E3))
    )
}
