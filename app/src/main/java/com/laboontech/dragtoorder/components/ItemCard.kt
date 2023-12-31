package com.laboontech.dragtoorder

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.laboontech.dragtoorder.components.Chip
import com.laboontech.dragtoorder.models.Item
import com.laboontech.dragtoorder.models.SlideState
import com.laboontech.dragtoorder.ui.theme.Purple
import kotlinx.coroutines.launch
import kotlin.math.hypot

private var itemHeight = 0
private var slotItemDifference = 0f
private var maxRadiusPx = 0f

@Composable
fun ItemCard(
    isVisible: Boolean,
    index: Int,
    item: Item,
    slideState: SlideState,
    itemList: MutableList<Item>,
    updateSlideState: (item: Item, slideState: SlideState) -> Unit,
    updateItemPosition: (currentIndex: Int, destinationIndex: Int) -> Unit,
) {
    // Add item animation
    val particleRadius: Float
    with(LocalDensity.current) {
        particleRadius = 10.dp.toPx()
    }
    var radius by remember { mutableStateOf(particleRadius) }
    var visibilityAlpha by remember { mutableStateOf(0f) }

    val itemHeightDp = 160.dp
    val slotPaddingDp = 12.dp

    with(LocalDensity.current) {
        itemHeight = itemHeightDp.toPx().toInt()
        slotItemDifference = 18.dp.toPx()
    }
    val verticalTranslation by animateIntAsState(
        targetValue = when (slideState) {
            SlideState.UP -> -itemHeight
            SlideState.DOWN -> itemHeight
            else -> 0
        },
    )

    val isDragged = remember {
        mutableStateOf(false)
    }
    val zIndex = if (isDragged.value) 1.0f else 0.0f
    val rotation = if (isDragged.value) -5.0f else 0.0f
    val elevation = if (isDragged.value) 8.dp else 0.dp

    val currentIndex = remember {
        mutableStateOf(0)
    }
    val destinationIndex = remember {
        mutableStateOf(0)
    }

    val isPlaced = remember {
        mutableStateOf(false)
    }
    val leftParticlesRotation = remember {
        Animatable((Math.PI / 4).toFloat())
    }
    val rightParticlesRotation = remember {
        Animatable((Math.PI * 3 / 4).toFloat())
    }

    LaunchedEffect(key1 = isPlaced.value) {
        if (isPlaced.value) {
            launch {
                leftParticlesRotation.animateTo(
                    targetValue = Math.PI.toFloat(),
                    animationSpec = tween(durationMillis = 400),
                )
                leftParticlesRotation.snapTo((Math.PI / 4).toFloat())
            }

            launch {
                rightParticlesRotation.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 400),
                )
                rightParticlesRotation.snapTo((Math.PI * 3 / 4).toFloat())
                if (currentIndex.value != destinationIndex.value) {
                    updateItemPosition(currentIndex.value, destinationIndex.value)
                }
                isPlaced.value = false
            }
        }
    }

    val transition = rememberInfiniteTransition()
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 500,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
    )

    val pathEffect = PathEffect.dashPathEffect(
        intervals = floatArrayOf(20f, 20f),
        phase = phase,
    )
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .wrapContentWidth()
            .wrapContentHeight()
            .background(color = item.color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)),

    ) {
        if (isDragged.value) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp)
                    .padding(3.dp),
            ) {
                drawRect(
                    color = item.color,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = pathEffect,

                    ),
                )
            }
        }

        Box(
            Modifier
                .dragToReorder(
                    item,
                    itemList,
                    itemHeight,
                    updateSlideState,
                    { isDragged.value = true },
                    { cIndex, dIndex ->
                        isDragged.value = false
                        isPlaced.value = true
                        currentIndex.value = cIndex
                        destinationIndex.value = dIndex
                    },
                )
                .offset { IntOffset(0, verticalTranslation) }
                .zIndex(zIndex)
                .rotate(rotation),
        ) {
            Column(
                modifier = Modifier
                    .shadow(elevation, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        color = Color.Transparent,
                    )
                    .onGloballyPositioned { coordinates ->
                        if (maxRadiusPx == 0f) {
                            maxRadiusPx =
                                hypot(coordinates.size.width / 2f, coordinates.size.height / 2f)
                        }
                    }
                    .drawBehind {
                        drawCircle(
                            color = if (isVisible) item.color else Color.Transparent,
                            radius = radius,
                        )
                    }
                    .padding(slotPaddingDp)
                    .height(100.dp)
                    .align(Alignment.CenterStart)
                    .fillMaxWidth()
                    .alpha(visibilityAlpha),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White,
                )
                Spacer(Modifier.height(10.dp))
                Text(text = item.subTitle, fontSize = 14.sp, color = Color.White)
            }

            val priority = when (index) {
                0, 1 -> {
                    "High"
                }

                2, 3 -> {
                    "Medium"
                }

                else -> {
                    "Low"
                }
            }
            Chip(
                text = priority,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.BottomEnd),
                color = item.color,
            )
        }
    }

    // Add item animation
    val animatedRadius = remember { Animatable(particleRadius) }
    val animatedAlpha = remember { Animatable(0f) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            animatedRadius.animateTo(maxRadiusPx, animationSpec = tween()) {
                radius = value
            }
            animatedAlpha.animateTo(1f, animationSpec = tween()) {
                visibilityAlpha = value
            }
        }
    }
}

@ExperimentalAnimationApi
@Preview
@Composable
fun ShoesCardPreview() {
    ItemCard(
        false,
        0,
        Item(
            title = "Nike Air Max 270",
            subTitle = "2X",
            color = Purple,
        ),
        SlideState.NONE,
        mutableListOf(),
        { _, _ -> },
        { _, _ -> },
    )
}
