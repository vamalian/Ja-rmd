package com.example.ui

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.InterfaceMode
import com.example.ui.screens.CapabilitiesScreen
import com.example.ui.screens.JarvisMobileScreen
import com.example.ui.screens.JarvisTabletScreen
import com.example.ui.screens.SettingsScreen
import com.example.viewmodel.JarvisViewModel

enum class CurrentScreen {
    HOME,
    SETTINGS,
    CAPABILITIES
}

@Composable
fun JarvisMainContainer(
    viewModel: JarvisViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(CurrentScreen.HOME) }
    val settings by viewModel.settings.collectAsState()

    // Permission launcher for Mic and Contacts
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshCapabilities()
    }

    LaunchedEffect(Unit) {
        // Request basic permissions on launch if not granted
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_CONTACTS
            )
        )
    }

    BackHandler(enabled = currentScreen != CurrentScreen.HOME) {
        currentScreen = CurrentScreen.HOME
    }

    when (currentScreen) {
        CurrentScreen.SETTINGS -> {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { currentScreen = CurrentScreen.HOME },
                modifier = modifier
            )
        }
        CurrentScreen.CAPABILITIES -> {
            CapabilitiesScreen(
                viewModel = viewModel,
                onNavigateBack = { currentScreen = CurrentScreen.HOME },
                modifier = modifier
            )
        }
        CurrentScreen.HOME -> {
            BoxWithConstraints(modifier = modifier.fillMaxSize()) {
                val isWideScreen = maxWidth >= 600.dp
                val activeMode = when (settings.interfaceMode) {
                    InterfaceMode.MOBILE -> InterfaceMode.MOBILE
                    InterfaceMode.TABLET -> InterfaceMode.TABLET
                    InterfaceMode.AUTOMATIC -> if (isWideScreen) InterfaceMode.TABLET else InterfaceMode.MOBILE
                }

                if (activeMode == InterfaceMode.TABLET) {
                    JarvisTabletScreen(
                        viewModel = viewModel,
                        onOpenSettings = { currentScreen = CurrentScreen.SETTINGS },
                        onOpenCapabilities = { currentScreen = CurrentScreen.CAPABILITIES }
                    )
                } else {
                    JarvisMobileScreen(
                        viewModel = viewModel,
                        onOpenSettings = { currentScreen = CurrentScreen.SETTINGS },
                        onOpenCapabilities = { currentScreen = CurrentScreen.CAPABILITIES }
                    )
                }
            }
        }
    }
}
