// build.gradle.kts 안에서 `java` 는 자바 플러그인 설정을 가리키므로 java.util.* 를 풀어 쓰지 말고 import 한다.
import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// 버전: GitHub 자동 빌드가 -PversionCode, -PversionName 을 넘긴다. PC 에서 빌드하면 기본값.
//   ./gradlew :app:assembleRelease -PversionCode=1012 -PversionName=1.0.12
val appVersionCode = (findProperty("versionCode") as String?)?.toIntOrNull() ?: 1
val appVersionName = (findProperty("versionName") as String?) ?: "1.0-local"

// 서명: 프로젝트 루트의 keystore.properties (git 에서 제외됨) 가 가리키는 키로 서명한다.
//   키 이름: storeFile, storePassword, keyAlias(기본 fieldcam), keyPassword(기본 storePassword 와 같음)
//   storeFile 은 절대 경로 또는 프로젝트 루트 기준 상대 경로.
// GitHub 자동 빌드는 저장소 Secrets 로 이 파일을 만든다. 파일이 없으면 debug 키로 서명한다.
// 모든 빌드가 같은 키로 서명돼야 새 APK 가 이미 깔린 앱 위에 덮어쓰기 설치되고 설정이 유지된다.
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val releaseKeystore: File? = keystoreProps.getProperty("storeFile")
    ?.trim()
    ?.takeIf { it.isNotEmpty() }
    ?.let { rootProject.file(it) }
    ?.takeIf { it.exists() }
if (releaseKeystore == null) {
    logger.warn("FieldCam: keystore.properties 가 없거나 storeFile 을 찾지 못해 debug 키로 서명합니다.")
}

android {
    namespace = "com.fieldcam"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.fieldcam"
        minSdk = 29
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName
    }

    signingConfigs {
        if (releaseKeystore != null) {
            create("fieldcam") {
                storeFile = releaseKeystore
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias", "fieldcam")
                keyPassword = keystoreProps.getProperty("keyPassword") ?: keystoreProps.getProperty("storePassword")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            // 키가 있으면 PC 의 debug 빌드도 같은 키로 서명해서 GitHub 에서 받은 앱 위에 덮어 깔 수 있게 한다.
            if (releaseKeystore != null) signingConfig = signingConfigs.getByName("fieldcam")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName(if (releaseKeystore != null) "fieldcam" else "debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    lint {
        // Play 스토어에 올리지 않고 APK 로 직접 설치하는 앱이다.
        // Play 의 targetSdk 기한 검사는 날짜가 지나면 release 빌드를 막으므로 끈다.
        disable += "ExpiredTargetSdkVersion"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
