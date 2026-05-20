package com.example.keyboardnmousegw.presentation.main

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.bluetooth.BluetoothAdapter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.keyboardnmousegw.domain.models.HidKeycodes
import com.example.keyboardnmousegw.presentation.components.ActionButton
import com.example.keyboardnmousegw.presentation.components.HidKeyboardView
import com.example.keyboardnmousegw.presentation.components.TrackpadScrollbar
import com.example.keyboardnmousegw.presentation.settings.SettingsDrawer
import com.example.keyboardnmousegw.presentation.settings.SettingsViewModel
import com.example.keyboardnmousegw.presentation.settings.SettingsViewModelFactory
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    settingsViewModelFactory: SettingsViewModelFactory,
    mainViewModelFactory: MainViewModelFactory
) {
    var showSettings by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()
    val settingsViewModel: SettingsViewModel = viewModel(factory = settingsViewModelFactory)
    val mainViewModel: MainViewModel = viewModel(factory = mainViewModelFactory)

    val view = LocalView.current
    val window = (view.context as? Activity)?.window

    val isKeyboardOpen = WindowInsets.isImeVisible

    // [C-01] Referensi ke custom HID keyboard view untuk kontrol IME
    var hidKeyboardView by remember { mutableStateOf<HidKeyboardView?>(null) }
    
    val isBluetoothEnabled by mainViewModel.isBluetoothEnabled.collectAsState()
    
    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ -> 
        // Hasil akan ter-update otomatis dari BroadcastReceiver di BluetoothHidManager
    }

    LaunchedEffect(isFullscreen) {
        window?.let {
            val controller = WindowCompat.getInsetsController(it, view)
            if (isFullscreen) {
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { isFullscreen = !isFullscreen }) {
                    Icon(
                        imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Row {
                    // [C-01] Tombol keyboard — request focus pada HidKeyboardView dan tampilkan IME
                    IconButton(onClick = {
                        hidKeyboardView?.let { kbView ->
                            kbView.requestFocus()
                            val imm = kbView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.showSoftInput(kbView, InputMethodManager.SHOW_IMPLICIT)
                        }
                    }) {
                        Icon(Icons.Default.Keyboard, contentDescription = null, tint = Color.White)
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // [C-01] Custom IME View — invisible, menangkap input dari software keyboard
            AndroidView(
                factory = { ctx ->
                    HidKeyboardView(ctx).apply {
                        onCharInput = { char -> mainViewModel.sendKeyPress(char) }
                        onSpecialKey = { keyCode -> mainViewModel.sendSpecialKey(keyCode) }
                        hidKeyboardView = this
                    }
                },
                modifier = Modifier.size(1.dp)
            )

            // Banner peringatan jika Bluetooth mati
            if (!isBluetoothEnabled) {
                BluetoothDisabledBanner(
                    onEnableClick = {
                        val intent = android.content.Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        enableBluetoothLauncher.launch(intent)
                    }
                )
            }

            // 1. Area Trackpad
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 100.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0B4C75))
                    // GESTURE A: Pergerakan Mouse (1 Jari)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            mainViewModel.moveMouse(dragAmount.x, dragAmount.y)
                        }
                    }
                    // GESTURE B: Klik Kiri (Tap 1 Jari) — [M-03] Menggunakan clickLeft() dengan delay
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { mainViewModel.clickLeft() }
                        )
                    }
                    // C. Gesture 3: Multi-touch (Scroll Dua Jari & Klik Kanan Tap Dua Jari)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            var pressTime = 0L
                            var pressPosition1 = Offset.Zero
                            var pressPosition2 = Offset.Zero

                            while (true) {
                                val event = awaitPointerEvent()
                                val changes = event.changes
                                val pressedPointers = changes.filter { it.pressed }

                                when (event.type) {
                                    PointerEventType.Press -> {
                                        if (pressedPointers.size == 2) {
                                            pressTime = System.currentTimeMillis()
                                            pressPosition1 = pressedPointers[0].position
                                            pressPosition2 = pressedPointers[1].position
                                        // [L-02] Simplifikasi kondisi logika redundan
                                        } else if (pressedPointers.size != 2) {
                                            pressTime = 0L
                                        }
                                    }
                                    PointerEventType.Move -> {
                                        if (pressedPointers.size == 2) {
                                            // --- LOGIKA DETEKSI CANCEL TAP ---
                                            if (pressTime > 0L) {
                                                val dist1 = (pressedPointers[0].position - pressPosition1).getDistanceSquared()
                                                val dist2 = (pressedPointers[1].position - pressPosition2).getDistanceSquared()
                                                val slopSq = viewConfiguration.touchSlop * viewConfiguration.touchSlop

                                                if (dist1 > slopSq || dist2 > slopSq) {
                                                    pressTime = 0L
                                                }
                                            }

                                            // --- LOGIKA SCROLL ---
                                            val dy = pressedPointers.map { it.position.y - it.previousPosition.y }.average().toFloat()
                                            if (dy != 0f) {
                                                mainViewModel.scroll(dy)
                                                changes.forEach { it.consume() }
                                            }
                                        }
                                    }
                                    PointerEventType.Release -> {
                                        // --- LOGIKA DETEKSI TAP DUA JARI — [M-03] Menggunakan clickRight() ---
                                        if (pressTime > 0L) {
                                            val releaseTime = System.currentTimeMillis()
                                            val duration = releaseTime - pressTime

                                            if (duration < 300) {
                                                mainViewModel.clickRight()
                                                event.changes.forEach { it.consume() }
                                            }
                                        }
                                        pressTime = 0L
                                    }
                                }
                            }
                        }
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Mouse,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(if (isKeyboardOpen) 48.dp else 72.dp),
                    tint = Color.White.copy(alpha = 0.3f)
                )
                // Area Scrollbar di sisi kanan
                TrackpadScrollbar(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { change, dragAmount ->
                                change.consume()
                                mainViewModel.scroll(-dragAmount)
                            }
                        }
                )
            }

            // 2. Tombol Klik Fisik — [M-03] Menggunakan clickLeft()/clickRight() dengan delay
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    text = "Left Click",
                    modifier = Modifier.weight(1f).height(72.dp),
                    onClick = { mainViewModel.clickLeft() }
                )
                ActionButton(
                    text = "Right Click",
                    modifier = Modifier.weight(1f).height(72.dp),
                    onClick = { mainViewModel.clickRight() }
                )
            }

            // 3. Tombol Shortcut
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(text = "Undo", modifier = Modifier.weight(1f)) {
                    mainViewModel.sendShortcut(HidKeycodes.MOD_LEFT_CTRL, HidKeycodes.KEY_Z)
                }
                ActionButton(text = "Redo", modifier = Modifier.weight(1f)) {
                    mainViewModel.sendShortcut(HidKeycodes.MOD_LEFT_CTRL, HidKeycodes.KEY_Y)
                }
                ActionButton(text = "Copy", modifier = Modifier.weight(1f)) {
                    mainViewModel.sendShortcut(HidKeycodes.MOD_LEFT_CTRL, HidKeycodes.KEY_C)
                }
                ActionButton(text = "Paste", modifier = Modifier.weight(1f)) {
                    mainViewModel.sendShortcut(HidKeycodes.MOD_LEFT_CTRL, HidKeycodes.KEY_V)
                }
            }
        }

        // Settings Drawer
        if (showSettings) {
            SettingsDrawer(
                onDismiss = { showSettings = false },
                sheetState = sheetState,
                viewModel = settingsViewModel
            )
        }
    }
}

@Composable
fun BluetoothDisabledBanner(onEnableClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDE7E9)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.BluetoothDisabled,
                contentDescription = null,
                tint = Color(0xFFB3261E),
                modifier = Modifier.padding(top = 4.dp, end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bluetooth disabled",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFB3261E),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Enable Bluetooth to connect to a device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB3261E)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onEnableClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB3261E)),
                    border = BorderStroke(1.dp, Color(0xFFB3261E)),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Enable Bluetooth")
                }
            }
        }
    }
}