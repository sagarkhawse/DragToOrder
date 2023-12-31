package com.laboontech.dragtoorder

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.IntOffset
import com.laboontech.dragtoorder.models.Item
import com.laboontech.dragtoorder.models.SlideState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sign

fun Modifier.dragToReorder(
    item: Item,
    itemList: MutableList<Item>,
    itemHeight: Int,
    updateSlideState: (item: Item, slideState: SlideState) -> Unit,
    onDrag: () -> Unit,
    onStopDrag: (currentIndex: Int, destinationIndex: Int) -> Unit,
): Modifier = composed {
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    pointerInput(Unit) {
        // Wrap in a coroutine scope to use suspend functions for touch events and animation.
        coroutineScope {
            while (true) {
                // Wait for a touch down event.
                val pointerId = awaitPointerEventScope { awaitFirstDown().id }
                // Interrupt any ongoing animation of other items.
                offsetX.stop()
                offsetY.stop()

                val itemIndex = itemList.indexOf(item)
                val offsetToSlide = itemHeight / 4
                var numberOfItems = 0
                var previousNumberOfItems: Int
                var listOffset = 0
                // Wait for drag events.
                awaitPointerEventScope {
                    drag(pointerId) { change ->
                        onDrag()
                        val horizontalDragOffset = offsetX.value + change.positionChange().x
                        launch {
                            offsetX.snapTo(horizontalDragOffset)
                        }
                        val verticalDragOffset = offsetY.value + change.positionChange().y
                        launch {
                            offsetY.snapTo(verticalDragOffset)
                            val offsetSign = offsetY.value.sign.toInt()
                            previousNumberOfItems = numberOfItems
                            numberOfItems = calculateNumberOfSlidItems(
                                offsetY.value * offsetSign,
                                itemHeight,
                                offsetToSlide,
                                previousNumberOfItems,
                            )

                            if (previousNumberOfItems > numberOfItems) {
                                updateSlideState(
                                    itemList[itemIndex + previousNumberOfItems * offsetSign],
                                    SlideState.NONE,
                                )
                            } else if (numberOfItems != 0) {
                                try {
                                    updateSlideState(
                                        itemList[itemIndex + numberOfItems * offsetSign],
                                        if (offsetSign == 1) SlideState.UP else SlideState.DOWN,
                                    )
                                } catch (e: IndexOutOfBoundsException) {
                                    numberOfItems = previousNumberOfItems
                                }
                            }
                            listOffset = numberOfItems * offsetSign
                        }
                        // Consume the gesture event, not passed to external
                        change.consumePositionChange()
                    }
                }
                launch {
                    offsetX.animateTo(0f)
                }
                launch {
                    offsetY.animateTo(itemHeight * numberOfItems * offsetY.value.sign)
                    onStopDrag(itemIndex, itemIndex + listOffset)
                }
            }
        }
    }
        .offset {
            // Use the animating offset value here.
            IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt())
        }
}

private fun calculateNumberOfSlidItems(
    offsetY: Float,
    itemHeight: Int,
    offsetToSlide: Int,
    previousNumberOfItems: Int,
): Int {
    val numberOfItemsInOffset = (offsetY / itemHeight).toInt()
    val numberOfItemsPlusOffset = ((offsetY + offsetToSlide) / itemHeight).toInt()
    val numberOfItemsMinusOffset = ((offsetY - offsetToSlide - 1) / itemHeight).toInt()
    return when {
        offsetY - offsetToSlide - 1 < 0 -> 0
        numberOfItemsPlusOffset > numberOfItemsInOffset -> numberOfItemsPlusOffset
        numberOfItemsMinusOffset < numberOfItemsInOffset -> numberOfItemsInOffset
        else -> previousNumberOfItems
    }
}

// fun Modifier.dashedBorder(width: Dp, radius: Dp, color: Color) =
//    drawBehind {
//        drawIntoCanvas {
//            val paint = Paint()
//                .apply {
//                    strokeWidth = width.toPx()
//                    this.color = color
//                    style = PaintingStyle.Stroke
//                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
//                }
//            it.drawRoundRect(
//                width.toPx(),
//                width.toPx(),
//                size.width - width.toPx(),
//                size.height - width.toPx(),
//                radius.toPx(),
//                radius.toPx(),
//                paint
//            )
//        }
//    }
