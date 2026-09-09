package com.rork.grayzone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rork.grayzone.ui.GrayzoneViewModel
import com.rork.grayzone.ui.components.MainScaffold
import com.rork.grayzone.ui.components.SectionLabel
import com.rork.grayzone.ui.components.UsageRing
import com.rork.grayzone.ui.models.FrictionLevel
import com.rork.grayzone.ui.theme.GzColors
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    vm: GrayzoneViewModel,
    openApp: (String) -> Unit,
    navigate: (String) -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()

    MainScaffold(selected = "home", onSelect = navigate) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Text("Today", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = GzColors.Ink)
            Spacer(Modifier.height(30.dp))

            UsageRing(
                percent = state.quotaPercent,
                modifier = Modifier
                    .size(236.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(
                    "${state.quotaLeftMin.roundToInt()}",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Light,
                    color = GzColors.Ink
                )
                Text("min left", fontSize = 15.sp, color = GzColors.Gray500)
                Spacer(Modifier.height(8.dp))
                Text("of ${state.allowanceMin} min", fontSize = 13.sp, color = GzColors.Gray400)
            }

            Spacer(Modifier.height(34.dp))

            SectionLabel("Protection")
            Spacer(Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = GzColors.Surface
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    state.disabled -> GzColors.Gray300
                                    state.paused -> GzColors.Gray400
                                    else -> GzColors.Black
                                }
                            )
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            when {
                                state.disabled -> "Off"
                                state.paused -> "Paused"
                                else -> "Active"
                            },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GzColors.Ink
                        )
                        Text(
                            when {
                                state.disabled -> "Protection is disabled"
                                state.paused -> "Paused ${state.pausedLabel ?: ""}"
                                else -> "Friction is enabled"
                            },
                            fontSize = 13.sp,
                            color = GzColors.Gray500
                        )
                    }
                    if (state.paused || state.disabled) {
                        TextButton(onClick = { vm.resumeProtection() }) {
                            Text("Resume", fontSize = 14.sp, color = GzColors.Ink, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(Modifier.height(22.dp))
            SectionLabel("Next friction level")
            Spacer(Modifier.height(8.dp))
            NextFrictionCard(state = state)

            Spacer(Modifier.height(22.dp))
            SectionLabel("Protected apps")
            Spacer(Modifier.height(4.dp))
            val protectedApps = state.apps.filter { it.isProtected }
            protectedApps.forEachIndexed { index, app ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable { openApp(app.id) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(app.name, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = GzColors.Ink, modifier = Modifier.weight(1f))
                    Text("${app.usedTodayMin} min", fontSize = 14.sp, color = GzColors.Gray500)
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Outlined.ChevronRight,
                        contentDescription = "Open ${app.name}",
                        tint = GzColors.Gray300,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (index < protectedApps.lastIndex) {
                    HorizontalDivider(color = GzColors.Border, thickness = 1.dp)
                }
            }
            if (protectedApps.isEmpty()) {
                Text(
                    "No apps are protected yet. Add some in Apps.",
                    fontSize = 14.sp,
                    color = GzColors.Gray400,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun NextFrictionCard(state: com.rork.grayzone.ui.models.GrayzoneState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = GzColors.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, GzColors.Border)
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                when {
                    state.disabled || state.paused -> {
                        Text("—", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = GzColors.Gray400)
                        Text("Friction is off", fontSize = 13.sp, color = GzColors.Gray500)
                    }
                    state.level == com.rork.grayzone.ui.models.FrictionLevel.NORMAL -> {
                        Text("Mild friction", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = GzColors.Ink)
                        Text(
                            "at ${state.thresholds.mildPercent * state.allowanceMin / 100} min left",
                            fontSize = 13.sp,
                            color = GzColors.Gray500
                        )
                    }
                    state.level == com.rork.grayzone.ui.models.FrictionLevel.MILD -> {
                        Text("Grayscale + throttle", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = GzColors.Ink)
                        Text(
                            "at ${state.thresholds.grayscalePercent * state.allowanceMin / 100} min left",
                            fontSize = 13.sp,
                            color = GzColors.Gray500
                        )
                    }
                    state.level == com.rork.grayzone.ui.models.FrictionLevel.GRAYSCALE -> {
                        Text("Intervention", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = GzColors.Ink)
                        Text("at 0 min left", fontSize = 13.sp, color = GzColors.Gray500)
                    }
                    else -> {
                        Text("Quota used", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = GzColors.Ink)
                        Text("Refill to de-escalate, or continue anyway", fontSize = 13.sp, color = GzColors.Gray500)
                    }
                }
            }
        }
    }
}
