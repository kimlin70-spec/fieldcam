# FieldCam

발전소 현장 사진 전용 카메라. **찍기 전에 이름을 정하는 카메라**다.
장갑을 낀 채로 큰 타일을 한두 번 눌러 "여기가 어디인지"를 정하고, 그 다음은 셔터만 누른다.
사진은 폴더와 파일명에 분류가 박힌 채로 갤러리에 바로 저장된다. 나중에 정리할 일이 없다.

## 설치와 업데이트

`main`에 합칠 때마다 GitHub Actions가 APK를 만들어 아래 링크에 덮어쓴다. 처음 한 번 설정한 뒤에는 PC가 필요 없다.

- 다운로드: https://github.com/kimlin70-spec/fieldcam/releases/download/apk-latest/fieldcam.apk
- 릴리스 페이지: https://github.com/kimlin70-spec/fieldcam/releases/tag/apk-latest

### 처음 한 번

1. **서명 키를 GitHub에 넣는다.** PC 브라우저로 저장소 Settings → Secrets and variables → Actions → **New repository secret**에서 아래 두 개를 만든다. 값은 따로 받은 파일 내용을 그대로 붙여넣는다.

   | 이름 | 값 |
   |---|---|
   | `ANDROID_KEYSTORE_B64` | 키스토어 파일을 base64로 바꾼 한 줄 문자열 |
   | `ANDROID_KEYSTORE_PASSWORD` | 키스토어 비밀번호 |

2. **한 번 빌드한다.** Actions → **Build Android APK** → **Run workflow**에서 브랜치 `main`으로 실행하고, 초록색으로 끝날 때까지 5분쯤 기다린다. 릴리스 페이지의 "서명" 줄이 `고정 키`인지 본다.
3. **폰에 있던 FieldCam을 지운다.** PC의 Android Studio에서 깐 앱은 서명 키가 달라서 그 위에 덮어 깔 수 없다. 지우면 설정한 이름도 지워지니 필요하면 미리 적어 둔다. 찍은 사진은 갤러리에 그대로 남는다.
4. **설치한다.** 폰 브라우저에서 다운로드 링크를 연다. Chrome이 "해를 끼칠 수 있는 파일"이라고 물으면 다운로드를 누른다. 다운로드 완료 알림에서 받은 파일을 열면 "출처를 알 수 없는 앱" 설정으로 가는데, APK를 연 앱(Chrome이나 내 파일)의 **이 출처 허용**을 켜고 돌아온다. 삼성은 설정 → 보안 및 개인정보 보호 → 자동 차단이 켜져 있으면 먼저 끈다. Play 프로텍트가 막으면 **자세히 알아보기** 아래의 **무시하고 설치**를 고른다.

### 업데이트

`main`에 합쳐질 때마다 몇 분 뒤 같은 링크의 APK가 새것으로 바뀐다. 같은 링크에서 받아 설치하면 덮어쓰기 되고 설정한 이름도 남는다.
받을 때마다 다운로드 완료 알림에서 방금 받은 파일을 연다. 같은 이름 파일이 있으면 브라우저가 `fieldcam (1).apk`처럼 이름을 바꿔 저장해서, 내 파일에서 `fieldcam.apk`를 열면 예전 것이 깔린다.
설정 화면 오른쪽 위의 `v1.0.N`이 지금 깔린 빌드 번호다. 릴리스 페이지의 앱 버전과 같으면 최신이다.

- 링크가 404거나 APK가 안 바뀌면 Actions 탭에서 마지막 `main` 실행이 빨간 X인지 본다. 서명 키가 없으면 빌드는 해도 릴리스는 갱신하지 않고 빨간 X로 끝난다.
- "앱이 설치되지 않았습니다"가 뜨면 대개 예전 파일을 연 것이다. 방금 받은 파일인지, 설정 화면의 버전이 릴리스 버전보다 높지 않은지 먼저 본다. 서명 키를 바꿨거나 PC에서 깐 앱일 때만 지우고 설치한다. 지우면 설정한 이름이 모두 지워진다.
- `claude/`로 시작하는 브랜치 실행은 검증용이라 릴리스를 바꾸지 않는다. 그 빌드는 이 실행 전용 임시 키로 서명된다.

### 서명 키 보관과 다른 키 쓰기

