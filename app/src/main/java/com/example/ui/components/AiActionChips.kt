package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.AiActionItem
import com.example.i18n.t

val workspaceActions = listOf(
    AiActionItem("act-summarize", "action.summarize", "ช่วยสรุปเนื้อหาสำคัญและประเด็นหลักให้หน่อย", "sparkle"),
    AiActionItem("act-checklist", "action.checklist", "ช่วยสร้าง Checklist การทำงานแบบทีละขั้นตอนให้หน่อย", "checklist"),
    AiActionItem("act-doc", "action.document", "ช่วยร่างโครงสร้างเอกสารสรุปผลการทำงานให้หน่อย", "doc"),
    AiActionItem("act-analyze", "action.analyze", "ช่วยวิเคราะห์ข้อมูลและหาข้อเสนอแนะเชิงลึกให้หน่อย", "stats"),
    AiActionItem("act-table", "action.table", "ช่วยจัดระเบียบข้อมูลนี้ให้อยู่ในรูปแบบตาราง Markdown", "table"),
    AiActionItem("act-code", "action.code", "ช่วยเขียนโค้ดตัวอย่างการนำไปใช้งานจริงให้หน่อย", "code")
)

@Composable
fun AiActionChips(
    onActionSelected: (AiActionItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        workspaceActions.forEach { action ->
            val icon = getActionIcon(action.iconType)
            FilterChip(
                selected = false,
                onClick = { onActionSelected(action) },
                label = { Text(text = t(action.titleKey), style = MaterialTheme.typography.labelMedium) },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = false,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                ),
                modifier = Modifier.testTag("action_${action.id}")
            )
        }
    }
}

private fun getActionIcon(iconType: String): ImageVector {
    return when (iconType) {
        "checklist" -> Icons.Default.Checklist
        "doc" -> Icons.Default.Description
        "stats" -> Icons.Default.QueryStats
        "table" -> Icons.Default.TableChart
        "code" -> Icons.Default.Code
        else -> Icons.Default.AutoAwesome
    }
}
