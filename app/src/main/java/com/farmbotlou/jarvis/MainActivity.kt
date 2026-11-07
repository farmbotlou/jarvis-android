package com.farmbotlou.jarvis

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JarvisScreen()
        }
    }
}

@androidx.compose.runtime.Composable
private fun JarvisScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val deepBlue = Color(0xFF06152E)
    val electricBlue = Color(0xFF19BFFF)
    val status = remember { mutableStateOf("Systems standing by") }
    val recognizedText = remember { mutableStateOf("Tap ACTIVATE JARVIS to speak") }

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val matches = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        if (result.resultCode == android.app.Activity.RESULT_OK && !matches.isNullOrEmpty()) {
            recognizedText.value = matches.first()
            status.value = "Voice input received"
        } else {
            status.value = "No speech detected. Try again."
        }
    }

    val launchRecognizer = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to JARVIS")
        }
        try {
            speechLauncher.launch(intent)
        } catch (_: Exception) {
            status.value = "Speech recognition is unavailable on this device"
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchRecognizer()
        } else {
            status.value = "Microphone permission is needed for voice input"
        }
    }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = deepBlue
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF06152E), Color(0xFF0A2548), Color(0xFF030B18))
                        )
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "J.A.R.V.I.S.",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
                Text(
                    text = "PERSONAL AI ASSISTANT",
                    color = electricBlue,
                    fontSize = 11.sp,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(56.dp))

                Box(
                    modifier = Modifier
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0x553ACBFF), Color(0x0019BFFF))
                            ),
                            CircleShape
                        )
                        .padding(26.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF103B61), CircleShape)
                            .padding(34.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("J", color = Color.White, fontSize = 52.sp, fontWeight = FontWeight.Light)
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = status.value,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = recognizedText.value,
                    color = Color(0xFF9CB9D5),
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            launchRecognizer()
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = electricBlue)
                ) {
                    Text(
                        text = "ACTIVATE JARVIS",
                        color = deepBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Tap to talk • “Hey Jarvis” is planned",
                    color = Color(0xFF7796B5),
                    fontSize = 12.sp
                )
            }
        }
    }
}
