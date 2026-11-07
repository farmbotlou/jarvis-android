package com.farmbotlou.jarvis

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
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
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var textToSpeech: TextToSpeech
    private var ttsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textToSpeech = TextToSpeech(this) { result ->
            ttsReady = result == TextToSpeech.SUCCESS
            if (ttsReady) textToSpeech.language = Locale.getDefault()
        }
        setContent {
            JarvisScreen { message ->
                if (ttsReady) {
                    textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, "jarvis_reply")
                }
            }
        }
    }

    override fun onDestroy() {
        if (::textToSpeech.isInitialized) textToSpeech.shutdown()
        super.onDestroy()
    }
}

@androidx.compose.runtime.Composable
private fun JarvisScreen(onSpeak: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val deepBlue = Color(0xFF06152E)
    val electricBlue = Color(0xFF19BFFF)
    val status = remember { mutableStateOf("Systems standing by") }
    val recognizedText = remember { mutableStateOf("Tap ACTIVATE JARVIS to speak") }
    val replyText = remember { mutableStateOf("Voice replies are ready to test") }

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        if (result.resultCode == android.app.Activity.RESULT_OK && !matches.isNullOrEmpty()) {
            val phrase = matches.first()
            recognizedText.value = phrase
            val reply = when {
                phrase.contains("hello", ignoreCase = true) ||
                    phrase.contains("hi jarvis", ignoreCase = true) ->
                    "Hello. JARVIS is online."
                phrase.contains("time", ignoreCase = true) ->
                    "The time is " + LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a"))
                phrase.contains("date", ignoreCase = true) ||
                    phrase.contains("day is it", ignoreCase = true) ->
                    "Today is " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
                phrase.contains("help", ignoreCase = true) ->
                    "You can ask me to say hello, tell you the time, or tell you today's date."
                else ->
                    "I heard you say: $phrase. My offline command list is still growing."
            }
            replyText.value = reply
            status.value = "JARVIS replied"
            onSpeak(reply)
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
        if (granted) launchRecognizer()
        else status.value = "Microphone permission is needed for voice input"
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = deepBlue) {
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

                Spacer(modifier = Modifier.height(44.dp))

                Box(
                    modifier = Modifier
                        .background(
                            Brush.radialGradient(listOf(Color(0x553ACBFF), Color(0x0019BFFF))),
                            CircleShape
                        )
                        .padding(26.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.background(Color(0xFF103B61), CircleShape).padding(34.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("J", color = Color.White, fontSize = 52.sp, fontWeight = FontWeight.Light)
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
                Text(status.value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Text(recognizedText.value, color = Color(0xFF9CB9D5), fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("JARVIS: " + replyText.value, color = Color(0xFFB9E8FF), fontSize = 14.sp)

                Spacer(modifier = Modifier.height(28.dp))
                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                            == PackageManager.PERMISSION_GRANTED
                        ) launchRecognizer()
                        else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
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
                    text = "Tap to talk • No paid AI API connected",
                    color = Color(0xFF7796B5),
                    fontSize = 12.sp
                )
            }
        }
    }
}