키스토어 파일과 비밀번호는 저장소에 넣지 않고 따로 보관한다. 잃어버리면 새 키를 만들어 Secrets를 바꿔야 하고, 그때 한 번은 앱을 지우고 깔아야 한다.
필요하면 두 비밀을 더 넣을 수 있다.

| 이름 | 값 |
|---|---|
| `ANDROID_KEY_ALIAS` | 키 별칭. 비우면 키스토어에 키가 하나일 때 그 별칭을 쓰고, 아니면 `fieldcam` |
| `ANDROID_KEY_PASSWORD` | 키 비밀번호. 비우면 키스토어 비밀번호와 같다고 본다 |

새 키를 직접 만들 때는 JDK의 keytool과 base64를 쓴다. 윈도우는 Android Studio의 `jbr\bin\keytool.exe`가 있다.

```bash
keytool -genkeypair -storetype PKCS12 -keystore fieldcam.p12 -alias fieldcam -keyalg RSA -keysize 2048 -validity 36500
base64 -w0 fieldcam.p12 > fieldcam.p12.b64
```

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes((Resolve-Path fieldcam.p12))) | Set-Content -NoNewline fieldcam.p12.b64
```

## 동작

```
앱 실행
 └─ 선택 화면  [경상] [오버홀] 탭
      ├─ 경상:   위치 타일(최대 12)  ─────────────────────▶ 촬영 화면
      └─ 오버홀: 1차 분류 타일(최대 12) ─▶ 2차 분류 타일(최대 12) ─▶ 촬영 화면
                                                          ◀ ▶ 로 같은 1차 안에서 2차 이동
```

촬영 화면
- 상단에 현재 분류 이름 크게. 누르면 선택 화면으로.
- 하단 1/3 전체가 셔터. 볼륨 상·하, 카메라, 엔터, 헤드셋 버튼도 전부 셔터(블루투스 리모컨 호환).
- 손전등 토글. 후면 카메라 고정, 전환 없음. 화면 꺼짐 방지, 세로 고정.
- 화면은 세로 그대로지만 사진은 셔터를 누른 순간 폰을 든 방향으로 저장된다. 옆으로 들면 가로 사진, 거꾸로 들면 바로 선 사진이 된다.
- 바닥이나 천장처럼 거의 수평으로 들고 찍으면 센서가 방향을 몰라 직전 방향을 쓴다. 원하는 방향으로 먼저 들었다가 기울이면 된다. 비스듬히 들 때도 손떨림에 방향이 오가지 않게 직전 방향을 유지한다.
- 사진은 픽셀을 돌리지 않고 방향 정보(EXIF)만 기록한다. 갤럭시에서 볼륨 버튼이 위로 오게 들고 찍은 가로 사진은 방향 정보를 무시하는 프로그램에서도 바로 선다.
- 촬영 화면을 나가면 카메라를 놓아서 손전등도 꺼진다. 찍는 중인 사진이 있으면 저장을 마친 뒤에 놓고, 그 사이 다른 이름으로 들어가도 저장이 끝난 뒤에 카메라를 잡는다.
- 찍으면 화면 흰색 깜빡 + 진동 + "분류 N장" 1초 표시.

## 저장 위치와 파일명

| 모드 | 폴더 | 파일명 | 예 |
|---|---|---|---|
| 경상 | `Pictures/FieldCam/Routine/` | `{mmddyy}_{위치}_{순번}.jpg` | `110426_Boiler2_007.jpg` |
| 오버홀 | `Pictures/FieldCam/{공사}/` | `{공사}_{1차}_{2차}_{mmddyy}_{순번}.jpg` | `B2OH26_ESP_1A3_110426_002.jpg` |

- 순번은 날짜·분류별로 매일 001부터. 폴더 안에서 이 앱이 찍은 파일을 세서 다음 번호를 정한다.
- 앱을 지웠다 다시 깔거나 앱 데이터를 지우면 안드로이드가 예전 사진을 이 앱 것으로 보지 않는다. 그래서 그날 이미 찍은 분류는 번호가 001부터 다시 시작하고, 이름이 겹치는 사진은 갤러리가 파일명 뒤에 ` (1)`을 붙여 저장한다. "오늘 N장"도 다시 깐 뒤에 찍은 것만 센다. 사진이 지워지거나 덮어써지지는 않고 다음 날부터는 정상이다. 그래서 앱을 다시 까는 일은 그날 사진을 찍기 전에 하는 게 좋다.
- 이름은 영문·숫자·하이픈만. 공백과 밑줄은 하이픈으로 바뀐다. 밑줄은 파일명 구분자다.
- 오버홀 공사는 설정에서 여러 개 두고 "현재 공사"를 고른다. 공사마다 폴더가 따로 생긴다.

## 설정

- 경상 위치 12개: 기본값 Boiler1, Boiler2, Boiler3, STG, WTR, Sewage, Workshop, RWshop, Office, Store, HS, Etc
- 오버홀 공사: 이름, 1차 분류 12개, 각 1차 아래 2차 분류 12개. 새 공사를 추가하면 현재 공사의 분류 구성을 복사해서 시작한다.
- 설정은 DataStore 에 JSON 하나로 저장된다. 데이터베이스 없음.

## PC에서 직접 빌드

평소엔 필요 없다. 고친 코드를 GitHub 거치지 않고 바로 시험할 때만 쓴다.
Android Studio에서 저장소를 열고 Build → Generate APKs 하면 된다. 요구 사항:

- JDK 17 이상, Android SDK 35, minSdk 29 (Android 10 이상)
- 서명 키: 프로젝트 루트에 `keystore.properties`를 만들면 그 키로 서명한다. git에는 올라가지 않는다. 없으면 PC의 debug 키로 서명되어 GitHub에서 받은 앱 위에는 못 깐다.
- Kotlin 2.0, Jetpack Compose, CameraX 1.4

```properties
# keystore.properties (storeFile 은 절대 경로 또는 프로젝트 루트 기준 상대 경로, 윈도우는 C:/Users/... 처럼 슬래시)
# 값 안의 역슬래시(\)는 두 번(\\) 쓴다
storeFile=C:/Users/me/keys/fieldcam.p12
storePassword=키스토어 비밀번호
keyAlias=fieldcam
```

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
./gradlew :app:assembleRelease -PversionCode=1012 -PversionName=1.0.12
```

