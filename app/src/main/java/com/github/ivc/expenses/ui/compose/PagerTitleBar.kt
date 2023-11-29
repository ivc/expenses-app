package com.github.ivc.expenses.ui.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PagerTitleBar(
    title: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineLarge,
    onLeft: (() -> Unit)? = null,
    onRight: (() -> Unit)? = null,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onLeft?.invoke() }, enabled = onLeft != null) {
            Icon(Icons.Default.KeyboardArrowLeft, "Previous")
        }

        Text(
            text = title,
            style = style,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )

        IconButton(onClick = { onRight?.invoke() }, enabled = onRight != null) {
            Icon(Icons.Default.KeyboardArrowRight, "Next")
        }
    }
}

@Preview
@Composable
fun PreviewPagerTitleBar() {
    val title = buildAnnotatedString {
        append("line1")
        appendLine()
        append("line2")
    }

    Surface(Modifier.width(412.dp)) {
        PagerTitleBar(title)
    }
}
