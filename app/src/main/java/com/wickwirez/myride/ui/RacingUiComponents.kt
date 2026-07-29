
package com.wickwirez.myride.ui
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wickwirez.myride.R
// Small icon badge + bold red all-caps label, used to head each section
// on The Parts Store and similar screens. Optionally shows a dimmed photo
// backdrop behind the whole section (e.g. oil pouring behind the Oil section).
// Full section panel: background photo spans the WHOLE card (header + the
// fields inside it), with a glowing red border around the entire panel —
// not just a small strip behind the header row.
@Composable
fun SectionCard(
    icon: ImageVector,
    title: String,
    backgroundImageRes: Int? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
    ) {
        Box(modifier = Modifier.matchParentSize().background(Color(0xFF120E0E)))
        if (backgroundImageRes != null) {
            Image(
                painter = painterResource(id = backgroundImageRes),
                contentDescription = null,
                modifier = Modifier.matchParentSize().alpha(0.28f),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0xB3000000))
        }
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1A1414))
                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    title.uppercase(),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
    }
}
// Field colors with a vivid, persistent red glow border (not just the
// default faint Material3 outline) — use on every OutlinedTextField.
fun glowFieldColors() = TextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
    focusedContainerColor = Color(0x30000000),
    unfocusedContainerColor = Color(0x30000000),
    focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
    unfocusedLeadingIconColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
)
// Thin glowing red vertical accent strip down the screen edge, matching
// the reference art's persistent side-glow motif.
fun EdgeGlow(modifier: Modifier = Modifier) {
        modifier = modifier
            .width(4.dp)
            .fillMaxHeight()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        Color.Transparent
    )
// Field with a red caps caption ABOVE it and an icon badge OUTSIDE to the
// left — matches the reference layout exactly, rather than Material3's
// default floating inline label.
fun LabeledIconField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconRes: Int? = null,
    placeholder: String? = null,
    trailingText: String? = null,
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
    readOnly: Boolean = false,
    trailingAction: (@Composable () -> Unit)? = null,
    minLines: Int = 1
    Column(modifier = modifier) {
        Text(
            label.uppercase(),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null || iconRes != null) {
                        .size(42.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), RoundedCornerShape(8.dp)),
                    if (icon != null) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    } else if (iconRes != null) {
                        Image(painter = painterResource(id = iconRes), contentDescription = null, modifier = Modifier.size(26.dp))
                    }
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = placeholder?.let { { Text(it, color = Color.White.copy(alpha = 0.35f)) } },
                readOnly = readOnly,
                trailingIcon = when {
                    trailingText != null -> {
                        { Text(trailingText, color = Color.White.copy(alpha = 0.6f)) }
                    trailingAction != null -> trailingAction
                    else -> null
                },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
                minLines = minLines,
                colors = glowFieldColors(),
                modifier = Modifier.weight(1f)
fun SectionHeader(iconRes: Int, title: String, backgroundImageRes: Int? = null) {
            .clip(RoundedCornerShape(10.dp))
                    .alpha(0.35f),
                    .background(Color(0xCC000000))
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
                painter = painterResource(id = iconRes),
                modifier = Modifier.size(32.dp)
            Spacer(Modifier.width(10.dp))
            Text(
                title.uppercase(),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
fun SectionHeader(icon: ImageVector, title: String, backgroundImageRes: Int? = null) {
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A1414))
                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
// Red-glow textured button (using the real asset), for primary save/confirm actions.
fun CheckeredFlagButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
            .height(56.dp)
            .let { if (enabled) it.clickable(onClick = onClick) else it },
        contentAlignment = Alignment.Center
        Image(
            painter = painterResource(id = R.drawable.red_glow_button),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
            text.uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
// Dark card with a carbon-fiber texture and glowing red border, used for
// vehicle cards and similar highlighted panels.
fun GlowCard(
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
            .background(Color(0xFF161414))
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            painter = painterResource(id = R.drawable.carbon_fiber_panel),
            modifier = Modifier.matchParentSize().alpha(0.16f),
            contentScale = ContentScale.Crop
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
        content()
