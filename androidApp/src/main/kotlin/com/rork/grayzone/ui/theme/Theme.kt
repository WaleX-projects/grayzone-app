package com.rork.grayzone.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Grayzone is intentionally monochrome: black ink on white, with grays for
 * hierarchy. No dynamic color, no dark variant — the calm, discreet look is
 * the product.
 */
object GzColors {
    val Black = Color(0xFF0A0A0A)
    val Ink = Color(0xFF161616)
    val Gray700 = Color(0xFF454545)
    val Gray500 = Color(0xFF707070)
    val Gray400 = Color(0xFF9A9A9A)
    val Gray300 = Color(0xFFC7C7C7)
    val Border = Color(0xFFE7E7E7)
    val Track = Color(0xFFECECEC)
    val Surface = Color(0xFFF6F6F6)
    val SurfaceDeep = Color(0xFFF1F1F1)
    val White = Color(0xFFFFFFFF)
}

private val LightScheme = lightColorScheme(
    primary = GzColors.Black,
    onPrimary = GzColors.White,
    secondary = GzColors.Gray700,
    onSecondary = GzColors.White,
    background = GzColors.White,
    onBackground = GzColors.Ink,
    surface = GzColors.White,
    onSurface = GzColors.Ink,
    surfaceVariant = GzColors.Surface,
    onSurfaceVariant = GzColors.Gray500,
    outlineVariant = GzColors.Border,
    error = GzColors.Ink
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightScheme,
        content = content
    )
}
