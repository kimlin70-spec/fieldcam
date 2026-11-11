package com.fieldcam.core

/**
 * 폰을 실제로 든 방향을 사진 방향 값으로 바꾼다. 안드로이드 의존성이 없어 단위 테스트가 된다.
 *
 * 입력 degrees 는 OrientationEventListener 값이다.
 *   0 = 똑바로 선 세로, 90 = 왼쪽 옆면이 위, 180 = 거꾸로 선 세로, 270 = 오른쪽 옆면이 위.
 * 출력은 CameraX ImageCapture.targetRotation 에 넣는 Surface.ROTATION_* 값이다 (ROTATION_n == n).
 *   0 = ROTATION_0, 1 = ROTATION_90, 2 = ROTATION_180, 3 = ROTATION_270.
 */
object DeviceRotation {
    /** 지금 방향의 중심에서 45° 에 이만큼을 더 벗어나야 방향을 바꾼다. 비스듬히 들 때 손떨림으로 방향이 오가지 않게 한다. */
    const val HYSTERESIS = 20

    /** CameraX UseCase.snapToSurfaceRotation 과 같은 구간 나눔. */
    fun snap(degrees: Int): Int = when (degrees.mod(360)) {
        in 45 until 135 -> 3
        in 135 until 225 -> 2
        in 225 until 315 -> 1
        else -> 0
    }

    /** 직전 방향 [current] 를 기준으로, 충분히 기울었을 때만 새 방향을 낸다. */
    fun next(degrees: Int, current: Int): Int {
        val center = (-current * 90).mod(360) // ROTATION_90 → 270°, ROTATION_270 → 90°
        val diff = (degrees - center).mod(360)
        val off = minOf(diff, 360 - diff)
        return if (off <= 45 + HYSTERESIS) current else snap(degrees)
    }
}
