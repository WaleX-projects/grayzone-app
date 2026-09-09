package com.rork.grayzone.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rork.grayzone.ui.theme.GzColors

private val TabRoutes = listOf("home", "apps", "insights", "settings")

@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GzColors.Black,
            contentColor = GzColors.White,
            disabledContainerColor = GzColors.Gray300,
            disabledContentColor = GzColors.White
        )
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = GzColors.White,
            contentColor = GzColors.Ink,
            disabledContainerColor = GzColors.White,
            disabledContentColor = GzColors.Gray300
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, GzColors.Gray300)
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun GhostTextButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick, modifier = modifier) {
        Text(text, fontSize = 14.sp, color = GzColors.Gray500, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        fontSize = 12.sp,
        letterSpacing = 1.4.sp,
        fontWeight = FontWeight.Medium,
        color = GzColors.Gray500
    )
}

@Composable
fun GzSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = GzColors.White,
            checkedTrackColor = GzColors.Black,
            checkedBorderColor = GzColors.Black,
            uncheckedThumbColor = GzColors.White,
            uncheckedTrackColor = GzColors.Gray300,
            uncheckedBorderColor = GzColors.Gray300
        )
    )
}

@Composable
fun GzChip(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) GzColors.Black else GzColors.Surface)
            .border(1.dp, if (selected) GzColors.Black else GzColors.Border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) GzColors.White else GzColors.Ink
        )
    }
}

@Composable
fun RefillChip(minutes: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = GzColors.SurfaceDeep
    ) {
        Text(
            "+$minutes min",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = GzColors.Gray700
        )
    }
}

/** The quota ring — reads as "how much do I have left". */
@Composable
fun UsageRing(
    percent: Float,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val stroke = 14.dp.toPx()
            val inset = stroke / 2 + 2.dp.toPx()
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
            drawArc(
                color = GzColors.Track,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
            val sweep = 360f * percent.coerceIn(0f, 1f)
            if (sweep > 0f) {
                drawArc(
                    color = GzColors.Black,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, content = content)
    }
}

@Composable
fun UsageBar(label: String, minutes: Int, maxMinutes: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = GzColors.Ink)
            Spacer(Modifier.weight(1f))
            Text(fmtMin(minutes), fontSize = 14.sp, color = GzColors.Gray500)
        }
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(GzColors.Track)
        ) {
            Box(
                Modifier
                    .fillMaxWidth((minutes.toFloat() / maxMinutes.coerceAtLeast(1)).coerceIn(0.02f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(GzColors.Black)
            )
        }
    }
}

@Composable
fun StatRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text(label, fontSize = 14.sp, color = GzColors.Gray700)
        Spacer(Modifier.weight(1f))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = GzColors.Ink)
    }
}

@Composable
fun SettingsRow(
    title: String,
    value: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = GzColors.Ink, modifier = Modifier.weight(1f))
        if (value != null) {
            Text(value, fontSize = 14.sp, color = GzColors.Gray500)
            Spacer(Modifier.width(6.dp))
        }
        Icon(
            Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = GzColors.Gray300,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun MainScaffold(
    selected: String,
    onSelect: (String) -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Column(Modifier.fillMaxSize().background(GzColors.White)) {
        Box(Modifier.weight(1f)) { content() }
        HorizontalDivider(color = GzColors.Border, thickness = 1.dp)
        GzBottomBar(selected = selected, onSelect = onSelect)
    }
}

private data class Tab(val route: String, val label: String, val icon: ImageVector, val active: ImageVector)

@Composable
fun GzBottomBar(selected: String, onSelect: (String) -> Unit) {
    val tabs = listOf(
        Tab("home", "Home", Icons.Outlined.Home, Icons.Filled.Home),
        Tab("apps", "Apps", Icons.Outlined.GridView, Icons.Filled.GridView),
        Tab("insights", "Insights", Icons.Outlined.BarChart, Icons.Filled.BarChart),
        Tab("settings", "Settings", Icons.Outlined.Settings, Icons.Filled.Settings)
    )
    Surface(color = GzColors.White) {
        Row(Modifier.fillMaxWidth().navigationBarsPadding().height(64.dp)) {
            tabs.forEach { tab ->
                val isSel = tab.route == selected
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onSelect(tab.route) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        if (isSel) tab.active else tab.icon,
                        contentDescription = tab.label,
                        tint = if (isSel) GzColors.Ink else GzColors.Gray400,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        tab.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSel) GzColors.Ink else GzColors.Gray400
                    )
                }
            }
        }
    }
}

fun fmtMin(minutes: Int): String =
    if (minutes >= 60) "${minutes / 60}h ${minutes % 60}m" else "${minutes}m"

