package com.laboontech.dragtoorder.components

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.laboontech.dragtoorder.R
import com.laboontech.dragtoorder.ui.theme.DragToOrderTheme
import com.laboontech.dragtoorder.ui.theme.black
import com.laboontech.dragtoorder.ui.theme.grey
import com.laboontech.dragtoorder.ui.theme.redd
import com.laboontech.dragtoorder.ui.theme.white
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

val delayTime = 300

@Composable
fun AddItemComposable(
    value: String,
    onValueChange: (String) -> Unit = {},
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    val backgroundColor = grey

    val textColor = MaterialTheme.colors.secondary

    var isVisible by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        delay(delayTime.milliseconds)
        isVisible = true
    }

    // For animation of card
    val transition = updateTransition(targetState = isVisible, label = null)

    val enterTransitionSpec = remember {
        spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        )
    }

    val exitTransitionSpec = remember {
        tween<Float>(
            durationMillis = 300,
            easing = FastOutSlowInEasing,
        )
    }

    val alpha by transition.animateFloat(
        transitionSpec = {
            if (targetState) enterTransitionSpec else exitTransitionSpec
        },
        targetValueByState = { state ->
            if (state) 1f else 0f
        },
        label = "Alpha Animation",
    )

    val translationY by transition.animateFloat(
        label = "Translation y animation",
        transitionSpec = {
            if (targetState) enterTransitionSpec else exitTransitionSpec
        },
        targetValueByState = { state ->
            if (state) 0f else with(LocalDensity.current) { 40.dp.toPx() }
        },
    )

    val scope = rememberCoroutineScope()

    Card(
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        modifier = Modifier.wrapContentSize().padding(horizontal = 15.dp).graphicsLayer {
            this.clip = true
            this.alpha = alpha
            this.translationY = translationY
        },
        elevation = 10.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            IconButton(modifier = Modifier.align(Alignment.End), onClick = { /*TODO*/ }) {
                Icon(
                    painter = painterResource(id = R.drawable.close),
                    contentDescription = "",
                )
            }

            Text(
                text = "Add new item",
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                fontSize = 24.sp,
            )

            Spacer(modifier = Modifier.height(10.dp))

            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(text = "Type here", fontSize = 14.sp) },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = backgroundColor,
                    disabledIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                ),
            )

            Spacer(modifier = Modifier.height(30.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        scope.launch {
                            delay(delayTime.milliseconds)
                            onCancelClick()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = grey,
                        contentColor = black,
                    ),
                ) {
                    Text(text = "Cancel")
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { onSaveClick() },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = redd,
                        contentColor = white,
                    ),
                ) {
                    Text(text = "Add")
                }
            }
        }
    }
}

@Preview("default")
@Preview("dark - mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private
fun AddItemComposablePreview() {
    DragToOrderTheme {
        var value by remember {
            mutableStateOf("")
        }
        val coroutine = rememberCoroutineScope()

        AddItemComposable(
            value = value,
            onValueChange = {
                value = it
            },
            onCancelClick = {
//                coroutine.launch {
//                    delay(delayTime.milliseconds)
//                    onCancelClick()
//                }
            },
            onSaveClick = {},
        )
    }
}
