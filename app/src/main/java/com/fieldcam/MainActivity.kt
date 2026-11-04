package com.fieldcam

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fieldcam.core.Config
import com.fieldcam.core.Mode
import com.fieldcam.core.Target
import com.fieldcam.data.ConfigStore
import com.fieldcam.data.PhotoStore
import com.fieldcam.ui.CaptureScreen
import com.fieldcam.ui.FieldCamTheme
import com.fieldcam.ui.SelectScreen
import com.fieldcam.ui.SettingsScreen
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    /** 볼륨키·블루투스 리모컨이 보내는 셔터 신호. 촬영 화면이 받는다. */
    private val shutter = MutableSharedFlow<Unit>(extraBufferCapacity = 8)

    /** 촬영 화면이 떠 있을 때만 하드웨어 키를 가로챈다. */
    private var capturing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val configStore = ConfigStore(applicationContext)
        val photoStore = PhotoStore(contentResolver)
        setContent {
            FieldCamTheme {
                App(configStore, photoStore, shutter) { capturing = it }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (!capturing) return super.onKeyDown(keyCode, event)
        return when (keyCode) {
            in SHUTTER_KEYS -> {
                if (event == null || event.repeatCount == 0) shutter.tryEmit(Unit)
                true
            }
            KeyEvent.KEYCODE_FOCUS -> true
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (capturing && (keyCode in SHUTTER_KEYS || keyCode == KeyEvent.KEYCODE_FOCUS)) return true
        return super.onKeyUp(keyCode, event)
    }

    companion object {
        /** 볼륨 상·하, 카메라, 엔터, 헤드셋 버튼. 리모컨 종류와 무관하게 전부 셔터. */
        val SHUTTER_KEYS = setOf(
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_CAMERA,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_HEADSETHOOK,
        )
    }
}

private sealed class Screen {
    data object Select : Screen()
    data class Capture(val target: Target) : Screen()
    data object Settings : Screen()
}

@Composable
private fun App(
    configStore: ConfigStore,
    photoStore: PhotoStore,
    shutter: MutableSharedFlow<Unit>,
    onCapturing: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val config by configStore.config.collectAsStateWithLifecycle<Config?>(initialValue = null)
    var screen by remember { mutableStateOf<Screen>(Screen.Select) }
    var granted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }

    SideEffect { onCapturing(screen is Screen.Capture) }

    if (!granted) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            Button(onClick = { permission.launch(Manifest.permission.CAMERA) }, Modifier.padding(24.dp).height(80.dp)) {
                Text("카메라 권한 허용", fontSize = 24.sp)
            }
        }
        return
    }

    val cfg = config ?: return
    fun update(transform: (Config) -> Config) { scope.launch { configStore.update(transform) } }

    when (val s = screen) {
        Screen.Select -> SelectScreen(
            config = cfg,
            mode = cfg.lastMode,
            onMode = { m -> update { it.copy(lastMode = m) } },
            onTarget = { screen = Screen.Capture(it) },
            onSettings = { screen = Screen.Settings },
        )

        is Screen.Capture -> {
            val t = s.target
            // 오버홀이면 같은 1차 분류 안에서 이전·다음 2차 분류로 이동
            val siblings = (t as? Target.Overhaul)?.let { o ->
                cfg.projects.firstOrNull { it.name == o.project }?.groups?.firstOrNull { it.name == o.group }?.items?.filter { it.isNotBlank() }
            }
            val index = siblings?.indexOf((t as Target.Overhaul).item) ?: -1
            fun go(i: Int) { screen = Screen.Capture((t as Target.Overhaul).copy(item = siblings!![i])) }
            CaptureScreen(
                target = t,
                shutter = shutter,
                photoStore = photoStore,
                onPrev = if (siblings != null && index > 0) ({ go(index - 1) }) else null,
                onNext = if (siblings != null && index in 0 until siblings.size - 1) ({ go(index + 1) }) else null,
                onBack = { screen = Screen.Select },
            )
        }

        Screen.Settings -> SettingsScreen(
            config = cfg,
            onSave = { saved -> update { saved } },
            onBack = { screen = Screen.Select },
        )
    }
}
