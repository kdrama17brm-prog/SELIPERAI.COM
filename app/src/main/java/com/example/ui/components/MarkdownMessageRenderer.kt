package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalSeliperTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    data class QuoteBlock(val text: String) : MarkdownBlock()
    data class BulletList(val items: List<String>) : MarkdownBlock()
    data class NumberedList(val items: List<Pair<String, String>>) : MarkdownBlock()
    data class TableBlock(val headers: List<String>, val rows: List<List<String>>) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
}

/**
 * Renders structured Markdown in SELIPER AI responses:
 * - Headings (#, ##, ###)
 * - Fenced Code Blocks (```lang ... ```) with one-tap Copy Code button
 * - Blockquotes (> Reasoning Summary)
 * - Bullet & Numbered lists
 * - Markdown Tables (| Col1 | Col2 |)
 * - Inline **bold**, *italic*, and `inline code`
 */
@Composable
fun MarkdownMessageRenderer(
    markdown: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }
    val tokens = LocalSeliperTokens.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> {
                    val fontSize = when (block.level) {
                        1 -> 20.sp
                        2 -> 18.sp
                        else -> 16.5.sp
                    }
                    Text(
                        text = parseInlineMarkdown(block.text, tokens.accentCyan),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = fontSize,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                is MarkdownBlock.QuoteBlock -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(tokens.accentCyan.copy(alpha = 0.14f))
                            .border(1.dp, tokens.accentCyan.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(20.dp)
                                .background(tokens.accentCyan, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parseInlineMarkdown(block.text, tokens.accentCyan),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                is MarkdownBlock.CodeBlock -> {
                    CodeBlockCard(
                        language = block.language,
                        code = block.code
                    )
                }

                is MarkdownBlock.BulletList -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        block.items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "•  ",
                                    color = tokens.accentCyan,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = parseInlineMarkdown(item, tokens.accentCyan),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }

                is MarkdownBlock.NumberedList -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        block.items.forEach { (num, item) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "$num. ",
                                    color = tokens.accentCyan,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = parseInlineMarkdown(item, tokens.accentCyan),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }

                is MarkdownBlock.TableBlock -> {
                    MarkdownTableCard(headers = block.headers, rows = block.rows, textColor = Color.White)
                }

                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = parseInlineMarkdown(block.text, tokens.accentCyan),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeBlockCard(
    language: String,
    code: String
) {
    val tokens = LocalSeliperTokens.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var copied by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = tokens.codeBlockBackground,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, tokens.cardBorder, RoundedCornerShape(10.dp))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language.ifBlank { "code" }.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = tokens.accentCyan,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )
                IconButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(code))
                        copied = true
                        scope.launch {
                            delay(1800)
                            copied = false
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (copied) Icons.Default.Check else Icons.Outlined.ContentCopy,
                        contentDescription = "Copy code",
                        tint = if (copied) Color(0xFF00E676) else Color(0xFFB0BEC5),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            HorizontalDivider(color = tokens.cardBorder.copy(alpha = 0.5f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Text(
                    text = code,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFE2E8F0),
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun MarkdownTableCard(
    headers: List<String>,
    rows: List<List<String>>,
    textColor: Color
) {
    val tokens = LocalSeliperTokens.current
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = tokens.cardBackground,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, tokens.cardBorder, RoundedCornerShape(8.dp))
            .horizontalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier
                    .background(tokens.accentCyan.copy(alpha = 0.14f), RoundedCornerShape(6.dp))
                    .padding(vertical = 6.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                headers.forEach { h ->
                    Text(
                        text = h,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = tokens.accentCyan
                        ),
                        modifier = Modifier.width(120.dp)
                    )
                }
            }
            rows.forEachIndexed { idx, row ->
                if (idx > 0) {
                    HorizontalDivider(color = tokens.cardBorder.copy(alpha = 0.4f))
                }
                Row(
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    row.forEach { cell ->
                        Text(
                            text = parseInlineMarkdown(cell, tokens.accentCyan),
                            style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                            modifier = Modifier.width(120.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun parseMarkdownBlocks(raw: String): List<MarkdownBlock> {
    val lines = raw.lines()
    val blocks = mutableListOf<MarkdownBlock>()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()

        // 1. Code block
        if (trimmed.startsWith("```")) {
            val lang = trimmed.removePrefix("```").trim()
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            blocks.add(MarkdownBlock.CodeBlock(lang, codeLines.joinToString("\n")))
            i++
            continue
        }

        // 2. Empty line
        if (trimmed.isEmpty()) {
            i++
            continue
        }

        // 3. Heading
        if (trimmed.startsWith("#")) {
            val level = trimmed.takeWhile { it == '#' }.length.coerceIn(1, 4)
            val text = trimmed.drop(level).trim()
            blocks.add(MarkdownBlock.Heading(level, text))
            i++
            continue
        }

        // 4. Blockquote
        if (trimmed.startsWith(">")) {
            val quoteLines = mutableListOf<String>()
            while (i < lines.size && lines[i].trim().startsWith(">")) {
                quoteLines.add(lines[i].trim().removePrefix(">").trim())
                i++
            }
            blocks.add(MarkdownBlock.QuoteBlock(quoteLines.joinToString("\n")))
            continue
        }

        // 5. Table
        if (trimmed.startsWith("|") && trimmed.endsWith("|") && i + 1 < lines.size && lines[i + 1].trim().contains("---")) {
            val headers = trimmed.trim('|').split("|").map { it.trim() }
            i += 2 // skip header and separator
            val rows = mutableListOf<List<String>>()
            while (i < lines.size && lines[i].trim().startsWith("|")) {
                val cells = lines[i].trim().trim('|').split("|").map { it.trim() }
                rows.add(cells)
                i++
            }
            blocks.add(MarkdownBlock.TableBlock(headers, rows))
            continue
        }

        // 6. Bullet list
        if (trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ")) {
            val items = mutableListOf<String>()
            while (i < lines.size) {
                val t = lines[i].trim()
                if (t.startsWith("- ") || t.startsWith("* ") || t.startsWith("• ")) {
                    items.add(t.drop(2).trim())
                    i++
                } else break
            }
            blocks.add(MarkdownBlock.BulletList(items))
            continue
        }

        // 7. Numbered list
        val numRegex = Regex("^(\\d+)\\.\\s+(.*)")
        val numMatch = numRegex.find(trimmed)
        if (numMatch != null) {
            val items = mutableListOf<Pair<String, String>>()
            while (i < lines.size) {
                val m = numRegex.find(lines[i].trim())
                if (m != null) {
                    items.add(m.groupValues[1] to m.groupValues[2])
                    i++
                } else break
            }
            blocks.add(MarkdownBlock.NumberedList(items))
            continue
        }

        // 8. Paragraph
        val paraLines = mutableListOf<String>()
        while (i < lines.size) {
            val t = lines[i].trim()
            if (t.isEmpty() || t.startsWith("```") || t.startsWith("#") ||
                t.startsWith(">") || t.startsWith("- ") || t.startsWith("* ") ||
                (t.startsWith("|") && t.endsWith("|")) || numRegex.matches(t)
            ) {
                break
            }
            paraLines.add(lines[i])
            i++
        }
        if (paraLines.isNotEmpty()) {
            blocks.add(MarkdownBlock.Paragraph(paraLines.joinToString("\n")))
        } else {
            i++
        }
    }

    return blocks
}

private fun parseInlineMarkdown(text: String, codeColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var idx = 0
        while (idx < text.length) {
            when {
                text.startsWith("**", idx) -> {
                    val end = text.indexOf("**", idx + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = Color.White)) {
                            append(text.substring(idx + 2, end))
                        }
                        idx = end + 2
                    } else {
                        append(text[idx])
                        idx++
                    }
                }
                text.startsWith("`", idx) -> {
                    val end = text.indexOf("`", idx + 1)
                    if (end != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                color = codeColor,
                                background = codeColor.copy(alpha = 0.12f)
                            )
                        ) {
                            append(" " + text.substring(idx + 1, end) + " ")
                        }
                        idx = end + 1
                    } else {
                        append(text[idx])
                        idx++
                    }
                }
                text.startsWith("*", idx) && !text.startsWith("**", idx) -> {
                    val end = text.indexOf("*", idx + 1)
                    if (end != -1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(idx + 1, end))
                        }
                        idx = end + 1
                    } else {
                        append(text[idx])
                        idx++
                    }
                }
                else -> {
                    append(text[idx])
                    idx++
                }
            }
        }
    }
}
