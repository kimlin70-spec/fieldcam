package com.fieldcam.data

import android.content.ContentResolver
import android.content.ContentValues
import android.provider.MediaStore
import com.fieldcam.core.Naming
import com.fieldcam.core.Target
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 갤러리(MediaStore) 에 사진을 넣고 순번을 센다.
 * 순번은 폴더 안 실제 파일을 세서 정하고, 연사 중 아직 기록되지 않은 사진 때문에
 * 번호가 겹치지 않도록 메모리에 마지막 번호를 같이 기억한다.
 */
class PhotoStore(private val resolver: ContentResolver) {
    private val lastSeq = HashMap<String, Int>()

    /** 이번 촬영에 쓸 파일명과 순번. */
    data class Slot(val fileName: String, val seq: Int, val values: ContentValues)

    suspend fun reserve(target: Target): Slot = withContext(Dispatchers.IO) {
        val prefix = target.prefix(Naming.dateStamp())
        val key = target.relativePath + prefix
        val seq = Naming.nextSeq(listNames(target.relativePath, prefix), prefix, floor = lastSeq[key] ?: 0)
        lastSeq[key] = seq
        val fileName = Naming.fileName(prefix, seq)
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, target.relativePath)
        }
        Slot(fileName, seq, values)
    }

    /** 오늘 이 분류로 찍힌 장수. 촬영 화면 진입 시 표시용. */
    suspend fun countToday(target: Target): Int = withContext(Dispatchers.IO) {
        val prefix = target.prefix(Naming.dateStamp())
        listNames(target.relativePath, prefix).count { Naming.seqOf(it, prefix) != null }
    }

    private fun listNames(relativePath: String, prefix: String): List<String> {
        val names = ArrayList<String>()
        val projection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
        val selection = "${MediaStore.Images.Media.RELATIVE_PATH} = ? AND ${MediaStore.Images.Media.DISPLAY_NAME} LIKE ?"
        val args = arrayOf(relativePath, "$prefix%")
        resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, args, null)?.use { c ->
            val col = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            while (c.moveToNext()) names.add(c.getString(col))
        }
        return names
    }
}
