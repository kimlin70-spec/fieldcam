package com.fieldcam.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 장갑용 큰 타일 격자. 스크롤 없이 화면을 꽉 채운다.
 * 6개 이하면 2열, 그 이상은 3열. 최대 12개면 3열 4행.
 */
@Composable
fun TileGrid(
    names: List<String>,
    modifier: Modifier = Modifier,
    onTap: (String) -> Unit,
) {
    if (names.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("설정에서 이름을 추가하세요", fontSize = 22.sp, color = MaterialTheme.colorScheme.onBackground)
        }
        return
    }
    val columns = if (names.size <= 6) 2 else 3
    val rows = (names.size + columns - 1) / columns
    val fontSize = if (columns == 2) 32.sp else 24.sp
    Column(modifier.fillMaxSize().padding(4.dp)) {
        repeat(rows) { r ->
            Row(Modifier.fillMaxWidth().weight(1f)) {
                repeat(columns) { c ->
                    val i = r * columns + c
                    if (i < names.size) {
                        Tile(names[i], fontSize, Modifier.weight(1f).fillMaxHeight()) { onTap(names[i]) }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun Tile(
    name: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier,
    onTap: () -> Unit,
) {
    Box(
        modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onTap)
            .padding(6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
