package com.examflow.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.examflow.app.R
import com.examflow.app.domain.model.ScheduleMode
import java.time.DayOfWeek

@Composable
fun SectionTitle(text: String, action: (@Composable () -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, style = MaterialTheme.typography.titleLarge)
        action?.invoke()
    }
}

@Composable
fun QuietCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(Modifier.padding(16.dp)) {
            content()
        }
    }
}

fun formatMinute(minute: Int): String = "%02d:%02d".format(minute / 60, minute % 60)

@Composable
fun dayLabel(dayOfWeek: Int): String = when (DayOfWeek.of(dayOfWeek)) {
    DayOfWeek.MONDAY -> stringResource(R.string.monday)
    DayOfWeek.TUESDAY -> stringResource(R.string.tuesday)
    DayOfWeek.WEDNESDAY -> stringResource(R.string.wednesday)
    DayOfWeek.THURSDAY -> stringResource(R.string.thursday)
    DayOfWeek.FRIDAY -> stringResource(R.string.friday)
    DayOfWeek.SATURDAY -> stringResource(R.string.saturday)
    DayOfWeek.SUNDAY -> stringResource(R.string.sunday)
}

@Composable
fun modeLabel(mode: ScheduleMode): String = when (mode) {
    ScheduleMode.BALANCED -> stringResource(R.string.balanced)
    ScheduleMode.FOCUS -> stringResource(R.string.focus)
    ScheduleMode.URGENT -> stringResource(R.string.urgent)
    ScheduleMode.RELAXED -> stringResource(R.string.relaxed)
}
