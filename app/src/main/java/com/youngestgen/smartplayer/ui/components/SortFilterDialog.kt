package com.youngestgen.smartplayer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.youngestgen.smartplayer.model.SortDirection
import com.youngestgen.smartplayer.model.SortOption
import com.youngestgen.smartplayer.ui.theme.NeonCyan

@Composable
fun SortFilterDialog(
    currentOption: SortOption,
    currentDirection: SortDirection,
    onOptionSelected: (SortOption, SortDirection) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Sort By", fontWeight = FontWeight.Bold, color = Color.White)
        },
        text = {
            Column {
                Text("Field", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = NeonCyan)
                Spacer(modifier = Modifier.height(4.dp))
                SortOption.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOptionSelected(option, currentDirection) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (option == currentOption),
                            onClick = { onOptionSelected(option, currentDirection) },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                        )
                        Text(option.displayName, color = Color.White, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(8.dp))

                Text("Order", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = NeonCyan)
                Spacer(modifier = Modifier.height(4.dp))
                SortDirection.entries.forEach { dir ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOptionSelected(currentOption, dir) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (dir == currentDirection),
                            onClick = { onOptionSelected(currentOption, dir) },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                        )
                        Text(
                            if (dir == SortDirection.ASCENDING) "Ascending (A-Z, Small-Big, Old-New)"
                            else "Descending (Z-A, Big-Small, New-Old)",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = NeonCyan)
            }
        }
    )
}

@Composable
fun SleepTimerDialog(
    activeMinutes: Int?,
    onSetTimer: (Int?) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(10, 15, 30, 45, 60, 90)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sleep Timer", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column {
                // Option to turn off
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSetTimer(null)
                            onDismiss()
                        }
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Turn Off", color = Color.White, fontSize = 15.sp)
                    if (activeMinutes == null) Text("✓", color = NeonCyan, fontWeight = FontWeight.Bold)
                }

                options.forEach { mins ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSetTimer(mins)
                                onDismiss()
                            }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("$mins Minutes", color = Color.White, fontSize = 15.sp)
                        if (activeMinutes == mins) Text("✓", color = NeonCyan, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = NeonCyan)
            }
        }
    )
}
