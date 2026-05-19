package com.example.keyboardnmousegw.presentation.main

import android.app.Activity
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.keyboardnmousegw.domain.models.HidKeycodes
import com.example.keyboardnmousegw.presentation.components.ActionButton
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
    var keyboardInput by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val sheetState = rememberModalBottomSheetState()
    val settingsViewModel: SettingsViewModel = viewModel(factory = settingsViewModelFactory)
    val mainViewModel: MainViewModel = viewModel(factory = mainViewModelFactory)

    val view = LocalView.current
    val window = (view.context as? Activity)?.window

    val isKeyboardOpen = WindowInsets.isImeVisible

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
                    IconButton(onClick = {
                        focusRequester.requestFocus()
                        keyboardController?.show()
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
            TextField(
                value = keyboardInput,
                onValueChange = { keyboardInput = it },
                modifier = Modifier
                    .size(1.dp)
                    .alpha(0f)
                    .focusRequester(focusRequester),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

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
                    // GESTURE B: Klik Kiri (Tap 1 Jari)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                mainViewModel.setLeftClick(true)
                                mainViewModel.setLeftClick(false)
                            }
                        )
                    }
                    // C. Gesture 3: Multi-touch (Sroll Dua Jari & Klik Kanan Tap Dua Jari)
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
                                        } else if (pressedPointers.size > 2 || pressedPointers.size < 2) {
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
                                        // --- LOGIKA DETEKSI TAP DUA JARI ---
                                        if (pressTime > 0L) {
                                            val releaseTime = System.currentTimeMillis()
                                            val duration = releaseTime - pressTime

                                            // Jika durasi sentuhan singkat (misal < 300ms) -> Tap Dua Jari
                                            if (duration < 300) {
                                                mainViewModel.setRightClick(true)
                                                mainViewModel.setRightClick(false)

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

            // 2. Tombol Klik Fisik
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    text = "Left Click",
                    modifier = Modifier.weight(1f).height(72.dp),
                    onClick = {
                        mainViewModel.setLeftClick(true)
                        mainViewModel.setLeftClick(false)
                    }
                )
                ActionButton(
                    text = "Right Click",
                    modifier = Modifier.weight(1f).height(72.dp),
                    onClick = {
                        mainViewModel.setRightClick(true)
                        mainViewModel.setRightClick(false)
                    }
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