package com.laboontech.dragtoorder

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.laboontech.dragtoorder.components.AddItemComposable
import com.laboontech.dragtoorder.components.Particle
import com.laboontech.dragtoorder.models.Item
import com.laboontech.dragtoorder.models.SlideState
import com.laboontech.dragtoorder.ui.theme.Blue
import com.laboontech.dragtoorder.ui.theme.DragToOrderTheme
import com.laboontech.dragtoorder.ui.theme.Green
import com.laboontech.dragtoorder.ui.theme.Orange
import com.laboontech.dragtoorder.ui.theme.Pink
import com.laboontech.dragtoorder.ui.theme.Purple
import com.laboontech.dragtoorder.ui.theme.Red
import com.laboontech.dragtoorder.ui.theme.Violet
import com.laboontech.dragtoorder.ui.theme.Yellow

@Composable
fun HomeScreen() {
    // dummy list
    val allItemList = arrayOf(
        Item(
            title = "Identify Project Stakeholders and send out memo to entire project team",
            subTitle = "John | May 12, 12 pm",
            color = Green,
        ),
        Item(
            title = "Conduct stakeholder analysis workshop with xxx",
            subTitle = "Susan | May 13, 1 pm",
            color = Blue,
        ),
        Item(
            title = "Specify deliverables and acceptance criteria",
            subTitle = "Kevin | May 14, 11 am",
            color = Red,
        ),
        Item(
            title = "Urgently need to fin workaround or alternate vendor",
            subTitle = "Tim | May 15, 12 am",
            color = Orange,
        ),
        Item(
            title = "Make Stopwatch App using jetpack compose",
            subTitle = "Today at 12 pm",
            color = Pink,
        ),
        Item(
            title = "Update feature based on last feedback",
            subTitle = "Today at 5pm",
            color = Purple,
        ),
        Item(
            title = "Push new update to version control",
            subTitle = "Tomorrow at 7am",
            color = Yellow,
        ),
        Item(
            title = "Bugs fixes, commit and push all changes",
            subTitle = "The day after tomorrow at 6pm",
            color = Violet,
        ),
    )

    val itemList = remember {
        mutableStateListOf<Item>()
    }

    val slideStates = remember {
        mutableStateMapOf<Item, SlideState>()
            .apply {
                itemList.map { item ->
                    item to SlideState.NONE
                }.toMap().also {
                    putAll(it)
                }
            }
    }

// add item animation
    var isFired by remember {
        mutableStateOf(false)
    }

    var addItem by remember {
        mutableStateOf(Item())
    }
    var id by remember {
        mutableStateOf(0)
    }

    val isVisibleStates = remember {
        mutableStateMapOf<Item, Boolean>()
            .apply {
                itemList.map { item ->
                    item to false
                }.toMap().also {
                    putAll(it)
                }
            }
    }

    // Add dialog
    var isAddNewItem by remember {
        mutableStateOf(false)
    }

    val transition = updateTransition(targetState = isAddNewItem, label = "")

    val blurEffect = transition.animateDp(
        label = "",
        transitionSpec = {
            tween(1000)
        },
    ) {
        if (it) 5.dp else 0.dp
    } // background blur needed when showing any dialog

    var value by remember {
        mutableStateOf("")
    }
    val coroutine = rememberCoroutineScope()
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.blur(blurEffect.value),
            topBar = {
                Box {
                    Particle(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        isFired = isFired,
                        color = addItem.color,
                    ) {
                        isFired = false
                        isVisibleStates[addItem] = true
                    }
                    TopAppBar(
                        title = {
                            Text(text = "Task Management")
                        },
                        backgroundColor = MaterialTheme.colors.background,
                        actions = {
                            IconButton(onClick = {
                                isAddNewItem = true

                                addItem = allItemList.random().copy(id = id++)

                                itemList.add(0, addItem)
                                isFired = true
                            }) {
                                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                            }
                        },
                    )
                }
            },
        ) { innerPadding ->

            ItemList(
                isVisibleStates = isVisibleStates,
                modifier = Modifier.padding(innerPadding),
                itemList = itemList,
                slideStates = slideStates,
                updateSlideState = { item, slideState ->
                    slideStates[item] = slideState
                },
                updateItemPosition = { currentIndex, destinationIndex ->
                    val item = itemList[currentIndex]
                    itemList.removeAt(currentIndex)
                    itemList.add(destinationIndex, item)
                    slideStates.apply {
                        itemList.map { shoesArticle ->
                            shoesArticle to SlideState.NONE
                        }.toMap().also {
                            putAll(it)
                        }
                    }
                },
            )
        }

        // Open add new bookmark dialog when Add click on Bookmark screen
        AnimatedVisibility(
            visible = isAddNewItem,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            AddItemComposable(
                value = value,
                onValueChange = {
                    value = it
                },
                onCancelClick = {
                        isAddNewItem = false
                },
                onSaveClick = {},
            )
        }
    }
}

@Composable
fun ItemList(
    isVisibleStates: Map<Item, Boolean>,
    modifier: Modifier,
    itemList: MutableList<Item>,
    slideStates: Map<Item, SlideState>,
    updateSlideState: (item: Item, slideState: SlideState) -> Unit,
    updateItemPosition: (currentIndex: Int, destinationIndex: Int) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    LazyColumn(
        state = lazyListState,
        modifier = modifier.padding(top = 14.dp),
    ) {
        items(itemList.size) { index ->
            val item = itemList.getOrNull(index)
            if (item != null) {
                key(item) {
                    val slideState = slideStates[item] ?: SlideState.NONE

                    ItemCard(
                        isVisible = isVisibleStates[item] == true,
                        index = index,
                        item = item,
                        slideState = slideState,
                        itemList = itemList,
                        updateSlideState = updateSlideState,
                        updateItemPosition = updateItemPosition,
                    )
                }
            }
        }
    }
}

@Preview("default")
@Preview("dark-mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private
fun HomeScreenPreview() {
    DragToOrderTheme {
        HomeScreen()
    }
}
