package com.fieldcam.ui

import android.hardware.SensorManager
import android.view.OrientationEventListener
import android.view.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.IntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleStartEffect
import com.fieldcam.core.DeviceRotation

/**
 * 폰을 실제로 든 방향 (Surface.ROTATION_*). 앱이 화면에 떠 있는 동안 가속도 센서로 잰다.
 * 화면 맨 위에서 한 번만 만들어서, 어느 화면에 있든 마지막으로 든 방향을 기억한다.
 * 거의 수평(바닥 장비를 내려다보거나 천장을 올려다볼 때)이면 센서가 방향을 몰라 ORIENTATION_UNKNOWN 을 주므로
 * 직전 방향을 그대로 둔다. 가속도 센서가 없는 기기는 enable() 이 아무것도 하지 않아 늘 세로로 저장된다.
 */
@Composable
fun rememberDeviceRotation(): IntState {
    val context = LocalContext.current
    val rotation = remember { mutableIntStateOf(Surface.ROTATION_0) }
    val listener = remember(context) {
        object : OrientationEventListener(context, SensorManager.SENSOR_DELAY_UI) {
            override fun onOrientationChanged(degrees: Int) {
                if (degrees == OrientationEventListener.ORIENTATION_UNKNOWN) return
                val next = DeviceRotation.next(degrees, rotation.intValue)
                if (next != rotation.intValue) rotation.intValue = next
            }
        }
    }
    LifecycleStartEffect(listener) {
        listener.enable()
        onStopOrDispose { listener.disable() }
    }
    return rotation
}
