package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.t
import com.example.ui.theme.CodeBlockBg
import com.example.ui.theme.CodeBlockBorder

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val context = LocalContext.current
    val copyLabel = t("chat.copy")
    val copiedLabel = t("chat.copied")

    // Simple markdown block parser
    val blocks = splitMarkdownBlocks(text)

    Column(modifier = modifier) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.CodeBlock -> {
                    CodeBlockView(
                        language = block.language,
                        code = block.content,
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("code", block.content))
                            Toast.makeText(context, copiedLabel, Toast.LENGTH_SHORT).show()
                        },
                        copyLabel = copyLabel
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                is MarkdownBlock.HeaderBlock -> {
                    val style = when (block.level) {
                        1 -> MaterialTheme.typography.headlineMedium
                        2 -> MaterialTheme.typography.titleLarge
                        else -> MaterialTheme.typography.titleMedium
                    }
                    Text(
                        text = parseInlineMarkdown(block.content),
                        style = style,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                is MarkdownBlock.BulletItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = parseInlineMarkdown(block.content),
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor
                        )
                    }
                }
                is MarkdownBlock.ChecklistItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (block.isChecked) "✓ " else "□ ",
                            color = if (block.isChecked) MaterialTheme.colorScheme.primary else textColor.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = parseInlineMarkdown(block.content),
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor
                        )
                    }
                }
                is MarkdownBlock.Paragraph -> {
                    if (block.content.isNotBlank()) {
                        Text(
                            text = parseInlineMarkdown(block.content),
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeBlockView(
    language: String,
    code: String,
    onCopy: () -> Unit,
    copyLabel: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CodeBlockBg)
            .border(1.dp, CodeBlockBorder, RoundedCornerShape(8.dp))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF111827))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.ifBlank { "code" },
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF9CA3AF)
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = onCopy,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("copy_code_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = copyLabel,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Code Content
        Text(
            text = code,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            color = Color(0xFFE5E7EB),
            lineHeight = 18.sp,
            modifier = Modifier.padding(12.dp)
        )
    }
}

private sealed class MarkdownBlock {
    data class CodeBlock(val language: String, val content: String) : MarkdownBlock()
    data class HeaderBlock(val level: Int, val content: String) : MarkdownBlock()
    data class BulletItem(val content: String) : MarkdownBlock()
    data class ChecklistItem(val isChecked: Boolean, val content: String) : MarkdownBlock()
    data class Paragraph(val content: String) : MarkdownBlock()
}

private fun splitMarkdownBlocks(raw: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = raw.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        if (line.trimStart().startsWith("```")) {
            val lang = line.trim().removePrefix("```").trim()
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            if (i < lines.size && lines[i].trimStart().startsWith("```")) {
                i++
            }
            blocks.add(MarkdownBlock.CodeBlock(lang, codeLines.joinToString("\n")))
            continue
        }

        val trimmed = line.trim()
        when {
            trimmed.startsWith("### ") -> {
                blocks.add(MarkdownBlock.HeaderBlock(3, trimmed.removePrefix("### ").trim()))
            }
            trimmed.startsWith("## ") -> {
                blocks.add(MarkdownBlock.HeaderBlock(2, trimmed.removePrefix("## ").trim()))
            }
            trimmed.startsWith("# ") -> {
                blocks.add(MarkdownBlock.HeaderBlock(1, trimmed.removePrefix("# ").trim()))
            }
            trimmed.startsWith("- [ ] ") -> {
                blocks.add(MarkdownBlock.ChecklistItem(false, trimmed.removePrefix("- [ ] ").trim()))
            }
            trimmed.startsWith("- [x] ") || trimmed.startsWith("- [X] ") -> {
                blocks.add(MarkdownBlock.ChecklistItem(true, trimmed.substring(6).trim()))
            }
            trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                blocks.add(MarkdownBlock.BulletItem(trimmed.substring(2).trim()))
            }
            else -> {
                blocks.add(MarkdownBlock.Paragraph(line))
            }
        }
        i++
    }
    return blocks
}

private fun parseInlineMarkdown(text: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        val boldPattern = "\\*\\*(.*?)\\*\\*".toRegex()
        val matches = boldPattern.findAll(text)

        for (match in matches) {
            if (match.range.first > cursor) {
                append(text.substring(cursor, match.range.first))
            }
            val boldContent = match.groupValues[1]
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(boldContent)
            }
            cursor = match.range.last + 1
        }
        if (cursor < text.length) {
            append(text.substring(cursor))
        }
    }
}
