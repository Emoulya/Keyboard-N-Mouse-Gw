package com.example.keyboardnmousegw.presentation.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.keyboardnmousegw.presentation.components.DeviceScanDialog
import com.example.keyboardnmousegw.presentation.theme.*

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
        containerColor = SurfaceDim,
        contentColor = TextWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            // Handle indicator kustom
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .padding(horizontal = 0.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(2.dp),
                    color = TextMuted.copy(alpha = 0.4f)
                ) {}
            }
        }
    ) {
        var selectedTab by remember { mutableIntStateOf(0) }
        val tabs = listOf("Configuration", "Mouse")

        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceDim,
                contentColor = AccentLavender
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                color = if (selectedTab == index) AccentLavender else TextMuted
                            )
                        }
                    )
                }
            }

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
    val connectedDevice by viewModel.connectedDevice.collectAsState()

    Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
        // Section: Device
        Text(
            "DEVICE",
            style = MaterialTheme.typography.labelMedium,
            color = TextMuted
        )
        Spacer(Modifier.height(12.dp))

        val deviceName = connectedDevice?.name ?: "Not connected"
        Text(deviceName, style = MaterialTheme.typography.bodyLarge, color = TextWhite)

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                showScanDialog = true
                viewModel.startBluetoothScan()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryViolet,
                contentColor = TextWhite
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Scan Devices")
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
        HorizontalDivider(color = TextMuted.copy(alpha = 0.15f))
        Spacer(Modifier.height(16.dp))

        // Section: Haptic
        Text(
            "HAPTIC FEEDBACK",
            style = MaterialTheme.typography.labelMedium,
            color = TextMuted
        )
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Touch Vibration", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = vibrationEnabled,
                onCheckedChange = { viewModel.updateVibration(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TextWhite,
                    checkedTrackColor = PrimaryViolet,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = SurfaceContainer
                )
            )
        }
    }
}

@Composable
fun MouseTab(viewModel: SettingsViewModel) {
    val pointerSpeed by viewModel.pointerSpeed.collectAsState()
    val scrollSpeed by viewModel.scrollSpeed.collectAsState()

    val sliderColors = SliderDefaults.colors(
        thumbColor = PrimaryViolet,
        activeTrackColor = PrimaryViolet,
        inactiveTrackColor = SurfaceContainer
    )

    Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
        // Pointer Speed
        Text(
            "POINTER SPEED",
            style = MaterialTheme.typography.labelMedium,
            color = TextMuted
        )
        Spacer(Modifier.height(4.dp))
        Slider(
            value = pointerSpeed,
            onValueChange = { viewModel.updatePointerSpeed(it) },
            colors = sliderColors
        )

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = TextMuted.copy(alpha = 0.15f))
        Spacer(Modifier.height(16.dp))

        // Scroll Speed
        Text(
            "SCROLL SPEED",
            style = MaterialTheme.typography.labelMedium,
            color = TextMuted
        )
        Spacer(Modifier.height(4.dp))
        Slider(
            value = scrollSpeed,
            onValueChange = { viewModel.updateScrollSpeed(it) },
            colors = sliderColors
        )
    }
}