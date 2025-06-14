package com.popolam.app.dailyfact.ui.compose

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.popolam.app.dailyfact.ui.theme.PurpleLight
import com.popolam.app.dailyfact.ui.theme.PurpleMedium
import kotlinx.coroutines.delay

/**
 * scalling_rotating_loader
 */
@Preview
@Composable
fun ScalingRotatingLoader() {
    val isDark = isSystemInDarkTheme()
    var rotateOuter by remember {
        mutableStateOf(false)
    }


    val angle by animateFloatAsState(
        targetValue = if (rotateOuter) 360 * 3f else 0f,
        animationSpec = spring(
            visibilityThreshold = 0.3f,
            dampingRatio = 0.1f,
            stiffness = 0.87f
        )
    )
    val infiniteTransition = rememberInfiniteTransition()
    val scaleBox by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        )
    )

    val centerOffset = with(LocalDensity.current) {
        Offset(
            LocalConfiguration.current.screenWidthDp.div(2).dp.toPx(),
            LocalConfiguration.current.screenHeightDp.div(2).dp.toPx()
        )
    }

    LaunchedEffect(key1 = true, block = {
        rotateOuter = !rotateOuter
        while (true){
            // infiniteRepeatable does not support spring yet  Compose documentation has a
            delay(2000)
            rotateOuter = !rotateOuter
        }
    })


    Box {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(1.2f)
                /*.background(
                    brush = Brush.radialGradient(
                        listOf(Color.Black, Color.Blue),
                        center = centerOffset, radius = 500f, tileMode = TileMode.Clamp
                    )
                )*/
        ) {
            Box(
                Modifier
                    .scale(scaleBox)
                    .align(Alignment.Center)
            ) {
                // center circle
                Box(
                    Modifier
                        .align(Alignment.Center)
                        .size(50.dp)
                        .background(PurpleMedium, shape = CircleShape)
                )
                // two arc's
                Box(Modifier.rotate(angle)) {
                    Canvas(modifier = Modifier
                        .align(Alignment.Center)
                        .size(100.dp), onDraw = {
                        drawArc(
                            color =
                                PurpleLight,
                            style = Stroke(
                                width = 6f,
                                cap = StrokeCap.Round,
                                join =
                                    StrokeJoin.Round,
                            ),
                            startAngle = 180f,
                            sweepAngle = 288f,
                            useCenter = false
                        )

                    })
                }

                Box(Modifier.rotate(angle)) {
                    Canvas(modifier = Modifier
                        .rotate(180f)
                        .align(Alignment.Center)
                        .size(100.dp), onDraw = {
                        drawArc(
                            color =
                                PurpleMedium,
                            style = Stroke(
                                width = 6f,
                                cap = StrokeCap.Round,
                                join =
                                    StrokeJoin.Round,
                            ),
                            startAngle = 180f,
                            sweepAngle = 288f,
                            useCenter = false
                        )
                    }
                    )
                }

            }

        }
    }
}