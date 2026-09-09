package com.rork.grayzone.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rork.grayzone.ui.GrayzoneViewModel
import com.rork.grayzone.ui.components.GzChip
import com.rork.grayzone.ui.components.GzSwitch
import com.rork.grayzone.ui.components.MainScaffold
import com.rork.grayzone.ui.models.ProtectedApp
import com.rork.grayzone.ui.theme.GzColors

private val LimitOptions = listOf(30, 45, 60, 90, 120)

@Composable
fun AppsScreen(vm: GrayzoneViewModel, navigate: (String) -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    var adding by remember { mutableStateOf(false) }

    MainScaffold(selected = "apps", onSelect = navigate) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Text("Protected Apps", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = GzColors.Ink)
            Spacer(Modifier.height(6.dp))
            Text(
                "Protected apps draw from your shared daily quota.",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = GzColors.Gray500
            )
            Spacer(Modifier.height(22.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = GzColors.White,
                border = BorderStroke(1.dp, GzColors.Border)
            ) {
                Column {
                    state.apps.forEachIndexed { index, app ->
                        AppRow(app, vm)
                        if (index < state.apps.lastIndex) {
                            HorizontalDivider(color = GzColors.Border, thickness = 1.dp, modifier = Modifier.padding(start = 16.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable { adding = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, tint = GzColors.Ink, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Add app", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = GzColors.Ink)
            }
        }
    }

    if (adding) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { adding = false },
            containerColor = GzColors.White,
            title = { Text("Add app", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = GzColors.Ink) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    placeholder = { Text("App name", color = GzColors.Gray400) }
                )
            },
            confirmButton = {
                TextButton(
                    enabled = name.isNotBlank(),
                    onClick = { vm.addApp(name.trim()); adding = false }
                ) { Text("Add", color = GzColors.Ink, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { adding = false }) { Text("Cancel", color = GzColors.Gray500) }
            }
        )
    }
}

@Composable
private fun AppRow(app: ProtectedApp, vm: GrayzoneViewModel) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 62.dp)
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(app.name, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = GzColors.Ink)
                Spacer(Modifier.height(2.dp))
                Text(
                    if (app.isProtected) "${app.usedTodayMin} min today · limit ${app.dailyLimitMin} min"
                    else "Protection off",
                    fontSize = 12.sp,
                    color = GzColors.Gray400
                )
            }
            GzSwitch(checked = app.isProtected, onCheckedChange = { vm.toggleProtected(app.id) })
        }
        AnimatedVisibility(expanded && app.isProtected) {
            Row(
                Modifier.padding(start = 16.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LimitOptions.forEach { limit ->
                    GzChip("$limit", app.dailyLimitMin == limit) { vm.setAppLimit(app.id, limit) }
                }
            }
        }
    }
}
