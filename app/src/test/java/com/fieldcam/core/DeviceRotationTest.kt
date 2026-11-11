package com.fieldcam.core

import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceRotationTest {
    private val r0 = 0
    private val r90 = 1
    private val r180 = 2
    private val r270 = 3

    @Test
    fun snapMatchesCameraXBoundaries() {
        assertEquals(r0, DeviceRotation.snap(0))
        assertEquals(r0, DeviceRotation.snap(44))
        assertEquals(r270, DeviceRotation.snap(45))   // 왼쪽 옆면이 위
        assertEquals(r270, DeviceRotation.snap(90))
        assertEquals(r270, DeviceRotation.snap(134))
        assertEquals(r180, DeviceRotation.snap(135))  // 거꾸로
        assertEquals(r180, DeviceRotation.snap(224))
        assertEquals(r90, DeviceRotation.snap(225))   // 오른쪽 옆면이 위
        assertEquals(r90, DeviceRotation.snap(270))
        assertEquals(r90, DeviceRotation.snap(314))
        assertEquals(r0, DeviceRotation.snap(315))
        assertEquals(r0, DeviceRotation.snap(359))
    }

    @Test
    fun turningClearlySidewaysSwitches() {
        assertEquals(r90, DeviceRotation.next(270, r0))
        assertEquals(r270, DeviceRotation.next(90, r0))
        assertEquals(r0, DeviceRotation.next(0, r90))
        assertEquals(r0, DeviceRotation.next(0, r270))
    }

    @Test
    fun diagonalHoldKeepsPreviousDirection() {
        // 세로에서 출발: 65° 까지는 세로 유지, 66° 부터 가로
        assertEquals(r0, DeviceRotation.next(45, r0))
        assertEquals(r0, DeviceRotation.next(65, r0))
        assertEquals(r270, DeviceRotation.next(66, r0))
        assertEquals(r0, DeviceRotation.next(295, r0))
        assertEquals(r90, DeviceRotation.next(294, r0))
        // 가로에서 출발: 같은 45° 라도 직전이 가로면 가로 유지
        assertEquals(r270, DeviceRotation.next(45, r270))
        assertEquals(r270, DeviceRotation.next(25, r270))
        assertEquals(r0, DeviceRotation.next(24, r270))
    }

    @Test
    fun wrapsAroundZero() {
        assertEquals(r0, DeviceRotation.next(359, r0))
        assertEquals(r90, DeviceRotation.next(330, r90))
        assertEquals(r0, DeviceRotation.next(336, r90))
    }

    @Test
    fun jumpsAcrossHalfTurn() {
        assertEquals(r180, DeviceRotation.next(180, r0))
        assertEquals(r90, DeviceRotation.next(270, r270))
        assertEquals(r270, DeviceRotation.next(90, r90))
    }

    @Test
    fun jitterAroundFortyFiveDegreesNeverFlips() {
        var current = r0
        var flips = 0
        for (i in 0 until 2000) {
            val degrees = 45 + ((i * 37) % 31) - 15 // 30° ~ 60° 사이를 오가는 떨림
            val next = DeviceRotation.next(degrees, current)
            if (next != current) flips++
            current = next
        }
        assertEquals(0, flips)
    }
}