PC 빌드는 GitHub에서 받은 앱 위에 깔리지 않는다. 버전 번호가 기본값 `1.0-local`, versionCode 1 로 GitHub 빌드보다 낮고, `keystore.properties`가 없으면 서명 키도 다르다.
Android Studio의 ▶ 실행이 기존 앱을 지울지 물으면 취소한다. 지우면 설정한 이름이 지워진다. 평소엔 GitHub 링크로만 설치한다.

Android 10 이상에서는 `Pictures/` 아래에 자기 사진을 넣는 데 저장소 권한이 필요 없다. 앱이 요구하는 권한은 카메라뿐이다.

## 구조

```
.github/workflows/apk.yml   GitHub 자동 빌드: 단위 테스트, release APK, 서명 대조, 릴리스 apk-latest 갱신
app/src/main/java/com/fieldcam/
├── MainActivity.kt         화면 전환, 카메라 권한, 하드웨어 키 → 셔터
├── core/                   안드로이드 의존성 없는 순수 로직 (단위 테스트 대상)
│   ├── Config.kt           설정 모델과 기본값
│   ├── DeviceRotation.kt   폰 기울기 각도 → 사진 방향 (흔들림 방지 여유각 포함)
│   ├── Naming.kt           날짜 표기, 이름 정리, 파일명·순번 규칙
│   └── Target.kt           찍을 사진의 분류 → 폴더·파일명 접두어
├── data/
│   ├── ConfigStore.kt      DataStore 에 설정 JSON 저장
│   └── PhotoStore.kt       MediaStore 조회로 다음 순번 결정, 저장 메타 생성
└── ui/
    ├── SelectScreen.kt     [경상][오버홀] 탭 + 타일
    ├── CaptureScreen.kt    CameraX 프리뷰, 셔터, 손전등, 피드백, 카메라 잡고 놓기 순서
    ├── DeviceRotationState.kt  폰 기울기 센서 (앱 전체에서 하나)
    ├── SettingsScreen.kt   이름 편집
    ├── Tiles.kt            장갑용 큰 타일 격자 (≤6개 2열, 그 외 3열)
    └── Theme.kt
```

## 팁

갤럭시 설정 → 유용한 기능 → 측면 버튼 → "두 번 누르기"를 이 앱으로 바꿔두면
주머니에서 꺼내자마자 선택 화면이 뜬다.
