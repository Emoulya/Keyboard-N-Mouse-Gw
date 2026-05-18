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
    sheetState: SheetState,
    viewModel: SettingsViewModel
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        var selectedTab by remember { mutableIntStateOf(0) }
        val tabs = listOf("Configuration", "Mouse")

        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.tertiary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Meneruskan viewModel ke tab masing-masing
            when (selectedTab) {
                0 -> ConfigurationTab(viewModel)
                1 -> MouseTab(viewModel)
            }
        }
    }
}

@Composable
fun ConfigurationTab(viewModel: SettingsViewModel) {
    val vibrationEnabled by viewModel.isVibrationEnabled.collectAsState()

    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        Text("Active Device: None", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        Button(onClick = { /* TODO: Open Bluetooth Dialog */ }) {
            Text("New Device")
        }

        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Touch Vibration")
            Switch(
                checked = vibrationEnabled,
                onCheckedChange = { viewModel.updateVibration(it) }
            )
        }
    }
}

@Composable
fun MouseTab(viewModel: SettingsViewModel) {
    val pointerSpeed by viewModel.pointerSpeed.collectAsState()
    val scrollSpeed by viewModel.scrollSpeed.collectAsState()

    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        Text("Pointer Speed")
        Slider(
            value = pointerSpeed,
            onValueChange = { viewModel.updatePointerSpeed(it) }
        )

        Spacer(Modifier.height(16.dp))

        Text("Scroll Bar Speed")
        Slider(
            value = scrollSpeed,
            onValueChange = { viewModel.updateScrollSpeed(it) }
        )
    }
}