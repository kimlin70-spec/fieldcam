package com.fieldcam.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fieldcam.core.Config
import com.fieldcam.core.Mode
import com.fieldcam.core.Target

/**
 * 앱 첫 화면. [경상] [오버홀] 탭과 큰 타일.
 * 경상은 한 번, 오버홀은 두 번 눌러서 촬영 화면으로 간다.
 */
@Composable
fun SelectScreen(
    config: Config,
    mode: Mode,
    onMode: (Mode) -> Unit,
    onTarget: (Target) -> Unit,
    onSettings: () -> Unit,
) {
    var groupName by remember { mutableStateOf<String?>(null) }
    val project = config.project
    val group = project?.groups?.firstOrNull { it.name == groupName }

    BackHandler(enabled = mode == Mode.OVERHAUL && group != null) { groupName = null }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding()) {
        Row(Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            ModeTab("경상", mode == Mode.ROUTINE, Modifier.weight(1f)) { groupName = null; onMode(Mode.ROUTINE) }
            ModeTab("오버홀", mode == Mode.OVERHAUL, Modifier.weight(1f)) { onMode(Mode.OVERHAUL) }
            IconButton(onClick = onSettings, Modifier.size(56.dp)) {
                Icon(Icons.Default.Settings, contentDescription = "설정", Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onBackground)
            }
        }

        when (mode) {
            Mode.ROUTINE -> TileGrid(config.routine.filter { it.isNotBlank() }) { onTarget(Target.Routine(it)) }

            Mode.OVERHAUL -> {
                if (project == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("설정에서 오버홀 공사를 추가하세요", fontSize = 22.sp)
                    }
                } else if (group == null) {
                    Header(project.name, null)
                    TileGrid(project.groups.map { it.name }.filter { it.isNotBlank() }) { groupName = it }
                } else {
                    Header("${project.name} · ${group.name}") { groupName = null }
                    TileGrid(group.items.filter { it.isNotBlank() }) { onTarget(Target.Overhaul(project.name, group.name, it)) }
                }
            }
        }
    }
}

@Composable
private fun ModeTab(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier
            .padding(4.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}

/** 오버홀 탭 머리글. onBack 이 있으면 누르면 1차 분류로 돌아간다. */
@Composable
private fun Header(text: String, onBack: (() -> Unit)?) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .then(if (onBack != null) Modifier.clickable(onClick = onBack) else Modifier)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            Icon(Icons.Default.ArrowBack, contentDescription = "뒤로", Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(text, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(start = 8.dp))
    }
}
