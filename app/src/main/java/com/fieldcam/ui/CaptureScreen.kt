package com.fieldcam.ui

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.MediaStore
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.fieldcam.core.Target
import com.fieldcam.data.PhotoStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * 촬영 화면.
 * 상단: 현재 분류 이름(누르면 선택 화면), 오버홀이면 ◀ ▶ 로 같은 1차 안에서 이동.
 * 하단 1/3 전체가 셔터. 볼륨키·리모컨은 [shutter] 로 들어온다.
 * 후면 카메라 고정, 전환 없음. 화면 꺼짐 방지.
 */
@Composable
fun CaptureScreen(
    target: Target,
    shutter: Flow<Unit>,
    photoStore: PhotoStore,
    onPrev: (() -> Unit)?,
    onNext: (() -> Unit)?,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val imageCapture = remember { ImageCapture.Builder().build() }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var torch by remember { mutableStateOf(false) }
    var count by remember(target) { mutableIntStateOf(0) }
    var banner by remember { mutableStateOf<String?>(null) }
    var flash by remember { mutableStateOf(false) }
    val flashAlpha by animateFloatAsState(if (flash) 0.9f else 0f, tween(if (flash) 0 else 250), label = "flash")

    BackHandler(onBack = onBack)
    KeepScreenOn()

    LaunchedEffect(target) { count = photoStore.countToday(target) }

    fun shoot() {
        scope.launch {
            val slot = photoStore.reserve(target)
            flash = true
            vibrate(context)
            count = slot.seq
            banner = "${target.title}  ${slot.seq}장"
            val options = ImageCapture.OutputFileOptions
                .Builder(context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, slot.values)
                .build()
            imageCapture.takePicture(options, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) = Unit
                override fun onError(e: ImageCaptureException) {
                    banner = "저장 실패: ${e.message}"
                }
            })
        }
    }

    LaunchedEffect(shutter) { shutter.collect { shoot() } }
    LaunchedEffect(flash) { if (flash) { delay(80); flash = false } }
    LaunchedEffect(banner) { if (banner != null) { delay(1200); banner = null } }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FIT_CENTER
                    val future = ProcessCameraProvider.getInstance(ctx)
                    future.addListener({
                        val provider = future.get()
                        val preview = Preview.Builder().build().also { it.setSurfaceProvider(surfaceProvider) }
                        provider.unbindAll()
                        camera = provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
        )

        // 촬영 순간 흰색 깜빡임. 장갑과 소음 때문에 눈으로 확인하는 피드백.
        Box(Modifier.fillMaxSize().alpha(flashAlpha).background(Color.White))

        Column(Modifier.fillMaxSize()) {
            // 상단 밴드
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .statusBarsPadding()
                    .height(88.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NavButton(Icons.Default.KeyboardArrowLeft, onPrev)
                Box(
                    Modifier.weight(1f).fillMaxHeight().clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        target.title,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                    )
                }
                NavButton(Icons.Default.KeyboardArrowRight, onNext)
            }

            // 가운데: 프리뷰 위에 토치 버튼과 오늘 장수
            Box(Modifier.fillMaxWidth().weight(2f)) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(if (torch) Color(0xFFFFC107) else Color.Black.copy(alpha = 0.5f))
                        .clickable {
                            torch = !torch
                            camera?.cameraControl?.enableTorch(torch)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (torch) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                        contentDescription = "손전등",
                        Modifier.size(40.dp),
                        tint = if (torch) Color.Black else Color.White,
                    )
                }
                Text(
                    "오늘 ${count}장",
                    Modifier.align(Alignment.TopStart).padding(16.dp),
                    fontSize = 22.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
                banner?.let {
                    Box(
                        Modifier
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                    ) {
                        Text(it, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFC107), textAlign = TextAlign.Center)
                    }
                }
            }

            // 하단 1/3 전체 셔터
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable { shoot() }
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier.size(96.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(80.dp).clip(CircleShape).background(Color.White))
                }
            }
        }
    }
}

@Composable
private fun NavButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: (() -> Unit)?) {
    Box(
        Modifier
            .size(88.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        if (onClick != null) {
            Icon(icon, contentDescription = null, Modifier.size(56.dp), tint = Color.White)
        }
    }
}

@Composable
private fun KeepScreenOn() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
}

private fun vibrate(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    vibrator.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
}
