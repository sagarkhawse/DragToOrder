package com.laboontech.dragtoorder.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.laboontech.dragtoorder.R

@Composable
fun Particle(modifier: Modifier, isFired: Boolean, color: Color, onCompleteAnim: () -> Unit) {
    val radiusDp = dimensionResource(id = R.dimen.particle_radius)
    val radius: Float
    val topPadding: Float
    val itemHeight: Float
    with(LocalDensity.current) {
        radius = radiusDp.toPx()
        topPadding = dimensionResource(id = R.dimen.list_top_padding).toPx()
        itemHeight = dimensionResource(id = R.dimen.image_size).toPx()
    }
    var topTranslation by remember { mutableStateOf(0f) }

    Canvas(modifier.size(radiusDp * 2)) {
        translate(top = topTranslation) {
            drawCircle(
                color = color,
                radius = radius,
            )
        }
    }
// SmileyFaceCanvas(modifier = modifier,topTranslation)
    val animatedTopTranslation = remember { Animatable(0f) }
    LaunchedEffect(isFired) {
        if (isFired) {
            animatedTopTranslation.animateTo(
                targetValue = radius + topPadding + itemHeight / 2,
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                ),
            ) {
                topTranslation = value
            }
            animatedTopTranslation.snapTo(0f)
            topTranslation = 0f
            onCompleteAnim()
        }
    }
}

@Preview(widthDp = 200, heightDp = 200)
@Composable
fun ParticlePreview() {
    var isVisible by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize()) {
        Particle(
            modifier = Modifier.align(Alignment.TopCenter),
            isFired = isVisible,
            color = Color.Yellow,
        ) { }

        Switch(
            modifier = Modifier.align(Alignment.TopStart),
            checked = isVisible,
            onCheckedChange = { isVisible = !isVisible },
        )
    }
}
