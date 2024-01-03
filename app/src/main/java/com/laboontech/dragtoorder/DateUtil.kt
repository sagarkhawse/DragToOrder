package com.newsclez.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtil {
    fun getFormatTimeWithTZ(currentTime: Date): String {
        val timeZoneDate = SimpleDateFormat("hh:mm a dd MMMM yyyy", Locale.getDefault())
        return timeZoneDate.format(currentTime)
    }
}
