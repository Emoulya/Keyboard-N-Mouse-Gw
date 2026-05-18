package com.example.keyboardnmousegw.presentation.main

import android.app.Activity
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.keyboardnmousegw.presentation.components.ActionButton
import com.example.keyboardnmousegw.presentation.components.TrackpadScrollbar
import com.example.keyboardnmousegw.presentation.settings.SettingsDrawer
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.keyboardnmousegw.presentation.settings.SettingsViewModel
import com.example.keyboardnmousegw.presentation.settings.SettingsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainScreen(viewModelFactory: SettingsViewModelFactory) {
    var showSettings by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var keyboardInput by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val sheetState = rememberModalBottomSheetState()
    val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)

    val view = LocalView.current
    val window = (view.context as? Activity)?.window

    // Deteksi Keyboard
    val isKeyboardOpen = WindowInsets.isImeVisible

    // Fullscreen Logic
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
            // Hidden Keyboard Input
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

            // 1. Trackpad
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 100.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.Mouse,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(if (isKeyboardOpen) 48.dp else 72.dp),
                    tint = Color.White.copy(alpha = 0.3f)
                )
                TrackpadScrollbar(modifier = Modifier.align(Alignment.CenterEnd))
            }

            // 2. Click Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    text = "Left Click",
                    modifier = Modifier.weight(1f).height(72.dp)
                ) {}
                ActionButton(
                    text = "Right Click",
                    modifier = Modifier.weight(1f).height(72.dp)
                ) {}
            }

            // 3. Shortcut Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(text = "Undo", modifier = Modifier.weight(1f)) {}
                ActionButton(text = "Redo", modifier = Modifier.weight(1f)) {}
                ActionButton(text = "Copy", modifier = Modifier.weight(1f)) {}
                ActionButton(text = "Paste", modifier = Modifier.weight(1f)) {}
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
