package com.fieldcam.core

/**
 * 지금 찍을 사진의 분류. 폴더와 파일명 접두어를 결정한다.
 * 안드로이드 의존성이 없어서 JVM 단위 테스트가 가능하다.
 */
sealed class Target {
    /** 갤러리 폴더. MediaStore RELATIVE_PATH 형식(끝에 슬래시). */
    abstract val relativePath: String

    /** 촬영 화면 상단에 크게 보일 이름. */
    abstract val title: String

    /** 날짜별 순번을 셀 파일명 접두어. 이 뒤에 NNN.jpg 가 붙는다. */
    abstract fun prefix(dateStamp: String): String

    data class Routine(val location: String) : Target() {
        override val relativePath = "${Naming.ROOT}/${Naming.ROUTINE_DIR}/"
        override val title = location
        override fun prefix(dateStamp: String) = "${dateStamp}_${location}_"
    }

    data class Overhaul(val project: String, val group: String, val item: String) : Target() {
        override val relativePath = "${Naming.ROOT}/${project}/"
        override val title = "$group · $item"
        override fun prefix(dateStamp: String) = "${project}_${group}_${item}_${dateStamp}_"
    }
}
