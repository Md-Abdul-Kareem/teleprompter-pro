package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.PrompterSettingsSheet
import com.example.ui.components.bounceClick
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioBlue
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.viewmodel.TeleprompterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: TeleprompterViewModel,
    onNavigateToPrompter: () -> Unit,
    onNavigateToSavedScripts: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val title by viewModel.currentTitle.collectAsStateWithLifecycle()
    val content by viewModel.currentContent.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val allScripts by viewModel.allScripts.collectAsStateWithLifecycle()

    var showSettingsSheet by remember { mutableStateOf(false) }

    val wordCount = remember(content) { TeleprompterViewModel.calculateWordCount(content) }
    val readingTime = remember(wordCount) {
        val wpm = (settings.scrollSpeed * 3.5).toInt().coerceAtLeast(60)
        TeleprompterViewModel.calculateReadingTime(wordCount, wpm)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(StudioPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Teleprompter Pro",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = StudioTextPrimary
                        )
                    }
                },
                actions = {
                    // New script button
                    IconButton(
                        onClick = { viewModel.createNewScript() },
                        modifier = Modifier.bounceClick { viewModel.createNewScript() }.testTag("new_script_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Script",
                            tint = StudioBlue
                        )
                    }

                    // Saved scripts button with count badge
                    BadgedBox(
                        badge = {
                            if (allScripts.isNotEmpty()) {
                                Badge(
                                    containerColor = StudioCyan,
                                    contentColor = Color.Black
                                ) {
                                    Text("${allScripts.size}")
                                }
                            }
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateToSavedScripts,
                            modifier = Modifier.bounceClick { onNavigateToSavedScripts() }.testTag("saved_scripts_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Saved Scripts",
                                tint = StudioTextPrimary
                            )
                        }
                    }

                    // Settings sheet button
                    IconButton(
                        onClick = { showSettingsSheet = true },
                        modifier = Modifier.bounceClick { showSettingsSheet = true }.testTag("editor_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Settings",
                            tint = StudioTextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = StudioSurface
                )
            )
        },
        bottomBar = {
            Surface(
                color = StudioSurface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .imePadding(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Save Script Button
                    FilledTonalButton(
                        onClick = {
                            viewModel.saveCurrentScript {
                                Toast.makeText(context, "Script Saved!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .bounceClick {
                                viewModel.saveCurrentScript {
                                    Toast.makeText(context, "Script Saved!", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .testTag("save_script_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Script", fontWeight = FontWeight.SemiBold)
                    }

                    // Start Teleprompter (Primary Action)
                    Button(
                        onClick = {
                            if (content.isBlank()) {
                                Toast.makeText(context, "Please write or paste a script first", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.saveCurrentScript()
                                onNavigateToPrompter()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudioBlue,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(52.dp)
                            .bounceClick {
                                if (content.isBlank()) {
                                    Toast.makeText(context, "Please write or paste a script first", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.saveCurrentScript()
                                    onNavigateToPrompter()
                                }
                            }
                            .testTag("start_teleprompter_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Start Prompter",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        },
        containerColor = StudioBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.updateTitle(it) },
                placeholder = { Text("Script Title (e.g. Product Launch Video)", color = StudioTextMuted) },
                singleLine = true,
                maxLines = 1,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                trailingIcon = {
                    if (title.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateTitle("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Title", tint = StudioTextMuted)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StudioBlue,
                    unfocusedBorderColor = StudioBorder,
                    focusedTextColor = StudioTextPrimary,
                    unfocusedTextColor = StudioTextPrimary,
                    focusedContainerColor = StudioSurface,
                    unfocusedContainerColor = StudioSurface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("script_title_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Live Metrics & Quick Tools Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(StudioCard)
                    .border(1.dp, StudioBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Word Count & Reading Time
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetricPill(label = "Words", value = "$wordCount", color = StudioCyan)
                    MetricPill(label = "Est. Read", value = readingTime, color = StudioAmber)
                }

                // Paste and Clear actions
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipData = clipboard.primaryClip
                            if (clipData != null && clipData.itemCount > 0) {
                                val pasted = clipData.getItemAt(0).text?.toString() ?: ""
                                if (pasted.isNotBlank()) {
                                    val current = viewModel.currentContent.value
                                    viewModel.updateContent(if (current.isBlank()) pasted else "$current\n\n$pasted")
                                    Toast.makeText(context, "Pasted from clipboard", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.size(34.dp).testTag("paste_script_button")
                    ) {
                        Icon(
                            Icons.Default.ContentPaste,
                            contentDescription = "Paste text",
                            tint = StudioBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (content.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.updateContent("") },
                            modifier = Modifier.size(34.dp).testTag("clear_script_button")
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear content",
                                tint = StudioTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Template Quick-Starters (If script is empty)
            if (content.isBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(StudioSurfaceVariant.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Quick Starter Templates:",
                        color = StudioTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TemplateChip(title = "Keynote Speech") { viewModel.loadTemplate("SPEECH") }
                        TemplateChip(title = "YouTube Hook") { viewModel.loadTemplate("YOUTUBE") }
                        TemplateChip(title = "Podcast Intro") { viewModel.loadTemplate("PODCAST") }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Multiline Script Editor
            OutlinedTextField(
                value = content,
                onValueChange = { viewModel.updateContent(it) },
                placeholder = {
                    Text(
                        "Type or paste your script here...\n\n" +
                                "• Paragraphs will automatically format for seamless auto-scrolling.\n" +
                                "• Use Mirror Mode for physical glass prompters.\n" +
                                "• Enable the camera overlay to look directly at your lens while speaking.",
                        color = StudioTextMuted,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StudioBlue,
                    unfocusedBorderColor = StudioBorder,
                    focusedTextColor = StudioTextPrimary,
                    unfocusedTextColor = StudioTextPrimary,
                    focusedContainerColor = StudioSurface,
                    unfocusedContainerColor = StudioSurface
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("script_content_editor")
            )
        }
    }

    if (showSettingsSheet) {
        PrompterSettingsSheet(
            settings = settings,
            onSettingsChanged = { viewModel.updateSettings(it) },
            onDismiss = { showSettingsSheet = false }
        )
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "$label:",
            color = StudioTextMuted,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TemplateChip(
    title: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(StudioCard)
            .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
            .bounceClick(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = "+ $title",
            color = StudioBlue,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
