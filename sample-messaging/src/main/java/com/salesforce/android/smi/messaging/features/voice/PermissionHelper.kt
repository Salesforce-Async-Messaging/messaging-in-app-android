package com.salesforce.android.smi.messaging.features.voice

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Helper to request voice call permissions (RECORD_AUDIO and BLUETOOTH_CONNECT).
 */
@Composable
fun rememberRecordAudioPermission(
    onPermissionGranted: () -> Unit
): () -> Unit {
    val context = LocalContext.current

    // Build list of required permissions based on Android version
    val requiredPermissions = buildList {
        add(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var allPermissionsGranted by remember {
        mutableStateOf(
            requiredPermissions.all { permission ->
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        allPermissionsGranted = permissions.values.all { it }
        if (allPermissionsGranted) {
            onPermissionGranted()
        }
    }

    return {
        if (allPermissionsGranted) {
            onPermissionGranted()
        } else {
            launcher.launch(requiredPermissions.toTypedArray())
        }
    }
}
