package com.example.keyboardnmousegw.presentation.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.keyboardnmousegw.presentation.components.DeviceScanDialog

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

@SuppressLint("MissingPermission")
@Composable
fun ConfigurationTab(viewModel: SettingsViewModel) {
    val vibrationEnabled by viewModel.isVibrationEnabled.collectAsState()

    var showScanDialog by remember { mutableStateOf(false) }
    val scannedDevices by viewModel.scannedDevices.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    // Ambil data device yang sedang terkoneksi
    val connectedDevice by viewModel.connectedDevice.collectAsState()

    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        val deviceName = connectedDevice?.name ?: "None"
        Text("Active Device: $deviceName", style = MaterialTheme.typography.bodyLarge)

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                showScanDialog = true
                viewModel.startBluetoothScan()
            }
        ) {
            Text("New Device")
        }

        if (showScanDialog) {
            DeviceScanDialog(
                devices = scannedDevices.toList(),
                isScanning = isScanning,
                onDeviceClick = { device ->
                    viewModel.connectToDevice(device)
                    showScanDialog = false
                },
                onDismiss = {
                    viewModel.stopBluetoothScan()
                    showScanDialog = false
                }
            )
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