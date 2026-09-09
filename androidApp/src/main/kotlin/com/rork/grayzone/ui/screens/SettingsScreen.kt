package com.rork.grayzone.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rork.grayzone.ui.GrayzoneViewModel
import com.rork.grayzone.ui.components.GzChip
import com.rork.grayzone.ui.components.GzSwitch
import com.rork.grayzone.ui.components.MainScaffold
import com.rork.grayzone.ui.components.PrimaryButton
import com.rork.grayzone.ui.components.SectionLabel
import com.rork.grayzone.ui.components.SettingsRow
import com.rork.grayzone.ui.theme.GzColors
import java.util.Calendar

private val AllowanceOptions = listOf(30, 45, 60, 90, 120)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(vm: GrayzoneViewModel, navigate: (String) -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showPause by remember { mutableStateOf(false) }
    var showAllowance by remember { mutableStateOf(false) }

    MainScaffold(selected = "settings", onSelect = navigate) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Text("Settings", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = GzColors.Ink)
            Spacer(Modifier.height(22.dp))

            SectionLabel("Daily allowance")
            Spacer(Modifier.height(8.dp))
            SettingsCard {
                SettingsRow("Daily allowance", "${state.allowanceMin} min") { showAllowance = true }
            }

            Spacer(Modifier.height(24.dp))
            SectionLabel("Friction thresholds")
            Spacer(Modifier.height(4.dp))
            Text(
                "Driven by quota remaining, not session time.",
                fontSize = 13.sp,
                color = GzColors.Gray400
            )
            Spacer(Modifier.height(8.dp))
            SettingsCard {
                ThresholdRow("Mild friction", "mild", state.thresholds.mildPercent, listOf(60, 50, 40, 30), vm)
                HorizontalDivider(color = GzColors.Border, thickness = 1.dp, modifier = Modifier.padding(start = 16.dp))
                ThresholdRow("Grayscale + throttle", "grayscale", state.thresholds.grayscalePercent, listOf(30, 25, 20, 15, 10), vm)
                HorizontalDivider(color = GzColors.Border, thickness = 1.dp, modifier = Modifier.padding(start = 16.dp))
                ThresholdRow("Intervention", "intervention", state.thresholds.interventionPercent, listOf(10, 5, 0), vm)
            }

            Spacer(Modifier.height(24.dp))
            SectionLabel("Refill sources")
            Spacer(Modifier.height(4.dp))
            Text(
                "Refills are capped each day and diminish with repeated use.",
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = GzColors.Gray400
            )
            Spacer(Modifier.height(8.dp))
            SettingsCard {
                state.refillSources.forEachIndexed { index, src ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            iconFor(src.id),
                            contentDescription = null,
                            tint = GzColors.Gray700,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(src.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = GzColors.Ink)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                when {
                                    !src.enabled -> "Turned off"
                                    src.usedToday >= src.dailyCap -> "Cap reached — 0 min left today"
                                    else -> "${src.usedToday} of ${src.dailyCap} used · next +${src.minutesNow} min"
                                },
                                fontSize = 12.sp,
                                color = GzColors.Gray400
                            )
                        }
                        GzSwitch(checked = src.enabled, onCheckedChange = { vm.toggleRefillSource(src.id) })
                    }
                    if (index < state.refillSources.lastIndex) {
                        HorizontalDivider(color = GzColors.Border, thickness = 1.dp, modifier = Modifier.padding(start = 16.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            SectionLabel("Protection")
            Spacer(Modifier.height(8.dp))
            SettingsCard {
                SettingsRow(
                    "Pause protection",
                    when {
                        state.disabled -> "Disabled"
                        state.paused -> state.pausedLabel
                        else -> null
                    }
                ) { showPause = true }
            }

            Spacer(Modifier.height(24.dp))
            SectionLabel("Other")
            Spacer(Modifier.height(8.dp))
            SettingsCard {
                InfoRow("Notifications", "Quiet by default. One notice when your quota runs out — nothing else.")
                HorizontalDivider(color = GzColors.Border, thickness = 1.dp, modifier = Modifier.padding(start = 16.dp))
                InfoRow("Privacy", "All data stays on this device. Nothing is uploaded, ever.")
                HorizontalDivider(color = GzColors.Border, thickness = 1.dp, modifier = Modifier.padding(start = 16.dp))
                InfoRow("Data", "Usage history is stored locally and can be erased at any time.")
                HorizontalDivider(color = GzColors.Border, thickness = 1.dp, modifier = Modifier.padding(start = 16.dp))
                InfoRow("About", "Grayzone 1.0 — prototype.")
            }
        }
    }

    if (showAllowance) {
        ModalBottomSheet(
            onDismissRequest = { showAllowance = false },
            containerColor = GzColors.White
        ) {
            Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 36.dp)) {
                Text("Daily allowance", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = GzColors.Ink)
                Spacer(Modifier.height(18.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AllowanceOptions.forEach { option ->
                        GzChip("$option min", state.allowanceMin == option) { vm.setAllowance(option) }
                    }
                }
                Spacer(Modifier.height(22.dp))
                PrimaryButton("Done") { showAllowance = false }
            }
        }
    }

    if (showPause) {
        PauseSheet(vm) { showPause = false }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PauseSheet(vm: GrayzoneViewModel, onDismiss: () -> Unit) {
    var confirmed by remember { mutableStateOf<String?>(null) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = GzColors.White) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 36.dp)) {
            if (confirmed == null) {
                Text("Pause protection", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = GzColors.Ink)
                Spacer(Modifier.height(6.dp))
                Text(
                    "You can always pause. No questions asked.",
                    fontSize = 13.sp,
                    color = GzColors.Gray500
                )
                Spacer(Modifier.height(10.dp))
                PauseRow("15 minutes") {
                    val label = "until ${timeIn(15)}"
                    vm.pauseProtection(label); confirmed = "Protection paused $label."
                }
                PauseRow("1 hour") {
                    val label = "until ${timeIn(60)}"
                    vm.pauseProtection(label); confirmed = "Protection paused $label."
                }
                PauseRow("Until tomorrow") {
                    vm.pauseProtection("until tomorrow"); confirmed = "Protection paused until tomorrow."
                }
                PauseRow("Disable protection") {
                    vm.disableProtection(); confirmed = "Protection disabled."
                }
            } else {
                Text(confirmed!!, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = GzColors.Ink)
                Spacer(Modifier.height(24.dp))
                PrimaryButton("Resume now") {
                    vm.resumeProtection()
                    onDismiss()
                }
            }
        }
    }
}

