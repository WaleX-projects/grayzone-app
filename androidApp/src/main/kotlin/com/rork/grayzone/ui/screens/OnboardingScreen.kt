package com.rork.grayzone.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rork.grayzone.ui.GrayzoneViewModel
import com.rork.grayzone.ui.components.GzSwitch
import com.rork.grayzone.ui.components.PrimaryButton
import com.rork.grayzone.ui.theme.GzColors

import android.content.Intent
import android.provider.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import com.rork.grayzone.UsageAccess



private val OnboardingApps = listOf(
    "instagram" to "Instagram",
    "tiktok" to "TikTok",
    "x" to "X",
    "youtube" to "YouTube",
    "reddit" to "Reddit",
    "facebook" to "Facebook"
)

private val AllowanceOptions = listOf(30, 45, 60, 90, 120)

@Composable
fun OnboardingScreen(vm: GrayzoneViewModel, onDone: () -> Unit) {

    val context = LocalContext.current

    var page by remember { mutableIntStateOf(0) }
    var selected by remember {
        mutableStateOf(
            setOf("instagram", "tiktok", "x", "youtube")
        )
    }
    var allowance by remember { mutableIntStateOf(60) }

    var waitingForPermission by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(waitingForPermission) {

        if (waitingForPermission) {

            while (waitingForPermission) {

                if (UsageAccess.hasPermission(context)) {

                    waitingForPermission = false

                    
                    vm.completeOnboarding(
                        selected.toList(),
                        allowance
                    )

                    onDone()
                }

                kotlinx.coroutines.delay(1000)
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(GzColors.White)
            .systemBarsPadding()
            .padding(horizontal = 24.dp)
    ) {

        if (page > 0) {

            IconButton(
                onClick = { page-- }
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = GzColors.Ink
                )
            }

        } else {

            Spacer(Modifier.height(48.dp))
        }

        AnimatedContent(
            targetState = page,
            transitionSpec = {
                (
                    slideInHorizontally { it / 5 } +
                        fadeIn()
                    ) togetherWith (
                    slideOutHorizontally { -it / 5 } +
                        fadeOut()
                    )
            },
            label = "onboarding"
        ) { p ->

            when (p) {

                0 -> IntroPage {
                    page = 1
                }

                1 -> AppsPage(
                    selected,
                    onSelect = { id, on ->
                        selected =
                            if (on) {
                                selected + id
                            } else {
                                selected - id
                            }
                    },
                    onContinue = {
                        page = 2
                    }
                )

                2 -> AllowancePage(
                    allowance,
                    onSelect = {
                        allowance = it
                    },
                    onContinue = {
                        page = 3
                    }
                )

               else -> ReadyPage {

    if (UsageAccess.hasPermission(context)) {

        vm.completeOnboarding(
            selected.toList(),
            allowance
        )

        onDone()

    } else {

        waitingForPermission = true

        context.startActivity(
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        )
    }
}
            }
        }
    }
}




@Composable
private fun IntroPage(onStart: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.weight(1f))
        Text(
            "GRAYZONE",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 8.sp,
            color = GzColors.Ink,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(14.dp))
        Text(
            "Reclaim your attention.",
            fontSize = 17.sp,
            color = GzColors.Gray500,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.weight(1.4f))
        PrimaryButton("Get started", onClick = onStart)
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun AppsPage(selected: Set<String>, onSelect: (String, Boolean) -> Unit, onContinue: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            "Choose the apps you want to change.",
            fontSize = 27.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 34.sp,
            color = GzColors.Ink
        )
        Spacer(Modifier.height(28.dp))
        OnboardingApps.forEach { (id, name) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(name, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = GzColors.Ink, modifier = Modifier.weight(1f))
                GzSwitch(checked = id in selected, onCheckedChange = { onSelect(id, it) })
            }
        }
        Spacer(Modifier.weight(1f))
        PrimaryButton(
            "Continue",
            enabled = selected.isNotEmpty(),
            onClick = onContinue
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun AllowancePage(selected: Int, onSelect: (Int) -> Unit, onContinue: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(16.dp))
        Text(
            "How much time feels reasonable?",
            fontSize = 27.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 34.sp,
            color = GzColors.Ink
        )
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            AllowanceOptions.forEach { option ->
                val isSel = option == selected
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(76.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSel) GzColors.Black else GzColors.White)
                        .border(
                            1.dp,
                            if (isSel) GzColors.Black else GzColors.Border,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelect(option) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "$option",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSel) GzColors.White else GzColors.Ink
                    )
                    Text(
                        "min",
                        fontSize = 12.sp,
                        color = if (isSel) GzColors.Gray300 else GzColors.Gray400
                    )
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "This isn't a hard block. Grayzone adds friction as your quota runs low — and you can always earn some back.",
            fontSize = 14.sp,
            lineHeight = 21.sp,
            color = GzColors.Gray500
        )
        Spacer(Modifier.weight(1f))
        PrimaryButton("Continue", onClick = onContinue)
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ReadyPage(onEnable: () -> Unit) {

    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.weight(1f))

        Box(
            Modifier
                .size(64.dp)
                .border(2.dp, GzColors.Black, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .size(64.dp)
                    .padding(8.dp)
                    .border(2.dp, GzColors.Gray300, CircleShape)
            )
        }

        Spacer(Modifier.height(28.dp))

        Text(
            "You're ready.",
            fontSize = 27.sp,
            fontWeight = FontWeight.SemiBold,
            color = GzColors.Ink
        )

        Spacer(Modifier.height(14.dp))

        Text(
            "Grayzone starts gently and only becomes noticeable as your quota runs down.",
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = GzColors.Gray500
        )

        Spacer(Modifier.weight(1f))

        PrimaryButton(
            "Enable protection",
            onClick = onEnable
        )

        Spacer(Modifier.height(32.dp))
    }
}