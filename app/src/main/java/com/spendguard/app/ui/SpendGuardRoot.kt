package com.spendguard.app.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.spendguard.app.ui.screens.HomeScreen
import com.spendguard.app.ui.screens.OnboardingScreen
import com.spendguard.app.ui.screens.SettingsScreen

@Composable
fun SpendGuardRoot(vm: SpendViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        vm.refreshListenerEnabled(
            NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName),
        )
    }

    if (!state.settings.onboardingDone) {
        OnboardingScreen(
            captureEnabled = state.listenerEnabled,
            onOpenCaptureSettings = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            },
            onContinue = {
                vm.refreshListenerEnabled(
                    NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName),
                )
                vm.completeOnboarding()
            },
        )
        return
    }

    val nav = rememberNavController()
    val route = nav.currentBackStackEntryAsState().value?.destination?.route
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = route == "home",
                    onClick = { nav.navigate("home") { popUpTo("home") } },
                    icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                )
                NavigationBarItem(
                    selected = route == "settings",
                    onClick = { nav.navigate("settings") },
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                )
            }
        },
    ) { padding ->
        NavHost(navController = nav, startDestination = "home", modifier = Modifier.padding(padding)) {
            composable("home") {
                HomeScreen(
                    state = state,
                    onAddManual = vm::addManual,
                    onDelete = vm::delete,
                )
            }
            composable("settings") {
                SettingsScreen(
                    state = state,
                    firebaseReady = vm.firebaseConfigured,
                    signedInEmail = vm.signedInEmail,
                    onSaveBudget = vm::saveBudget,
                    onCurrency = vm::setCurrency,
                    onAlarm = vm::setAlarmEnabled,
                    onOpenCapture = {
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    },
                    onRefreshCapture = {
                        vm.refreshListenerEnabled(
                            NotificationManagerCompat.getEnabledListenerPackages(context)
                                .contains(context.packageName),
                        )
                    },
                    onSignIn = vm::signIn,
                    onSignUp = vm::signUp,
                    onSignOut = vm::signOut,
                )
            }
        }
    }
}
