package com.lambrk.qrcode.ui.scan.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private fun icon(name: String, path: String) =
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
        .addPath(pathData = addPathNodes(path), fill = SolidColor(Color.Black))
        .build()

val FlashOnIcon: ImageVector = icon("FlashOn", "M7,2v11h3v9l7,-12h-4l4,-8z")

val FlashOffIcon: ImageVector = icon(
    "FlashOff",
    "M3.27,3L2,4.27l5,5V13h3v9l3.58,-6.14L17.73,20 19,18.73 3.27,3zM17,10h-4l4,-8H7v2.18l8.46,8.46L17,10z"
)

val WifiOffIcon: ImageVector = icon(
    "WifiOff",
    "M22.99,9C19.15,5.16 13.8,3.76 8.84,4.78l2.52,2.52c3.47,-0.17 6.99,1.05 9.63,3.7l2,-2zM18.99,13c-1.29,-1.29 -2.84,-2.13 -4.49,-2.56l3.53,3.53 0.96,-0.97zM2,3.05L5.07,6.1C3.6,6.83 2.22,7.79 1,9l2,2c1.23,-1.23 2.65,-2.16 4.17,-2.78l2.24,2.24C7.79,10.89 6.27,11.74 5,13l2,2c1.35,-1.35 3.11,-2.04 4.89,-2.06l7.08,7.08 1.41,-1.41L3.41,1.64 2,3.05zM9,17l3,3 3,-3c-1.65,-1.66 -4.34,-1.66 -6,0z"
)

val CloudOffIcon: ImageVector = icon(
    "CloudOff",
    "M19.35,10.04C18.67,6.59 15.64,4 12,4c-1.48,0 -2.85,0.43 -4.01,1.17l1.46,1.46C10.21,6.23 11.08,6 12,6c3.04,0 5.5,2.46 5.5,5.5v0.5H19c1.66,0 3,1.34 3,3 0,1.13 -0.64,2.11 -1.56,2.62l1.45,1.45C23.16,18.16 24,16.68 24,15c0,-2.64 -2.05,-4.78 -4.65,-4.96zM3,5.27l2.75,2.74C2.56,8.15 0,10.77 0,14c0,3.31 2.69,6 6,6h11.73l2,2L21,20.73 4.27,4 3,5.27zM7.73,10l8,8H6c-2.21,0 -4,-1.79 -4,-4s1.79,-4 4,-4h1.73z"
)
