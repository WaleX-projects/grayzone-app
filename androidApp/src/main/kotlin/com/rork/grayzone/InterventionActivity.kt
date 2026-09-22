package com.rork.grayzone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rork.grayzone.ui.components.PrimaryButton
import com.rork.grayzone.ui.components.SecondaryButton
import com.rork.grayzone.ui.theme.AppTheme
import com.rork.grayzone.ui.theme.GzColors

/**
 * Full-screen intervention activity shown when usage threshold is reached
 * This is a system-level interruption that appears on top of the monitored app
 */
class InterventionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appName = intent.getStringExtra("intervention_app") ?: "App"
        val usedMinutes = intent.getIntExtra("intervention_used", 0)
        val thresholdMinutes = intent.getIntExtra("intervention_threshold", 0)

        setContent {
            AppTheme {
                InterventionScreen(
                    appName = appName,
                    usedMinutes = usedMinutes,
                    thresholdMinutes = thresholdMinutes,
                    onContinue = {
                        finish()
                    },
                    onTakeBreak = {
                        // Could add logic here to actually close the app
                        // For now, just close the intervention activity
                        finish()
                    }
                )
            }
        }
    }

    override fun onBackPressed() {
        // Don't allow back button to dismiss intervention
        // User must make a choice
    }
}

@Composable
private fun InterventionScreen(
    appName: String,
    usedMinutes: Int,
    thresholdMinutes: Int,
    onContinue: () -> Unit,
    onTakeBreak: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(GzColors.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                "Hold on.",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = GzColors.Ink,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            // Main message
            Text(
                "You've been using $appName for $usedMinutes minutes.",
                fontSize = 18.sp,
                lineHeight = 26.sp,
                color = GzColors.Gray600,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            // Threshold message
            Text(
                "Your threshold is $thresholdMinutes minutes.",
                fontSize = 16.sp,
                color = GzColors.Gray500,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // Question
            Text(
                "What would you like to do?",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = GzColors.Gray500,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            // Buttons
            PrimaryButton(
                text = "Continue using $appName",
                modifier = Modifier.fillMaxWidth(),
                onClick = onContinue
            )

            Spacer(Modifier.height(12.dp))

            SecondaryButton(
                text = "Take a break",
                modifier = Modifier.fillMaxWidth(),
                onClick = onTakeBreak
            )

            Spacer(Modifier.height(32.dp))

            // Footer
            Text(
                "Remember: You're in control. This is just a gentle reminder.",
                fontSize = 12.sp,
                color = GzColors.Gray400,
                textAlign = TextAlign.Center
            )
        }
    }
}
