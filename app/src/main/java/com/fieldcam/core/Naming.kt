package com.fieldcam.core

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** 파일명·폴더명 규칙. 순수 Kotlin. */
object Naming {
    /** Pictures/ 아래 앱 루트 폴더. */
    const val ROOT = "Pictures/FieldCam"
    const val ROUTINE_DIR = "Routine"
    const val EXT = ".jpg"

    private val dateFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("MMddyy")
    private val allowed = Regex("[^A-Za-z0-9-]")

    /** 날짜 표기 mmddyy. 예: 2026-11-04 → 110426 */
    fun dateStamp(date: LocalDate = LocalDate.now()): String = date.format(dateFmt)

    /**
     * 타일 이름 정리. 영문·숫자·하이픈만 남긴다.
     * 공백은 하이픈으로, 밑줄은 파일명 구분자라서 하이픈으로 바꾼다.
     */
    fun sanitize(raw: String): String =
        raw.trim()
            .replace(Regex("[\\s_]+"), "-")
            .replace(allowed, "")
            .trim('-')

    fun isValid(name: String): Boolean = name.isNotEmpty() && sanitize(name) == name

    /** 접두어 + 3자리 순번 + 확장자. 999 를 넘으면 자릿수가 늘어난다. */
    fun fileName(prefix: String, seq: Int): String =
        prefix + seq.toString().padStart(3, '0') + EXT

    /**
     * 기존 파일명에서 순번을 꺼낸다. 접두어가 다르거나 형식이 안 맞으면 null.
     * "110426_Boiler2_007.jpg" + "110426_Boiler2_" → 7
     */
    fun seqOf(fileName: String, prefix: String): Int? {
        if (!fileName.startsWith(prefix)) return null
        val rest = fileName.removePrefix(prefix)
        val digits = rest.substringBefore('.')
        if (digits.isEmpty() || !digits.all { it.isDigit() }) return null
        return digits.toIntOrNull()
    }

    /** 폴더 안에 이미 있는 파일명들로부터 다음 순번을 정한다. */
    fun nextSeq(existing: Iterable<String>, prefix: String, floor: Int = 0): Int {
        val max = existing.mapNotNull { seqOf(it, prefix) }.maxOrNull() ?: 0
        return maxOf(max, floor) + 1
    }
}
