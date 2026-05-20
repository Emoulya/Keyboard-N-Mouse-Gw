package com.example.keyboardnmousegw.presentation.main

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.keyboardnmousegw.domain.models.HidKeycodes
import com.example.keyboardnmousegw.presentation.components.ClickButton
import com.example.keyboardnmousegw.presentation.components.HidKeyboardView
import com.example.keyboardnmousegw.presentation.components.ShortcutButton
import com.example.keyboardnmousegw.presentation.components.TrackpadScrollbar
import com.example.keyboardnmousegw.presentation.settings.SettingsDrawer
import com.example.keyboardnmousegw.presentation.settings.SettingsViewModel
import com.example.keyboardnmousegw.presentation.settings.SettingsViewModelFactory
import com.example.keyboardnmousegw.presentation.theme.*

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

    var hidKeyboardView by remember { mutableStateOf<HidKeyboardView?>(null) }

    val isBluetoothEnabled by mainViewModel.isBluetoothEnabled.collectAsState()

    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ -> }

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
        containerColor = AppBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { isFullscreen = !isFullscreen }) {
                    Icon(
                        imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = null,
                        tint = TextMuted
                    )
                }
                Row {
                    IconButton(onClick = {
                        hidKeyboardView?.let { kbView ->
                            kbView.requestFocus()
                            val imm = kbView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.showSoftInput(kbView, InputMethodManager.SHOW_IMPLICIT)
                        }
                    }) {
                        Icon(Icons.Default.Keyboard, contentDescription = null, tint = TextMuted)
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = TextMuted)
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // IME View invisible
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

            // Banner Bluetooth mati
            if (!isBluetoothEnabled) {
                BluetoothDisabledBanner(
                    onEnableClick = {
                        val intent = android.content.Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        enableBluetoothLauncher.launch(intent)
                    }
                )
            }

            // ========================================
            // AREA TRACKPAD — Gradient + Glassmorphism
            // ========================================
            val trackpadShape = RoundedCornerShape(24.dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 100.dp)
                    .fillMaxWidth()
                    .clip(trackpadShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(TrackpadGradientStart, TrackpadGradientEnd)
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                PrimaryViolet.copy(alpha = 0.3f),
                                PrimaryViolet.copy(alpha = 0.08f)
                            )
                        ),
                        shape = trackpadShape
                    )
                    // GESTURE A: Pergerakan Mouse
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            mainViewModel.moveMouse(dragAmount.x, dragAmount.y)
                        }
                    }
                    // GESTURE B: Tap = Klik Kiri
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { mainViewModel.clickLeft() }
                        )
                    }
                    // GESTURE C: Multi-touch (Scroll + Klik Kanan)
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
                                        } else if (pressedPointers.size != 2) {
                                            pressTime = 0L
                                        }
                                    }
                                    PointerEventType.Move -> {
                                        if (pressedPointers.size == 2) {
                                            if (pressTime > 0L) {
                                                val dist1 = (pressedPointers[0].position - pressPosition1).getDistanceSquared()
                                                val dist2 = (pressedPointers[1].position - pressPosition2).getDistanceSquared()
                                                val slopSq = viewConfiguration.touchSlop * viewConfiguration.touchSlop
                                                if (dist1 > slopSq || dist2 > slopSq) {
                                                    pressTime = 0L
                                                }
                                            }
                                            val dy = pressedPointers.map { it.position.y - it.previousPosition.y }.average().toFloat()
                                            if (dy != 0f) {
                                                mainViewModel.scroll(dy)
                                                changes.forEach { it.consume() }
                                            }
                                        }
                                    }
                                    PointerEventType.Release -> {
                                        if (pressTime > 0L) {
                                            val duration = System.currentTimeMillis() - pressTime
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
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TOUCH AREA",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = if (isKeyboardOpen) 12.sp else 14.sp,
                        letterSpacing = 4.sp,
                        color = TextMuted.copy(alpha = 0.25f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "tap  ·  double-tap  ·  swipe",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = if (isKeyboardOpen) 10.sp else 11.sp,
                        letterSpacing = 1.sp,
                        color = TextMuted.copy(alpha = 0.18f)
                    )
                }
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

            // ========================================
            // TOMBOL KLIK
            // ========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ClickButton(
                    text = "Left Click",
                    icon = Icons.Default.TouchApp,
                    isLeftClick = true,
                    modifier = Modifier.weight(1f).height(72.dp),
                    onClick = { mainViewModel.clickLeft() }
                )
                ClickButton(
                    text = "Right Click",
                    icon = Icons.Default.Mouse,
                    isLeftClick = false,
                    modifier = Modifier.weight(1f).height(72.dp),
                    onClick = { mainViewModel.clickRight() }
                )
            }

            // ========================================
            // TOMBOL SHORTCUT
            // ========================================
            Text(
                text = "SHORTCUTS",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
                modifier = Modifier.padding(start = 2.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShortcutButton(
                    text = "Cut",
                    icon = Icons.Default.ContentCut,
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    mainViewModel.sendShortcut(HidKeycodes.MOD_LEFT_CTRL, HidKeycodes.KEY_X)
                }
                ShortcutButton(
                    text = "Copy",
                    icon = Icons.Default.ContentCopy,
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    mainViewModel.sendShortcut(HidKeycodes.MOD_LEFT_CTRL, HidKeycodes.KEY_C)
                }
                ShortcutButton(
                    text = "Paste",
                    icon = Icons.Default.ContentPaste,
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    mainViewModel.sendShortcut(HidKeycodes.MOD_LEFT_CTRL, HidKeycodes.KEY_V)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShortcutButton(
                    text = "Undo",
                    icon = Icons.AutoMirrored.Filled.Undo,
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    mainViewModel.sendShortcut(HidKeycodes.MOD_LEFT_CTRL, HidKeycodes.KEY_Z)
                }
                ShortcutButton(
                    text = "Redo",
                    icon = Icons.AutoMirrored.Filled.Redo,
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    mainViewModel.sendShortcut(HidKeycodes.MOD_LEFT_CTRL, HidKeycodes.KEY_Y)
                }
                ShortcutButton(
                    text = "Sel All",
                    icon = Icons.Default.SelectAll,
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    mainViewModel.sendShortcut(HidKeycodes.MOD_LEFT_CTRL, HidKeycodes.KEY_A)
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

// ========================================
// BLUETOOTH DISABLED BANNER
// ========================================
@Composable
fun BluetoothDisabledBanner(onEnableClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.BluetoothDisabled,
                contentDescription = null,
                tint = ErrorRed,
                modifier = Modifier.padding(top = 4.dp, end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bluetooth disabled",
                    style = MaterialTheme.typography.titleMedium,
                    color = ErrorRed,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Enable Bluetooth to connect to a device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ErrorRed.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onEnableClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                    border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f)),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Enable Bluetooth")
                }
            }
        }
    }
}