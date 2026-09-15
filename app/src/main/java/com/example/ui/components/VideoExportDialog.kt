package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.audio.GuitarHeroVideoExporter
import com.example.model.GuitarHeroNote
import com.example.model.GuitarHeroSectionMarker
import com.example.model.PracticeSong
import com.example.model.SongSection
import kotlinx.coroutines.launch
import java.io.File

enum class ExportRangeOption {
    ACTIVE_SECTION,
    HIGHLIGHT_10S,
    FULL_SONG
}

@Composable
fun VideoExportDialog(
    song: PracticeSong,
    activeSection: SongSection?,
    notes: List<GuitarHeroNote>,
    sectionMarkers: List<GuitarHeroSectionMarker>,
    currentAudioTimeSec: Float,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedOption by remember { mutableStateOf(ExportRangeOption.ACTIVE_SECTION) }
    var isExporting by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableIntStateOf(0) }
    var exportResult by remember { mutableStateOf<GuitarHeroVideoExporter.ExportResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = {
            if (!isExporting) onDismiss()
        },
        containerColor = Color(0xFF131B2E),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B)
                    )
                    Text(
                        text = "Export Video Guitar Hero",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (!isExporting) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color.Gray
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (exportResult != null) {
                    // Export Complete State
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = "Video Berhasil Diexport!",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        // File details card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Resolusi: 720p HD (720x1280, 30 FPS)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF38BDF8)
                                )
                                Text(
                                    text = "Ukuran: ${exportResult?.fileSizeFormatted} (Bitrate Ringkas)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF10B981)
                                )
                                Text(
                                    text = "Lokasi: ${exportResult?.file?.name}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        // Play & Share Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    exportResult?.file?.let { file ->
                                        openVideo(context, file)
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircleOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Putar")
                            }

                            Button(
                                onClick = {
                                    exportResult?.file?.let { file ->
                                        shareVideo(context, file)
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF59E0B),
                                    contentColor = Color(0xFF0F172A)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Bagikan", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else if (isExporting) {
                    // Export in progress
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.size(48.dp)
                        )

                        Text(
                            text = "Merender video Guitar Hero 720p...",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )

                        LinearProgressIndicator(
                            progress = { exportProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFFF59E0B),
                            trackColor = Color(0xFF1E293B)
                        )

                        Text(
                            text = "$exportProgress% selesai",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                } else {
                    // Configuration state before export
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Pilih Bagian yang Ingin Diexport:",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF94A3B8)
                        )

                        // Option 1: Active Section
                        RangeOptionCard(
                            title = "Bagian Aktif: ${activeSection?.name ?: "Intro"}",
                            subtitle = "Durasi: ${activeSection?.durationSec?.toInt() ?: 12} detik (Mulai: ${(activeSection?.startTimeSec ?: 0f).toInt()}s)",
                            isSelected = selectedOption == ExportRangeOption.ACTIVE_SECTION,
                            onClick = { selectedOption = ExportRangeOption.ACTIVE_SECTION }
                        )

                        // Option 2: 10s Highlight
                        RangeOptionCard(
                            title = "Klip Sorotan (10 Detik)",
                            subtitle = "Detik ${currentAudioTimeSec.toInt()}s - ${(currentAudioTimeSec + 10f).toInt()}s",
                            isSelected = selectedOption == ExportRangeOption.HIGHLIGHT_10S,
                            onClick = { selectedOption = ExportRangeOption.HIGHLIGHT_10S }
                        )

                        // Technical Specs Badge
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F172A))
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Spesifikasi Output Video:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                                Text(
                                    text = "• Resolusi: 720p HD (720x1280)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = Color(0xFFE2E8F0)
                                )
                                Text(
                                    text = "• Codec: H.264 / MP4 (Kompatibel semua pemutar)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = Color(0xFFE2E8F0)
                                )
                                Text(
                                    text = "• Bitrate: 2.5 Mbps (Ukuran hemat, kualitas jernih)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                        }

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (exportResult == null && !isExporting) {
                Button(
                    onClick = {
                        isExporting = true
                        errorMessage = null
                        scope.launch {
                            try {
                                val (startSec, durSec) = when (selectedOption) {
                                    ExportRangeOption.ACTIVE_SECTION -> {
                                        val s = activeSection?.startTimeSec ?: 0f
                                        val d = (activeSection?.durationSec ?: 12f).coerceIn(4f, 30f)
                                        Pair(s, d)
                                    }
                                    ExportRangeOption.HIGHLIGHT_10S -> {
                                        Pair(currentAudioTimeSec, 10f)
                                    }
                                    ExportRangeOption.FULL_SONG -> {
                                        Pair(0f, song.totalDurationSec.coerceIn(10f, 60f))
                                    }
                                }

                                val result = GuitarHeroVideoExporter.exportVideo(
                                    context = context,
                                    song = song,
                                    notes = notes,
                                    sectionMarkers = sectionMarkers,
                                    startTimeSec = startSec,
                                    durationSec = durSec,
                                    onProgress = { percent ->
                                        exportProgress = percent
                                    }
                                )
                                exportResult = result
                            } catch (e: Exception) {
                                errorMessage = "Gagal export: ${e.localizedMessage}"
                            } finally {
                                isExporting = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF59E0B),
                        contentColor = Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_export_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mulai Export", fontWeight = FontWeight.Bold)
                }
            } else if (exportResult != null) {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Selesai")
                }
            }
        },
        dismissButton = {
            if (!isExporting && exportResult == null) {
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

@Composable
private fun RangeOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A))
            .border(
                if (isSelected) 1.5.dp else 1.dp,
                if (isSelected) Color(0xFFF59E0B) else Color(0xFF334155),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color(0xFFF59E0B) else Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .border(2.dp, if (isSelected) Color(0xFFF59E0B) else Color.Gray, CircleShape)
                    .background(if (isSelected) Color(0xFFF59E0B) else Color.Transparent)
            )
        }
    }
}

private fun openVideo(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "video/mp4")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback
    }
}

private fun shareVideo(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Bagikan Video Guitar Hero"))
    } catch (e: Exception) {
        // Fallback
    }
}
