package com.laboontech.dragtoorder.models

import androidx.compose.ui.graphics.Color

data class Item(
    var id: Int = 0,
    var title: String = "",
    var subTitle: String = "",
    var color: Color = Color.Transparent,
)
