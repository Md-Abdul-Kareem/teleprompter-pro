package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.PrompterSettings
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioBlue
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioSecondary
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextGreen
import com.example.ui.theme.TextOrange
import com.example.ui.theme.TextPink
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TextYellow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PrompterSettingsSheet(
    settings: PrompterSettings,
    onSettingsChanged: (PrompterSettings) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val colorPresets = listOf(
        "#FFFFFF" to TextWhite,
        "#FDE047" to TextYellow,
        "#67E8F9" to TextCyan,
        "#86EFAC" to TextGreen,
        "#FB923C" to TextOrange,
        "#F472B6" to TextPink
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(StudioBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 36.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Prompter Studio Settings",
                    color = StudioTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close settings",
                        tint = StudioTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live Preview Card
            Text(
                text = "LIVE PREVIEW",
                color = StudioBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            val previewColor = try {
                Color(android.graphics.Color.parseColor(settings.textColorHex))
            } catch (e: Exception) {
                Color.White
            }

            val previewAlign = when (settings.textAlignment) {
                "LEFT" -> TextAlign.Left
                "RIGHT" -> TextAlign.Right
                else -> TextAlign.Center
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Color.Black.copy(
                            alpha = if (settings.isCameraEnabled) settings.overlayAlpha else 1f
                        )
                    )
                    .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = (settings.horizontalPadding / 2).dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Professional teleprompter text scrolling live in real-time.",
                    color = previewColor,
                    fontSize = (settings.fontSize * 0.55f).sp,
                    fontWeight = if (settings.isBold) FontWeight.Bold else FontWeight.Normal,
                    textAlign = previewAlign,
                    lineHeight = (settings.fontSize * 0.55f * settings.lineSpacing).sp,
                    modifier = Modifier.graphicsLayer(
                        scaleX = if (settings.isMirrored) -1f else 1f
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = StudioBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Scroll Speed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = StudioCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scroll Speed", color = StudioTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
                Text(
                    text = "${settings.scrollSpeed.toInt()} (${(settings.scrollSpeed * 3.5).toInt()} WPM)",
                    color = StudioCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = settings.scrollSpeed,
                onValueChange = { onSettingsChanged(settings.copy(scrollSpeed = it)) },
                valueRange = 5f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = StudioCyan,
                    activeTrackColor = StudioCyan,
                    inactiveTrackColor = StudioBorder
                ),
                modifier = Modifier.testTag("settings_speed_slider")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Font Size
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TextFields, contentDescription = null, tint = StudioBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Font Size", color = StudioTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
                Text(
                    text = "${settings.fontSize.toInt()} sp",
                    color = StudioBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = settings.fontSize,
                onValueChange = { onSettingsChanged(settings.copy(fontSize = it)) },
                valueRange = 18f..72f,
                colors = SliderDefaults.colors(
                    thumbColor = StudioBlue,
                    activeTrackColor = StudioBlue,
                    inactiveTrackColor = StudioBorder
                ),
                modifier = Modifier.testTag("settings_font_size_slider")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Text Color Presets
            Text(
                text = "Text Color",
                color = StudioTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                colorPresets.forEach { (hex, color) ->
                    val isSelected = settings.textColorHex.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) StudioBlue else StudioBorder,
                                shape = CircleShape
                            )
                            .clickable {
                                onSettingsChanged(settings.copy(textColorHex = hex))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = if (hex == "#FFFFFF" || hex == "#FDE047") Color.Black else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Background / Camera Overlay Darkness
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Background / Overlay Darkness",
                    color = StudioTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${(settings.overlayAlpha * 100).toInt()}%",
                    color = StudioTextSecondary,
                    fontSize = 14.sp
                )
            }
            Slider(
                value = settings.overlayAlpha,
                onValueChange = { onSettingsChanged(settings.copy(overlayAlpha = it)) },
                valueRange = 0.0f..1.0f,
                colors = SliderDefaults.colors(
                    thumbColor = StudioTextPrimary,
                    activeTrackColor = StudioTextPrimary,
                    inactiveTrackColor = StudioBorder
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Text Alignment & Bold Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Alignment & Style", color = StudioTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = { onSettingsChanged(settings.copy(textAlignment = "LEFT")) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (settings.textAlignment == "LEFT") StudioBlue.copy(alpha = 0.25f) else StudioCard,
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.FormatAlignLeft,
                            contentDescription = "Align Left",
                            tint = if (settings.textAlignment == "LEFT") StudioBlue else StudioTextSecondary
                        )
                    }

                    IconButton(
                        onClick = { onSettingsChanged(settings.copy(textAlignment = "CENTER")) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (settings.textAlignment == "CENTER") StudioBlue.copy(alpha = 0.25f) else StudioCard,
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.FormatAlignCenter,
                            contentDescription = "Align Center",
                            tint = if (settings.textAlignment == "CENTER") StudioBlue else StudioTextSecondary
                        )
                    }

                    IconButton(
                        onClick = { onSettingsChanged(settings.copy(textAlignment = "RIGHT")) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (settings.textAlignment == "RIGHT") StudioBlue.copy(alpha = 0.25f) else StudioCard,
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.FormatAlignRight,
                            contentDescription = "Align Right",
                            tint = if (settings.textAlignment == "RIGHT") StudioBlue else StudioTextSecondary
                        )
                    }

                    IconButton(
                        onClick = { onSettingsChanged(settings.copy(isBold = !settings.isBold)) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (settings.isBold) StudioBlue.copy(alpha = 0.25f) else StudioCard,
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.FormatBold,
                            contentDescription = "Bold",
                            tint = if (settings.isBold) StudioBlue else StudioTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Line Spacing & Side Margin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Line Spacing", color = StudioTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(String.format("%.1fx", settings.lineSpacing), color = StudioTextSecondary, fontSize = 14.sp)
            }
            Slider(
                value = settings.lineSpacing,
                onValueChange = { onSettingsChanged(settings.copy(lineSpacing = it)) },
                valueRange = 1.0f..2.5f,
                steps = 5,
                colors = SliderDefaults.colors(
                    thumbColor = StudioSecondary,
                    activeTrackColor = StudioSecondary,
                    inactiveTrackColor = StudioBorder
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Side Margin / Padding", color = StudioTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text("${settings.horizontalPadding.toInt()} dp", color = StudioTextSecondary, fontSize = 14.sp)
            }
            Slider(
                value = settings.horizontalPadding,
                onValueChange = { onSettingsChanged(settings.copy(horizontalPadding = it)) },
                valueRange = 8f..64f,
                colors = SliderDefaults.colors(
                    thumbColor = StudioSecondary,
                    activeTrackColor = StudioSecondary,
                    inactiveTrackColor = StudioBorder
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = StudioBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // 7. Toggle Switches: Mirror Mode, Eye Guide, Countdown
            // Mirror Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Flip, contentDescription = null, tint = StudioCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mirror Mode (Glass Rig)", color = StudioTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        "Horizontally flips text layer for beam-splitter glass",
                        color = StudioTextSecondary,
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = settings.isMirrored,
                    onCheckedChange = { onSettingsChanged(settings.copy(isMirrored = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = StudioCyan
                    ),
                    modifier = Modifier.testTag("switch_mirror_mode")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Eye Guide Line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RemoveRedEye, contentDescription = null, tint = StudioBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Eye Contact Guide Line", color = StudioTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        "Highlight marker to align gaze with camera lens",
                        color = StudioTextSecondary,
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = settings.showEyeGuide,
                    onCheckedChange = { onSettingsChanged(settings.copy(showEyeGuide = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = StudioBlue
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Countdown duration selector
            Text("Countdown Timer", color = StudioTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(0 to "Off", 3 to "3s", 5 to "5s", 10 to "10s").forEach { (sec, label) ->
                    val isSelected = settings.countdownSeconds == sec
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSettingsChanged(settings.copy(countdownSeconds = sec)) },
                        label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudioBlue,
                            selectedLabelColor = Color.Black,
                            containerColor = StudioCard,
                            labelColor = StudioTextPrimary
                        )
                    )
                }
            }
        }
    }
}
