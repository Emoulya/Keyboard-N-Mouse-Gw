package com.example.keyboardnmousegw.presentation.components

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@SuppressLint("MissingPermission")
@Composable
fun DeviceScanDialog(
    devices: List<BluetoothDevice>,
    isScanning: Boolean,
    onDeviceClick: (BluetoothDevice) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Scan Devices")
                if (isScanning) {
                    Spacer(Modifier.width(12.dp))
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            }
        },
        text = {
            if (devices.isEmpty() && !isScanning) {
                Text("No devices found. Ensure your PC/Laptop Bluetooth is discoverable.")
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(devices) { device ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDeviceClick(device) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Computer, contentDescription = null)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(device.name ?: "Unknown Device", style = MaterialTheme.typography.bodyLarge)
                                Text(device.address, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}