package com.farmbotlou.jarvis

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.AlarmClock
import android.provider.Settings
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
                if (ttsReady) textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, "jarvis_reply")
            }
        }
    }

    override fun onDestroy() {
        if (::textToSpeech.isInitialized) textToSpeech.shutdown()
        super.onDestroy()
    }
}

private fun runPhoneCommand(context: Context, phrase: String): String {
    val command = phrase.trim()
    val lower = command.lowercase(Locale.getDefault())

    fun launch(intent: Intent, success: String): String = try {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        success
    } catch (_: ActivityNotFoundException) {
        "I couldn't find an app to handle that request."
    } catch (_: SecurityException) {
        "Android blocked that action. You can open it manually in Settings."
    }

    if (lower.startsWith("open ")) {
        val requested = lower.removePrefix("open ").trim()
        if (requested.isBlank()) return "Tell me which app to open."
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = context.packageManager.queryIntentActivities(launcherIntent, 0)
        val match = apps.firstOrNull {
            it.loadLabel(context.packageManager).toString().lowercase(Locale.getDefault()).contains(requested)
        }
        if (match != null) {
            val appIntent = context.packageManager.getLaunchIntentForPackage(match.activityInfo.packageName)
            if (appIntent != null) return launch(appIntent, "Opening ${match.loadLabel(context.packageManager)}.")
        }
        return "I couldn't find an installed app called $requested."
    }

    if (lower.startsWith("call ")) {
        val target = command.substringAfter(" ", "").trim()
        val digits = target.filter { it.isDigit() || it == '+' || it == '*' || it == '#' }
        return if (digits.isNotBlank()) {
            launch(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digits")), "Opening the dialer. Review the number and tap call when you're ready.")
        } else {
            launch(Intent(Intent.ACTION_VIEW, Uri.parse("content://com.android.contacts/contacts")), "Opening your contacts. Choose the person and start the call yourself.")
        }
    }

    if (lower.startsWith("text ") || lower.startsWith("message ")) {
        return launch(Intent(Intent.ACTION_VIEW, Uri.parse("sms:")), "Opening Messages. Choose the recipient, review your message, and send it yourself.")
    }

    if (lower.contains("set an alarm") || lower.contains("set alarm")) {
        val alarmMatch = Regex("""\\b(1[0-2]|0?[1-9])(?::([0-5]\\d))?\\s*(am|pm)\\b""", RegexOption.IGNORE_CASE).find(command)
        if (alarmMatch != null) {
            var hour = alarmMatch.groupValues[1].toInt()
            val minute = alarmMatch.groupValues[2].ifBlank { "0" }.toInt()
            if (alarmMatch.groupValues[3].equals("pm", true) && hour != 12) hour += 12
            if (alarmMatch.groupValues[3].equals("am", true) && hour == 12) hour = 0
            return launch(Intent(AlarmClock.ACTION_SET_ALARM).putExtra(AlarmClock.EXTRA_HOUR, hour)
                .putExtra(AlarmClock.EXTRA_MINUTES, minute).putExtra(AlarmClock.EXTRA_SKIP_UI, false),
                "Opening the alarm screen for ${alarmMatch.value}. Confirm it there.")
        }
        return launch(Intent(AlarmClock.ACTION_SET_ALARM), "Opening the alarm screen. Set the time and confirm it there.")
    }

    if (lower.contains("wifi") || lower.contains("wi-fi")) return launch(Intent(Settings.ACTION_WIFI_SETTINGS), "Opening Wi-Fi settings.")
    if (lower.contains("bluetooth")) return launch(Intent(Settings.ACTION_BLUETOOTH_SETTINGS), "Opening Bluetooth settings.")
    if (lower.contains("settings")) return launch(Intent(Settings.ACTION_SETTINGS), "Opening phone settings.")
    if (lower.startsWith("search for ") || lower.startsWith("search ")) {
        val query = command.substringAfter("search", "").removePrefix(" for ").trim()
        return launch(Intent(Intent.ACTION_WEB_SEARCH).putExtra("query", query), "Searching the web for $query.")
    }
    if (lower.contains("what time")) return "The time is " + LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a"))
    if (lower.contains("date") || lower.contains("what day")) return "Today is " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
    if (lower.contains("hello") || lower.contains("hi jarvis")) return "Hello. JARVIS is online."
    if (lower.contains("help")) return "Try: open YouTube, call a phone number, text someone, set an alarm for 7 AM, open Wi-Fi settings, or search for something. I'll open the relevant screen; you confirm calls and messages."
    return "I heard: $command. I can open apps, the dialer, Messages, alarms, settings, and web searches. More commands will be added as we test."
}

@androidx.compose.runtime.Composable
private fun JarvisScreen(onSpeak: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val deepBlue = Color(0xFF06152E)
    val electricBlue = Color(0xFF19BFFF)
    val status = remember { mutableStateOf("Systems standing by") }
    val recognizedText = remember { mutableStateOf("Tap ACTIVATE JARVIS to speak") }
    val replyText = remember { mutableStateOf("Voice control is ready") }

    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        if (result.resultCode == android.app.Activity.RESULT_OK && !matches.isNullOrEmpty()) {
            val phrase = matches.first()
            recognizedText.value = phrase
            val reply = runPhoneCommand(context, phrase)
            replyText.value = reply
            status.value = "JARVIS completed a command"
            onSpeak(reply)
        } else status.value = "No speech detected. Try again."
    }

    val launchRecognizer = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to JARVIS")
        }
        try { speechLauncher.launch(intent) }
        catch (_: Exception) { status.value = "Speech recognition is unavailable on this device" }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchRecognizer() else status.value = "Microphone permission is needed for voice input"
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = deepBlue) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0xFF06152E), Color(0xFF0A2548), Color(0xFF030B18))))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("J.A.R.V.I.S.", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                Text("PERSONAL AI ASSISTANT", color = electricBlue, fontSize = 11.sp, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(44.dp))
                Box(modifier = Modifier.background(Brush.radialGradient(listOf(Color(0x553ACBFF), Color(0x0019BFFF))), CircleShape).padding(26.dp), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.background(Color(0xFF103B61), CircleShape).padding(34.dp), contentAlignment = Alignment.Center) {
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
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) launchRecognizer()
                        else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = electricBlue)
                ) {
                    Text("ACTIVATE JARVIS", color = deepBlue, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text("Tap to talk • Phone actions require your confirmation", color = Color(0xFF7796B5), fontSize = 12.sp)
            }
        }
    }
}
