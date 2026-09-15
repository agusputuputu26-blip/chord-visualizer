package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChordItem
import com.example.model.ChordLibrary
import com.example.model.SongSection

@Composable
fun SectionEditorDialog(
    sectionToEdit: SongSection?, // null if adding new
    currentAudioTimeSec: Float,
    onDismiss: () -> Unit,
    onSaveNewSection: (name: String, startTimeSec: Float, chords: List<String>) -> Unit,
    onUpdateSection: (SongSection) -> Unit,
    onDeleteSection: ((String) -> Unit)? = null
) {
    val isEditMode = sectionToEdit != null

    var name by remember { mutableStateOf(sectionToEdit?.name ?: "Verse 1") }
    var startSec by remember {
        mutableStateOf(sectionToEdit?.startTimeSec ?: currentAudioTimeSec.toInt().toFloat())
    }
    var durationSec by remember {
        mutableStateOf(sectionToEdit?.durationSec ?: 16f)
    }

    var selectedChords by remember {
        mutableStateOf(
            sectionToEdit?.chords?.map { it.name } ?: listOf("G", "Em", "C", "D")
        )
    }

    val availableChords = listOf("G", "Em", "C", "D", "Am", "F", "Bm", "A7", "D7", "E7")
    val presetSectionNames = listOf("Intro", "Verse 1", "Verse 2", "Chorus", "Solo", "Interlude", "Bridge", "Outro")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditMode) "Edit Bagian Lagu" else "Tambah Bagian Lagu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = Color.Gray
                    )
                }
            }
        },
        containerColor = Color(0xFF131B2E),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Quick suggested names
                Text(
                    text = "Pilih / Ketik Nama Bagian:",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF94A3B8)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetSectionNames.forEach { presetName ->
                        val isSelected = name.equals(presetName, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFFF59E0B) else Color(0xFF1E293B))
                                .clickable { name = presetName }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = presetName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color(0xFF0F172A) else Color.White
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Bagian") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFF59E0B),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Timestamp start & "Use Current Audio Time"
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Waktu Mulai: ${startSec.toInt()} detik (${formatTime(startSec)})",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF94A3B8)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { startSec = (startSec - 1).coerceAtLeast(0f) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(36.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "-1s", tint = Color.White)
                        }

                        Text(
                            text = "${startSec.toInt()}s",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        OutlinedButton(
                            onClick = { startSec += 1 },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(36.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "+1s", tint = Color.White)
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Grab current audio position button
                        Button(
                            onClick = { startSec = currentAudioTimeSec },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E293B),
                                contentColor = Color(0xFF38BDF8)
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Waktu Sekarang (${currentAudioTimeSec.toInt()}s)",
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Chord Progression builder
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Barisan Kord (Chords):",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF94A3B8)
                    )

                    // Current chosen chord sequence
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        selectedChords.forEachIndexed { idx, chord ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2E2413))
                                    .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(8.dp))
                                    .clickable {
                                        // Remove on click
                                        selectedChords = selectedChords.filterIndexed { i, _ -> i != idx }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = chord,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF59E0B)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("✕", color = Color.Gray, fontSize = 10.sp)
                                }
                            }
                        }
                        if (selectedChords.isEmpty()) {
                            Text(
                                text = "Belum ada kord. Tap di bawah untuk menambah.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    // Available chords palette to tap and append
                    Text(
                        text = "Tap untuk menambah kord:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableChords.forEach { chord ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E293B))
                                    .clickable {
                                        selectedChords = selectedChords + chord
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "+ $chord",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val chords = if (selectedChords.isNotEmpty()) selectedChords else listOf("G")
                    if (isEditMode && sectionToEdit != null) {
                        val updated = sectionToEdit.copy(
                            name = name,
                            startTimeSec = startSec,
                            endTimeSec = startSec + (chords.size * 4f),
                            chords = chords.map { cName ->
                                val gc = ChordLibrary.getChord(cName)
                                ChordItem(name = gc.chordName, beats = 4, fingering = gc)
                            }
                        )
                        onUpdateSection(updated)
                    } else {
                        onSaveNewSection(name, startSec, chords)
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF59E0B),
                    contentColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_section_button")
            ) {
                Text(
                    text = "Simpan Bagian",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isEditMode && onDeleteSection != null && sectionToEdit != null) {
                    OutlinedButton(
                        onClick = {
                            onDeleteSection(sectionToEdit.id)
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hapus")
                    }
                }
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Batal")
                }
            }
        }
    )
}

private fun formatTime(seconds: Float): String {
    val totalSec = seconds.toInt().coerceAtLeast(0)
    val mins = totalSec / 60
    val secs = totalSec % 60
    return String.format("%02d:%02d", mins, secs)
}