@Composable
private fun PauseRow(title: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = GzColors.Ink, modifier = Modifier.weight(1f))
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = GzColors.Gray300, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun ThresholdRow(
    title: String,
    which: String,
    current: Int,
    options: List<Int>,
    vm: GrayzoneViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        SettingsRow(title, "$current%") { expanded = !expanded }
        if (expanded) {
            FlowRow(
                Modifier.padding(start = 16.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    GzChip("$option%", current == option) { vm.setThreshold(which, option) }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(title: String, detail: String) {
    var open by remember { mutableStateOf(false) }
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clickable { open = !open },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = GzColors.Ink, modifier = Modifier.weight(1f))
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = GzColors.Gray300,
                modifier = Modifier.size(20.dp).rotate(if (open) 90f else 0f)
            )
        }
        if (open) {
            Text(
                detail,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = GzColors.Gray500,
                modifier = Modifier.padding(start = 16.dp, bottom = 14.dp)
            )
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = GzColors.White,
        border = BorderStroke(1.dp, GzColors.Border)
    ) {
        Column { content() }
    }
}

private fun iconFor(id: String): ImageVector = when (id) {
    "walk" -> Icons.Outlined.DirectionsWalk
    "todo" -> Icons.Outlined.TaskAlt
    else -> Icons.Outlined.MenuBook
}

private fun timeIn(minutes: Int): String {
    val cal = Calendar.getInstance().apply { add(Calendar.MINUTE, minutes) }
    val h24 = cal.get(Calendar.HOUR_OF_DAY)
    val m = cal.get(Calendar.MINUTE)
    val ampm = if (h24 < 12) "AM" else "PM"
    val h = (h24 + 11) % 12 + 1
    return "$h:${m.toString().padStart(2, '0')} $ampm"
}
