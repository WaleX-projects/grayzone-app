package com.rork.grayzone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rork.grayzone.ui.GrayzoneViewModel
import com.rork.grayzone.ui.components.MainScaffold
import com.rork.grayzone.ui.components.SectionLabel
import com.rork.grayzone.ui.components.StatRow
import com.rork.grayzone.ui.components.UsageBar
import com.rork.grayzone.ui.components.fmtMin
import com.rork.grayzone.ui.theme.GzColors

@Composable
fun InsightsScreen(vm: GrayzoneViewModel, navigate: (String) -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    var range by rememberSaveable { mutableStateOf("day") }

    MainScaffold(selected = "insights", onSelect = navigate) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Text("Insights", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = GzColors.Ink)
            Spacer(Modifier.height(20.dp))
            Segmented(range) { range = it }
            Spacer(Modifier.height(26.dp))

            when (range) {
                "day" -> DayView(state.totalUsedTodayMin, state.apps.filter { it.isProtected })
                "week" -> PeriodView(
                    title = "This week",
                    total = 689,
                    bars = listOf("M" to 118, "T" to 96, "W" to 132, "T" to 88, "F" to 141, "S" to 64, "S" to 50)
                )
                else -> PeriodView(
                    title = "This month",
                    total = 2435,
                    bars = listOf("W1" to 620, "W2" to 545, "W3" to 690, "W4" to 580)
                )
            }

            Spacer(Modifier.height(28.dp))
            SectionLabel("Patterns")
            Spacer(Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = GzColors.Surface
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    val topApp = state.apps.filter { it.isProtected }.maxByOrNull { it.usedTodayMin }
                    StatRow("Most used", topApp?.name ?: "—")
                    StatRow("Most active time", "10 PM – 12 AM")
                    StatRow("Interrupted sessions", "${state.interruptedSessions}")
                    StatRow("Stopped after friction", "${state.stoppedAfterFriction}")
                    StatRow("Quota refilled today", "+${state.refilledTodayMin} min")
                }
            }
        }
    }
}

@Composable
private fun Segmented(selected: String, onSelect: (String) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(GzColors.SurfaceDeep)
    ) {
        listOf("day", "week", "month").forEach { r ->
            val isSel = r == selected
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSel) GzColors.Black else Color.Transparent)
                    .clickable { onSelect(r) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    r.replaceFirstChar { it.uppercase() },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSel) GzColors.White else GzColors.Gray500
                )
            }
        }
    }
}

@Composable
private fun DayView(totalUsed: Int, apps: List<com.rork.grayzone.ui.models.ProtectedApp>) {
    Text("Total usage today", fontSize = 13.sp, color = GzColors.Gray500)
    Text(fmtMin(totalUsed), fontSize = 36.sp, fontWeight = FontWeight.SemiBold, color = GzColors.Ink)
    Spacer(Modifier.height(24.dp))
    val sorted = apps.sortedByDescending { it.usedTodayMin }
    val max = sorted.maxOfOrNull { it.usedTodayMin } ?: 1
    sorted.forEach { app ->
        UsageBar(label = app.name, minutes = app.usedTodayMin, maxMinutes = max)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun PeriodView(title: String, total: Int, bars: List<Pair<String, Int>>) {
    Text(title, fontSize = 13.sp, color = GzColors.Gray500)
    Text(fmtMin(total), fontSize = 36.sp, fontWeight = FontWeight.SemiBold, color = GzColors.Ink)
    Spacer(Modifier.height(28.dp))
    VerticalBars(bars)
}

@Composable
private fun VerticalBars(data: List<Pair<String, Int>>) {
    val max = data.maxOf { it.second }
    Row(
        Modifier
            .fillMaxWidth()
            .height(170.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        data.forEach { (label, value) ->
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height((118f * value / max).dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(GzColors.Black)
                )
                Spacer(Modifier.height(8.dp))
                Text(label, fontSize = 11.sp, color = GzColors.Gray400)
            }
        }
    }
}


