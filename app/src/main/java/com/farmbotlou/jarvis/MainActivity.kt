package com.farmbotlou.jarvis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JarvisScreen()
        }
    }
}

@Composable
private fun JarvisScreen() {
    val deepBlue = Color(0xFF06152E)
    val electricBlue = Color(0xFF19BFFF)

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
                    text = "Systems standing by",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Voice assistant setup is coming next",
                    color = Color(0xFF9CB9D5),
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { /* Voice interaction will be added in the next milestone. */ },
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
                    text = "Wake phrase planned: “Hey Jarvis”",
                    color = Color(0xFF7796B5),
                    fontSize = 12.sp
                )
            }
        }
    }
}
