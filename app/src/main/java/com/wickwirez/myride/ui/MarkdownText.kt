package com.wickwirez.myride.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier
) {
    val annotated = remember(text) { parseMarkdown(text) }
    Text(text = annotated, modifier = modifier, style = MaterialTheme.typography.bodyMedium)
}

private fun parseMarkdown(raw: String): AnnotatedString = buildAnnotatedString {
    raw.lines().forEachIndexed { lineIndex, rawLine ->
        if (lineIndex > 0) append("\n")

        val headingMatch = Regex("^(#{1,6})\\s+(.*)").find(rawLine)
        if (headingMatch != null) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                appendInline(headingMatch.groupValues[2])
            }
            return@forEachIndexed
        }

        val bulletMatch = Regex("^\\s*
cat >> ~/My-Ride/app/src/main/java/com/wickwirez/myride/ui/MarkdownText.kt << 'EOF'

private fun parseMarkdown(raw: String): AnnotatedString = buildAnnotatedString {
    raw.lines().forEachIndexed { lineIndex, rawLine ->
        if (lineIndex > 0) append("\n")

        val headingMatch = Regex("^(#{1,6})\\s+(.*)").find(rawLine)
        if (headingMatch != null) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                appendInline(headingMatch.groupValues[2])
            }
            return@forEachIndexed
        }

        val bulletMatch = Regex("^\\s*[-*\u2022]\\s+(.*)").find(rawLine)
        if (bulletMatch != null) {
            append("  \u2022  ")
            appendInline(bulletMatch.groupValues[1])
            return@forEachIndexed
        }

        appendInline(rawLine)
    }
}

private fun AnnotatedString.Builder.appendInline(line: String) {
    var index = 0
    val pattern = Regex("\\*\\*(.+?)\\*\\*|__(.+?)__|\\*(.+?)\\*|_(.+?)_")

    pattern.findAll(line).forEach { match ->
        if (match.range.first > index) {
            append(line.substring(index, match.range.first))
        }

        val boldText = match.groupValues[1].ifEmpty { match.groupValues[2] }
        val italicText = match.groupValues[3].ifEmpty { match.groupValues[4] }

        when {
            boldText.isNotEmpty() ->
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(boldText) }
            italicText.isNotEmpty() ->
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(italicText) }
        }

        index = match.range.last + 1
    }

    if (index < line.length) {
        append(line.substring(index))
    }
}
