package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun GraduationCapIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Mortarboard Diamond (Top)
        val capPath = Path().apply {
            moveTo(w * 0.5f, h * 0.2f)       // Top center
            lineTo(w * 0.9f, h * 0.45f)      // Right
            lineTo(w * 0.5f, h * 0.7f)       // Bottom center
            lineTo(w * 0.1f, h * 0.45f)      // Left
            close()
        }
        drawPath(path = capPath, color = tint)

        // Skull Cap (Underneath base)
        val baseLeftX = w * 0.3f
        val baseRightX = w * 0.7f
        val baseY = h * 0.57f
        val baseBottomY = h * 0.78f

        val basePath = Path().apply {
            moveTo(baseLeftX, baseY)
            cubicTo(
                baseLeftX, baseBottomY,
                baseRightX, baseBottomY,
                baseRightX, baseY
            )
            lineTo(baseRightX, h * 0.55f)
            cubicTo(
                baseRightX, baseBottomY * 0.9f,
                baseLeftX, baseBottomY * 0.9f,
                baseLeftX, h * 0.55f
            )
            close()
        }
        drawPath(path = basePath, color = tint)

        // Center tassel node
        drawCircle(color = tint.copy(alpha = 0.9f), radius = w * 0.035f, center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.45f))

        // Tassel string sweeping left
        val tasselPath = Path().apply {
            moveTo(w * 0.5f, h * 0.45f)
            quadraticTo(w * 0.25f, h * 0.48f, w * 0.22f, h * 0.65f)
        }
        drawPath(
            path = tasselPath,
            color = tint,
            style = Stroke(width = w * 0.03f, cap = StrokeCap.Round)
        )

        // Tassel brush fringe at the end
        val fringePath = Path().apply {
            moveTo(w * 0.22f, h * 0.65f)
            lineTo(w * 0.17f, h * 0.8f)
            lineTo(w * 0.27f, h * 0.8f)
            close()
        }
        drawPath(path = fringePath, color = tint)
    }
}
