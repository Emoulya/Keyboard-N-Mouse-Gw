package com.example.keyboardnmousegw.presentation.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Komponen Reusable (DRY) untuk menangani izin Bluetooth.
 * Hanya me-render 'content' jika semua izin yang dibutuhkan telah diberikan.
 */
@Composable
fun RequireBluetoothPermissions(
    content: @Composable () -> Unit
) {
    // Menentukan daftar izin berdasarkan versi Android
    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )
    } else {
        // Untuk Android 11 ke bawah, izin Bluetooth diberikan saat instalasi,
        // namun butuh izin lokasi untuk melakukan scanning perangkat baru.
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    var permissionsGranted by remember { mutableStateOf(false) }

    // Launcher untuk memunculkan dialog pop-up bawaan Android
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Cek apakah SEMUA izin yang diminta bernilai true (diizinkan)
        permissionsGranted = permissions.values.all { it }
    }

    // Jika izin sudah diberikan, langsung jalankan konten utama (MainScreen)
    if (permissionsGranted) {
        content()
    } else {
        // Layar Fallback (Security UX) jika izin belum diberikan
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Aplikasi ini membutuhkan izin Bluetooth untuk dapat berfungsi sebagai Keyboard dan Mouse.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    permissionLauncher.launch(permissionsToRequest)
                }
            ) {
                Text("Berikan Izin Bluetooth")
            }
        }
    }
}