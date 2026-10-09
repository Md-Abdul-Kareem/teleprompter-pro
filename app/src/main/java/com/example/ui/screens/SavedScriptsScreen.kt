package com.example.ui.screens

import android.text.format.DateUtils
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.bounceClick
import com.example.data.local.ScriptEntity
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioBlue
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioRecordingRed
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.viewmodel.TeleprompterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScriptsScreen(
    viewModel: TeleprompterViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPrompter: () -> Unit
) {
    val context = LocalContext.current
    val scripts by viewModel.allScripts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var scriptToDelete by remember { mutableStateOf<ScriptEntity?>(null) }
    var scriptToRename by remember { mutableStateOf<ScriptEntity?>(null) }
    var renameTitleInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Saved Scripts",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = StudioTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("saved_scripts_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = StudioTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.createNewScript()
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("create_script_fab")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New Script", tint = StudioBlue)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = StudioSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.createNewScript()
                    onNavigateBack()
                },
                containerColor = StudioBlue,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier
                    .bounceClick {
                        viewModel.createNewScript()
                        onNavigateBack()
                    }
                    .testTag("fab_add_new_script")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create New Script", tint = Color.Black)
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
            // Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search your scripts...", color = StudioTextMuted) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = StudioTextMuted)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = StudioTextMuted)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StudioBlue,
                    unfocusedBorderColor = StudioBorder,
                    focusedTextColor = StudioTextPrimary,
                    unfocusedTextColor = StudioTextPrimary,
                    focusedContainerColor = StudioSurface,
                    unfocusedContainerColor = StudioSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_scripts_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (scripts.isEmpty()) {
                // Empty State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(StudioCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = StudioBlue.copy(alpha = 0.7f),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No scripts found matching '$searchQuery'" else "No Saved Scripts Yet",
                            color = StudioTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Try searching for a different keyword" else "Create or paste your first script to get started.",
                            color = StudioTextSecondary,
                            fontSize = 13.sp
                        )
                        if (searchQuery.isBlank()) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    viewModel.createNewScript()
                                    onNavigateBack()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StudioBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create Script", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(scripts, key = { it.id }) { script ->
                        val relativeTime = DateUtils.getRelativeTimeSpanString(
                            script.updatedAt,
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS
                        ).toString()

                        val words = remember(script.content) { TeleprompterViewModel.calculateWordCount(script.content) }
                        val readTime = remember(words) {
                            val wpm = (settings.scrollSpeed * 3.5).toInt().coerceAtLeast(60)
                            TeleprompterViewModel.calculateReadingTime(words, wpm)
                        }

                        ScriptCard(
                            script = script,
                            relativeTime = relativeTime,
                            wordCount = words,
                            readingTime = readTime,
                            onClick = {
                                viewModel.loadScript(script)
                                onNavigateBack()
                            },
                            onStartPrompter = {
                                viewModel.loadScript(script)
                                onNavigateToPrompter()
                            },
                            onDuplicate = {
                                viewModel.duplicateScript(script)
                                Toast.makeText(context, "Script Duplicated", Toast.LENGTH_SHORT).show()
                            },
                            onRename = {
                                scriptToRename = script
                                renameTitleInput = script.title
                            },
                            onDelete = {
                                scriptToDelete = script
                            }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    scriptToDelete?.let { script ->
        AlertDialog(
            onDismissRequest = { scriptToDelete = null },
            containerColor = StudioSurface,
            title = {
                Text(
                    text = "Delete Script?",
                    color = StudioRecordingRed,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete \"${script.title}\"?",
                    color = StudioTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteScript(script)
                        scriptToDelete = null
                        Toast.makeText(context, "Script deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioRecordingRed),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { scriptToDelete = null }) {
                    Text("Cancel", color = StudioTextPrimary)
                }
            }
        )
    }

    // Rename Dialog
    scriptToRename?.let { script ->
        AlertDialog(
            onDismissRequest = { scriptToRename = null },
            containerColor = StudioSurface,
            title = {
                Text(
                    text = "Rename Script",
                    color = StudioTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = renameTitleInput,
                    onValueChange = { renameTitleInput = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioBlue,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = StudioTextPrimary,
                        unfocusedTextColor = StudioTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newTitle = renameTitleInput.trim().ifEmpty { script.title }
                        viewModel.renameScript(script, newTitle)
                        scriptToRename = null
                        Toast.makeText(context, "Script renamed", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { scriptToRename = null }) {
                    Text("Cancel", color = StudioTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun ScriptCard(
    script: ScriptEntity,
    relativeTime: String,
    wordCount: Int,
    readingTime: String,
    onClick: () -> Unit,
    onStartPrompter: () -> Unit,
    onDuplicate: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        color = StudioCard,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("script_card_${script.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = script.title.ifBlank { "Untitled Script" },
                    color = StudioTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Script options",
                            tint = StudioTextMuted
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(StudioSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit / Load in Editor", color = StudioTextPrimary) },
                            onClick = {
                                showMenu = false
                                onClick()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = StudioBlue)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Rename", color = StudioTextPrimary) },
                            onClick = {
                                showMenu = false
                                onRename()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = StudioCyan)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate", color = StudioTextPrimary) },
                            onClick = {
                                showMenu = false
                                onDuplicate()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = StudioTextSecondary)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = StudioRecordingRed) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = StudioRecordingRed)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Script Preview snippet
            Text(
                text = script.content.ifBlank { "(Empty script)" },
                color = StudioTextSecondary,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom stats & Direct Play Prompter Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Metrics
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$wordCount words",
                        color = StudioCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "•",
                        color = StudioBorder,
                        fontSize = 12.sp
                    )
                    Text(
                        text = readingTime,
                        color = StudioAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "•",
                        color = StudioBorder,
                        fontSize = 12.sp
                    )
                    Text(
                        text = relativeTime,
                        color = StudioTextMuted,
                        fontSize = 11.sp
                    )
                }

                // Quick Prompter Play Button
                IconButton(
                    onClick = onStartPrompter,
                    modifier = Modifier
                        .size(34.dp)
                        .background(StudioBlue, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start teleprompter for this script",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
