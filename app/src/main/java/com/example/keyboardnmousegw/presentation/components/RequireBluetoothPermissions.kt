package com.example.keyboardnmousegw.presentation.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.keyboardnmousegw.presentation.theme.*

/**
 * Komponen Reusable (DRY) untuk menangani izin Bluetooth.
 * Hanya me-render 'content' jika semua izin yang dibutuhkan telah diberikan.
 */
@Composable
fun RequireBluetoothPermissions(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    var permissionsGranted by remember {
        mutableStateOf(
            permissionsToRequest.all { permission ->
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions.values.all { it }
    }

    if (permissionsGranted) {
        content()
    } else {
        // Layar Fallback — Violet themed
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.BluetoothSearching,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = PrimaryViolet
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Bluetooth Permission Required",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Aplikasi ini membutuhkan izin Bluetooth untuk dapat berfungsi sebagai Keyboard dan Mouse.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { permissionLauncher.launch(permissionsToRequest) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryViolet,
                    contentColor = TextWhite
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    "Berikan Izin Bluetooth",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}