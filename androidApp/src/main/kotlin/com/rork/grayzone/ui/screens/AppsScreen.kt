package com.rork.grayzone.ui.screens

import android.content.Context
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rork.grayzone.ui.GrayzoneViewModel
import com.rork.grayzone.ui.components.GzChip
import com.rork.grayzone.ui.components.GzSwitch
import com.rork.grayzone.ui.components.MainScaffold
import com.rork.grayzone.ui.components.PrimaryButton
import com.rork.grayzone.ui.models.ProtectedApp
import com.rork.grayzone.ui.theme.GzColors
import com.rork.grayzone.util.AppUtils
import com.rork.grayzone.util.InstalledApp
import kotlinx.coroutines.launch

private val LimitOptions = listOf(30, 45, 60, 90, 120)

@Composable
fun AppsScreen(vm: GrayzoneViewModel, navigate: (String) -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var adding by remember { mutableStateOf(false) }
    var availableApps by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    var isLoadingApps by remember { mutableStateOf(true) }

    // Load available apps on first composition
    LaunchedEffect(Unit) {
        scope.launch {
            val apps = AppUtils.getInstalledApps(context, includeSystemApps = false)
            val filtered = AppUtils.filterRelevantApps(apps)
            availableApps = filtered
            isLoadingApps = false
        }
    }

    MainScaffold(selected = "apps", onSelect = navigate) {
        Column(
            Modifier
                .fillMaxSize()
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

            if (state.apps.isNotEmpty()) {
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

                Spacer(Modifier.height(16.dp))
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable { adding = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, tint = GzColors.Ink, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Add monitored app", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = GzColors.Ink)
            }

            if (state.apps.isEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "No apps are being monitored yet. Add one to get started.",
                    fontSize = 14.sp,
                    color = GzColors.Gray500,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }

    if (adding) {
        AddAppDialog(
            context = context,
            availableApps = availableApps,
            isLoading = isLoadingApps,
            onAdd = { packageName, displayName, dailyLimit, threshold ->
                scope.launch {
                    vm.addAppToMonitoring(
                        packageName = packageName,
                        displayName = displayName,
                        dailyLimitMinutes = dailyLimit,
                        interventionThresholdMinutes = threshold
                    )
                    adding = false
                }
            },
            onDismiss = { adding = false }
        )
    }
}

@Composable
private fun AddAppDialog(
    context: Context,
    availableApps: List<InstalledApp>,
    isLoading: Boolean,
    onAdd: (packageName: String, displayName: String, dailyLimit: Int, threshold: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedApp by remember { mutableStateOf<InstalledApp?>(null) }
    var dailyLimit by remember { mutableStateOf(60) }
    var threshold by remember { mutableStateOf(18) }
    var showAppList by remember { mutableStateOf(false) }

    if (!showAppList) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            containerColor = GzColors.White,
            title = { 
                Text(
                    if (selectedApp == null) "Select app" else "Configure ${selectedApp!!.displayName}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GzColors.Ink
                )
            },
            text = {
                Column {
                    if (selectedApp == null) {
                        Text(
                            "Choose from popular apps or search installed apps.",
                            fontSize = 13.sp,
                            color = GzColors.Gray500,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        TextButton(onClick = { showAppList = true }) {
                            Text("Browse installed apps", color = GzColors.Ink, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Column {
                            Text("Daily limit (minutes)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GzColors.Gray500)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(30, 45, 60, 90, 120).forEach { limit ->
                                    GzChip(limit.toString(), dailyLimit == limit) { dailyLimit = limit }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Text("Intervention threshold (minutes)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GzColors.Gray500)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(10, 15, 18, 25, 30).forEach { t ->
                                    GzChip(t.toString(), threshold == t) { threshold = t }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (selectedApp != null) {
                    TextButton(
                        onClick = {
                            onAdd(
                                selectedApp!!.packageName,
                                selectedApp!!.displayName,
                                dailyLimit,
                                threshold
                            )
                        }
                    ) {
                        Text("Add", color = GzColors.Ink, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    if (selectedApp != null) {
                        selectedApp = null
                    } else {
                        onDismiss()
                    }
                }) {
                    Text(if (selectedApp != null) "Back" else "Cancel", color = GzColors.Gray500)
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = { showAppList = false },
            containerColor = GzColors.White,
            title = {
                Text(
                    "Installed apps",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GzColors.Ink
                )
            },
            text = {
                if (isLoading) {
                    Text("Loading apps...", color = GzColors.Gray500)
                } else if (availableApps.isEmpty()) {
                    Text("No apps found.", color = GzColors.Gray500)
                } else {
                    LazyColumn(modifier = Modifier.heightIn(maxHeight = 300.dp)) {
                        items(availableApps) { app ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedApp = app
                                        showAppList = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(app.displayName, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = GzColors.Ink)
                                    Text(app.packageName, fontSize = 11.sp, color = GzColors.Gray400)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAppList = false }) {
                    Text("Close", color = GzColors.Gray500)
                }
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
