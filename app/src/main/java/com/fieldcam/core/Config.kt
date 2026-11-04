package com.fieldcam.core

import kotlinx.serialization.Serializable

/** 촬영 모드. 경상(1단계 분류) / 오버홀(2단계 분류). */
@Serializable
enum class Mode { ROUTINE, OVERHAUL }

/** 오버홀 1차 분류 하나와 그 아래 2차 분류 목록. */
@Serializable
data class Group(
    val name: String,
    val items: List<String> = emptyList(),
)

/** 오버홀 공사 하나. 이름이 갤러리 폴더명과 파일명 접두어가 된다. */
@Serializable
data class Project(
    val name: String,
    val groups: List<Group> = emptyList(),
)

/** 앱 설정 전체. DataStore 에 JSON 한 덩어리로 저장된다. */
@Serializable
data class Config(
    val routine: List<String> = Defaults.routine,
    val projects: List<Project> = listOf(Defaults.project),
    val currentProject: Int = 0,
    val lastMode: Mode = Mode.ROUTINE,
) {
    val project: Project? get() = projects.getOrNull(currentProject)
}

object Defaults {
    const val MAX_TILES = 12

    val routine = listOf(
        "Boiler1", "Boiler2", "Boiler3",
        "STG", "WTR", "Sewage",
        "Workshop", "RWshop", "Office",
        "Store", "HS", "Etc",
    )

    val project = Project(
        name = "B2OH26",
        groups = listOf(
            Group("ESP", listOf("1A1", "1A2", "1A3", "1A4", "1A5", "2A1", "2A2", "2A3", "2A4", "2A5")),
            Group("BackPass", (1..9).map { it.toString() }),
            Group("Cyclone", listOf("Inlet-S", "Inlet-N", "Outlet-S", "Outlet-N", "LowerCone-S", "LowerCone-N")),
        ),
    )
}
