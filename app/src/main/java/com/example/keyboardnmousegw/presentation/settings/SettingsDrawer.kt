package com.example.keyboardnmousegw.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDrawer(
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.tertiary
    ) {
        var selectedTab by remember { mutableIntStateOf(0) }
        val tabs = listOf("Configuration", "Mouse")

        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color.Cyan
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> ConfigurationTab()
                1 -> MouseTab()
            }
        }
    }
}

@Composable
fun ConfigurationTab() {
    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        Text("Active Device: None", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        Button(onClick = { /* Open Bluetooth Dialog */ }) {
            Text("New Device")
        }

        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Touch Vibration")
            var vibrationEnabled by remember { mutableStateOf(true) }
            Switch(checked = vibrationEnabled, onCheckedChange = { vibrationEnabled = it })
        }
    }
}

@Composable
fun MouseTab() {
    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        Text("Pointer Speed")
        var pointerSpeed by remember { mutableFloatStateOf(0.5f) }
        Slider(value = pointerSpeed, onValueChange = { pointerSpeed = it })

        Spacer(Modifier.height(16.dp))

        Text("Scroll Bar Speed")
        var scrollSpeed by remember { mutableFloatStateOf(0.5f) }
        Slider(value = scrollSpeed, onValueChange = { scrollSpeed = it })
    }
}
