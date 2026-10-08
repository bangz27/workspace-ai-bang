package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.i18n.Language
import com.example.i18n.t
import com.example.ui.viewmodel.WorkspaceViewModel

@Composable
fun SettingsScreen(
    viewModel: WorkspaceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTheme by viewModel.selectedTheme.collectAsState()
    val currentLanguage by viewModel.selectedLanguage.collectAsState()
    val currentModel by viewModel.selectedModel.collectAsState()
    val customKey by viewModel.customApiKey.collectAsState()

    var showClearDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 72.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = t("settings.title"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // 1. Appearance Section
        SettingsSection(title = t("settings.appearance"), icon = Icons.Default.SettingsBrightness) {
            // Theme selection
            Text(
                text = t("settings.theme"),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = currentTheme == "dark",
                    onClick = { viewModel.setTheme("dark") },
                    label = { Text(t("settings.themeDark")) },
                    leadingIcon = { Icon(Icons.Default.DarkMode, null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f).testTag("theme_dark")
                )
                FilterChip(
                    selected = currentTheme == "light",
                    onClick = { viewModel.setTheme("light") },
                    label = { Text(t("settings.themeLight")) },
                    leadingIcon = { Icon(Icons.Default.LightMode, null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f).testTag("theme_light")
                )
                FilterChip(
                    selected = currentTheme == "system",
                    onClick = { viewModel.setTheme("system") },
                    label = { Text(t("settings.themeSystem")) },
                    modifier = Modifier.weight(1f).testTag("theme_system")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Language selection
            Text(
                text = t("settings.language"),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = currentLanguage == Language.TH,
                    onClick = { viewModel.setLanguage(Language.TH) },
                    label = { Text("ภาษาไทย (Thai)") },
                    leadingIcon = { Icon(Icons.Default.Language, null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f).testTag("lang_th")
                )
                FilterChip(
                    selected = currentLanguage == Language.EN,
                    onClick = { viewModel.setLanguage(Language.EN) },
                    label = { Text("English") },
                    modifier = Modifier.weight(1f).testTag("lang_en")
                )
            }
        }

        // 2. AI Section
        SettingsSection(title = t("settings.aiSection"), icon = Icons.Default.AutoAwesome) {
            Text(
                text = t("settings.modelDefault"),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setModel("gemini-3.5-flash") }
                ) {
                    RadioButton(
                        selected = currentModel == "gemini-3.5-flash",
                        onClick = { viewModel.setModel("gemini-3.5-flash") }
                    )
                    Text(text = t("settings.modelFast"), style = MaterialTheme.typography.bodyMedium)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setModel("gemini-3.1-pro-preview") }
                ) {
                    RadioButton(
                        selected = currentModel == "gemini-3.1-pro-preview",
                        onClick = { viewModel.setModel("gemini-3.1-pro-preview") }
                    )
                    Text(text = t("settings.modelAdvanced"), style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Custom API Key Card
            OutlinedButton(
                onClick = { showApiKeyDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (customKey.isNotBlank()) "Gemini API Key: กำหนดเองแล้ว" else t("settings.customApiKey")
                )
            }
        }

        // 3. Data Section
        SettingsSection(title = t("settings.dataSection"), icon = Icons.Default.Storage) {
            // Export Chat
            OutlinedButton(
                onClick = {
                    val content = viewModel.exportCurrentChat(asMarkdown = true)
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("AI Workspace Export", content))
                    Toast.makeText(context, "คัดลอกข้อมูลแชทรูปแบบ Markdown เรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(t("settings.exportData"))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Reset Demo Data
            OutlinedButton(
                onClick = {
                    viewModel.resetToDemoData()
                    Toast.makeText(context, "โหลดข้อมูลตัวอย่างเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(t("settings.resetDemo"))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Clear Data
            OutlinedButton(
                onClick = { showClearDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(t("settings.clearData"))
            }
        }

        // 4. App Info Section
        SettingsSection(title = t("settings.appSection"), icon = Icons.Default.Info) {
            Text(
                text = t("settings.version"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = t("settings.offlineStatus"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    // Clear Data Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(t("settings.clearData")) },
            text = { Text("คุณแน่ใจหรือไม่ว่าต้องการล้างข้อมูลแชทและโปรเจกต์ทั้งหมดในเครื่อง?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(t("project.delete"))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearDialog = false }) {
                    Text(t("project.cancel"))
                }
            }
        )
    }

    // Custom API Key Dialog
    if (showApiKeyDialog) {
        var keyInput by remember { mutableStateOf(customKey) }
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text(t("settings.customApiKey")) },
            text = {
                Column {
                    Text(
                        text = t("settings.apiKeyHint"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        placeholder = { Text("AIzaSy...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCustomApiKey(keyInput)
                        showApiKeyDialog = false
                        Toast.makeText(context, "บันทึก Gemini API Key เรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(t("settings.saveApiKey"))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showApiKeyDialog = false }) {
                    Text(t("project.cancel"))
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}
