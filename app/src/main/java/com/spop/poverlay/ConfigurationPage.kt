package com.spop.poverlay

import android.os.Build
import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spop.poverlay.releases.Release
import com.spop.poverlay.ui.theme.ErrorColor
import com.spop.poverlay.ui.theme.LatoFontFamily


@Composable
fun ConfigurationPage(
    viewModel: ConfigurationViewModel
) {
    val showPermissionInfo by remember { viewModel.showPermissionInfo }
    val latestRelease by remember { viewModel.latestRelease }

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (showPermissionInfo) {
            PermissionPage(viewModel::onGrantPermissionClicked)
        } else {
            val timerShownWhenMinimized by viewModel.showTimerWhenMinimized
                .collectAsStateWithLifecycle(initialValue = true)
            val serverUrl by viewModel.serverUrl
                .collectAsStateWithLifecycle(initialValue = "")
            val isWebSocketConnected by viewModel.isWebSocketConnected
                .collectAsStateWithLifecycle(initialValue = false)
            val webSocketStatus by viewModel.webSocketStatus
                .collectAsStateWithLifecycle(initialValue = "Disconnected")
            StartServicePage(
                timerShownWhenMinimized,
                viewModel::onShowTimerWhenMinimizedClicked,
                serverUrl,
                viewModel::onServerUrlChanged,
                isWebSocketConnected,
                webSocketStatus,
                viewModel::onConnectClicked,
                viewModel::onDisconnectClicked,
                viewModel::onStartServiceClicked,
                viewModel::onStopServiceClicked,
                viewModel::onRestartClicked,
                viewModel::onClickedRelease,
                latestRelease
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StartServicePage(
    timerShownWhenMinimized: Boolean,
    onTimerShownWhenMinimizedToggled: (Boolean) -> Unit,
    serverUrl: String,
    onServerUrlChanged: (String) -> Unit,
    isWebSocketConnected: Boolean,
    webSocketStatus: String,
    onConnectClicked: () -> Unit,
    onDisconnectClicked: () -> Unit,
    onClickedStartOverlay: () -> Unit,
    onClickedStopOverlay: () -> Unit,
    onClickedRestartApp: () -> Unit,
    onClickedRelease: (Release) -> Unit,
    latestRelease: Release?
) {
    Text(
        text = "Grupetto: An overlay for your Peloton bike",
        fontSize = 50.sp,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Note: Not endorsed with, associated with, or supported by Peloton",
        fontSize = 25.sp,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(20.dp))
    Button(
        onClick = onClickedStartOverlay,
    ) {
        Text(
            text = "Click here to start the overlay",
            fontSize = 30.sp,
            fontFamily = LatoFontFamily,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
        )
    }
    Spacer(modifier = Modifier.height(20.dp))
    Button(
        onClick = onClickedStopOverlay,
    ) {
        Text(
            text = "Click here to stop the overlay",
            fontSize = 30.sp,
            fontFamily = LatoFontFamily,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
        )
    }
    Spacer(modifier = Modifier.height(100.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Show timer when the overlay is minimized?",
            fontSize = 20.sp
        )
        Checkbox(
            checked = timerShownWhenMinimized,
            onCheckedChange = onTimerShownWhenMinimizedToggled
        )
    }

    Spacer(modifier = Modifier.height(30.dp))

    Text(
        text = "Server",
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(10.dp))
    OutlinedTextField(
        value = serverUrl,
        onValueChange = onServerUrlChanged,
        label = { Text("WebSocket URL (e.g. ws://192.168.1.1:3000)", fontSize = 16.sp) },
        placeholder = { Text("Leave blank to disable") },
        singleLine = true,
        modifier = Modifier.width(600.dp),
        textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
    )
    Spacer(modifier = Modifier.height(12.dp))
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(
            onClick = onConnectClicked,
            enabled = !isWebSocketConnected && serverUrl.isNotBlank()
        ) {
            Text("Connect", fontSize = 18.sp)
        }
        Button(
            onClick = onDisconnectClicked,
            enabled = isWebSocketConnected,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Disconnect", fontSize = 18.sp)
        }
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = if (isWebSocketConnected) Color(0xFF00C853) else Color(0xFF9E9E9E),
                    shape = CircleShape
                )
        )
        Text(
            text = webSocketStatus,
            fontSize = 16.sp,
            color = if (isWebSocketConnected) Color(0xFF00C853) else LocalContentColor.current.copy(alpha = 0.6f)
        )
    }

    Spacer(modifier = Modifier.height(40.dp))

    if (latestRelease == null) {
        Text(text = "Couldn't check for updates")
    } else {
        val formattedDate = DateUtils.getRelativeTimeSpanString(latestRelease.createdAt.time)
        val releaseText = if (latestRelease.isCurrentlyInstalled) {
            buildAnnotatedString {
                "Grupetto is up to date: ${latestRelease.tagName} • $formattedDate • ${latestRelease.friendlyName}"
            }
        } else {
            buildAnnotatedString {
                append("⭐ ")
                withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                    append("New version released $formattedDate: ${latestRelease.friendlyName}.")
                }
            }
        }
        ClickableText(
            text = releaseText,
            style = LocalTextStyle.current.copy(
                fontSize = 20.sp,
                color = LocalContentColor.current
            )
        ) {
            onClickedRelease(latestRelease)
        }
    }


    Spacer(modifier = Modifier.height(40.dp))
    Button(
        onClick = onClickedRestartApp,
        colors = ButtonDefaults.buttonColors(containerColor = ErrorColor),
    ) {
        Text(
            text = "Restart Grupetto",
            fontSize = 20.sp,
            fontStyle = FontStyle.Italic,
            color = Color.White
        )
    }
    Spacer(modifier = Modifier.height(10.dp))

    Text(
        "Device: ${Build.DEVICE}\t" +
                "SDK: ${Build.VERSION.RELEASE}\t" +
                "OS Version: ${Build.FINGERPRINT}\t",
        color = LocalContentColor.current.copy(alpha = .5f)
    )
}

@Composable
private fun PermissionPage(onClickedGrantPermission: () -> Unit) {
    Text(
        text = "Grupetto Needs Permission To Draw Over Other Apps",

        fontSize = 40.sp,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "It uses this permission to draw an overlay with your bike's sensor data",
        fontSize = 20.sp,
        fontWeight = FontWeight.Normal
    )
    Spacer(modifier = Modifier.height(10.dp))
    Button(
        onClick = onClickedGrantPermission
    ) {
        Text(text = "Grant Permission")
    }
}