package com.fieldcam.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fieldcam.core.Config
import com.fieldcam.core.Defaults
import com.fieldcam.core.Group
import com.fieldcam.core.Naming
import com.fieldcam.core.Project

/**
 * 설정. 사무실에서 장갑 벗고 쓰는 화면이라 일반 리스트 UI.
 * 경상 이름 12개, 오버홀 공사 목록과 각 공사의 1차·2차 분류 12개씩.
 * 이름은 영문·숫자·하이픈만 허용되고 그대로 파일명이 된다.
 */
@Composable
fun SettingsScreen(
    config: Config,
    onUpdate: ((Config) -> Config) -> Unit,
    onBack: () -> Unit,
) {
    var tab by remember { mutableIntStateOf(0) }
    var groupIndex by remember { mutableStateOf<Int?>(null) }
    val project = config.project

    BackHandler { if (groupIndex != null) groupIndex = null else onBack() }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding()) {
        Row(Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (groupIndex != null) groupIndex = null else onBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
            }
            Text("설정", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0; groupIndex = null }, text = { Text("경상", fontSize = 18.sp) })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("오버홀", fontSize = 18.sp) })
        }

        val g = groupIndex
        when {
            tab == 0 -> NameListEditor(
                title = "경상 위치 (최대 ${Defaults.MAX_TILES}개)",
                items = config.routine,
                name = { it },
                rename = { _, n -> n },
                create = { "" },
                onChange = { list -> onUpdate { it.copy(routine = list) } },
            )

            project != null && g != null && g < project.groups.size -> {
                val group = project.groups[g]
                NameListEditor(
                    title = "${project.name} · ${group.name} · 2차 분류 (최대 ${Defaults.MAX_TILES}개)",
                    items = group.items,
                    name = { it },
                    rename = { _, n -> n },
                    create = { "" },
                    onChange = { list ->
                        onUpdate { c -> c.updateProject { p -> p.copy(groups = p.groups.replaceAt(g) { it.copy(items = list) }) } }
                    },
                )
            }

            else -> OverhaulSettings(config, onUpdate) { groupIndex = it }
        }
    }
}

@Composable
private fun OverhaulSettings(config: Config, onUpdate: ((Config) -> Config) -> Unit, onOpenGroup: (Int) -> Unit) {
    val project = config.project
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        item {
            Text("오버홀 공사 (현재 공사가 갤러리 폴더명이 됩니다)", fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp))
        }
        itemsIndexed(config.projects) { i, p ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = i == config.currentProject, onClick = { onUpdate { it.copy(currentProject = i) } })
                OutlinedTextField(
                    value = p.name,
                    onValueChange = { n -> onUpdate { c -> c.copy(projects = c.projects.replaceAt(i) { it.copy(name = Naming.sanitize(n)) }) } },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = {
                    onUpdate { c ->
                        val rest = c.projects.filterIndexed { j, _ -> j != i }
                        val cur = when {
                            rest.isEmpty() -> 0
                            i < c.currentProject -> c.currentProject - 1
                            i == c.currentProject -> c.currentProject.coerceAtMost(rest.size - 1)
                            else -> c.currentProject
                        }
                        c.copy(projects = rest, currentProject = cur)
                    }
                }) { Icon(Icons.Default.Delete, contentDescription = "삭제") }
            }
        }
        item {
            Button(
                onClick = {
                    onUpdate { c ->
                        // 새 공사는 현재 공사의 분류 구성을 복사해서 시작한다.
                        val template = c.project?.groups ?: Defaults.project.groups
                        c.copy(projects = c.projects + Project("NewOH", template), currentProject = c.projects.size)
                    }
                },
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("새 공사 추가", fontSize = 18.sp)
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
        }
        if (project != null) {
            item {
                Text("${project.name} · 1차 분류 (최대 ${Defaults.MAX_TILES}개, ▶ 로 2차 분류 편집)", fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp))
            }
            itemsIndexed(project.groups) { i, group ->
                NameRow(
                    name = group.name,
                    canUp = i > 0,
                    canDown = i < project.groups.size - 1,
                    onRename = { n -> onUpdate { c -> c.updateProject { p -> p.copy(groups = p.groups.replaceAt(i) { it.copy(name = n) }) } } },
                    onUp = { onUpdate { c -> c.updateProject { p -> p.copy(groups = p.groups.swap(i, i - 1)) } } },
                    onDown = { onUpdate { c -> c.updateProject { p -> p.copy(groups = p.groups.swap(i, i + 1)) } } },
                    onDelete = { onUpdate { c -> c.updateProject { p -> p.copy(groups = p.groups.filterIndexed { j, _ -> j != i }) } } },
                    onOpen = { onOpenGroup(i) },
                )
            }
            if (project.groups.size < Defaults.MAX_TILES) {
                item {
                    Button(
                        onClick = { onUpdate { c -> c.updateProject { p -> p.copy(groups = p.groups + Group("")) } } },
                        modifier = Modifier.padding(vertical = 8.dp),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("1차 분류 추가", fontSize = 18.sp)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(48.dp)) }
    }
}

/** 이름 목록 편집기. 경상 위치와 2차 분류가 같이 쓴다. */
@Composable
private fun <T> NameListEditor(
    title: String,
    items: List<T>,
    name: (T) -> String,
    rename: (T, String) -> T,
    create: () -> T,
    onChange: (List<T>) -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        item { Text(title, fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp)) }
        itemsIndexed(items) { i, item ->
            NameRow(
                name = name(item),
                canUp = i > 0,
                canDown = i < items.size - 1,
                onRename = { n -> onChange(items.replaceAt(i) { rename(it, n) }) },
                onUp = { onChange(items.swap(i, i - 1)) },
                onDown = { onChange(items.swap(i, i + 1)) },
                onDelete = { onChange(items.filterIndexed { j, _ -> j != i }) },
                onOpen = null,
            )
        }
        if (items.size < Defaults.MAX_TILES) {
            item {
                Button(onClick = { onChange(items + create()) }, modifier = Modifier.padding(vertical = 8.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("추가", fontSize = 18.sp)
                }
            }
        }
        item { Spacer(Modifier.height(48.dp)) }
    }
}

@Composable
private fun NameRow(
    name: String,
    canUp: Boolean,
    canDown: Boolean,
    onRename: (String) -> Unit,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onDelete: () -> Unit,
    onOpen: (() -> Unit)?,
) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = name,
            onValueChange = { onRename(Naming.sanitize(it)) },
            singleLine = true,
            placeholder = { Text("영문/숫자/하이픈") },
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onUp, enabled = canUp) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = "위로") }
        IconButton(onClick = onDown, enabled = canDown) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = "아래로") }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "삭제") }
        if (onOpen != null) {
            IconButton(onClick = onOpen) { Icon(Icons.Default.ChevronRight, contentDescription = "2차 분류") }
        }
    }
}

private fun Config.updateProject(transform: (Project) -> Project): Config =
    if (project == null) this else copy(projects = projects.replaceAt(currentProject, transform))

private fun <T> List<T>.replaceAt(index: Int, transform: (T) -> T): List<T> =
    mapIndexed { i, v -> if (i == index) transform(v) else v }

private fun <T> List<T>.swap(a: Int, b: Int): List<T> {
    if (a !in indices || b !in indices) return this
    val m = toMutableList()
    val t = m[a]; m[a] = m[b]; m[b] = t
    return m
}
