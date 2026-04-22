package com.example.alertify.ui.screens.sos

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alertify.ui.components.SlidingCancelButton
import com.example.alertify.ui.screens.fakecall.FakeCallScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SosScreen(onSosClick: () -> Unit, onCancelSos: () -> Unit) {
    val context = LocalContext.current
    val viewModel: SosViewModel = viewModel()
    val haptic = LocalHapticFeedback.current

    // Show fake call screen as full overlay
    var showFakeCall by remember { mutableStateOf(false) }
    if (showFakeCall) {
        FakeCallScreen(onDismiss = { showFakeCall = false })
        return
    }

    val permissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE
        )
    )

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) onSosClick()
    }

    var isHolding by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    val holdDuration = 3000L

    val triggerSosAction = {
        if (permissionsState.allPermissionsGranted) {
            viewModel.triggerSOS(context)
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(isHolding) {
        if (isHolding) {
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < holdDuration && isHolding) {
                progress = (System.currentTimeMillis() - startTime).toFloat() / holdDuration
                delay(16)
            }
            if (isHolding) {
                progress = 1f
                haptic.performHapticFeedback(
                    androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress
                )
                triggerSosAction()
                isHolding = false
            }
        } else {
            progress = 0f
        }
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Title
        Text(
            text = "AlertiFy",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isHolding) "Keep holding..." else "Hold the button in emergency",
            style = MaterialTheme.typography.bodyLarge,
            color = if (isHolding) Color.Red else Color.LightGray
        )

        Spacer(modifier = Modifier.weight(1.4f))

        // SOS Button
        Box(contentAlignment = Alignment.Center) {
            if (!isHolding) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.6f,
                    animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Restart),
                    label = "scale"
                )
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Restart),
                    label = "alpha"
                )
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(pulseScale)
                        .graphicsLayer { alpha = pulseAlpha }
                        .background(Color.Red.copy(alpha = 0.4f), CircleShape)
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(220.dp)
                    .shadow(20.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.Red)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isHolding = true
                                try {
                                    awaitRelease()
                                } finally {
                                    isHolding = false
                                }
                            }
                        )
                    }
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(220.dp),
                    color = Color.White,
                    strokeWidth = 8.dp,
                    trackColor = Color.Gray.copy(alpha = 0.3f)
                )
                Text(
                    text = if (isHolding) "${((1 - progress) * 3).toInt() + 1}" else "SOS",
                    color = Color.White,
                    style = MaterialTheme.typography.displayMedium
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // ── Fake Call Button ──────────────────────────────
        OutlinedButton(
            onClick = { showFakeCall = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White.copy(alpha = 0.06f),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color(0xFF4ADE80)   // green phone icon
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Fake Call",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
        // ─────────────────────────────────────────────────

        Spacer(modifier = Modifier.height(16.dp))

        SlidingCancelButton(onCancel = onCancelSos)

        Spacer(modifier = Modifier.height(16.dp))
    }
}