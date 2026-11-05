# FieldCam

발전소 현장 사진 전용 카메라. **찍기 전에 이름을 정하는 카메라**다.
장갑을 낀 채로 큰 타일을 한두 번 눌러 "여기가 어디인지"를 정하고, 그 다음은 셔터만 누른다.
사진은 폴더와 파일명에 분류가 박힌 채로 갤러리에 바로 저장된다. 나중에 정리할 일이 없다.

## 설치와 업데이트

`main`에 합칠 때마다 GitHub Actions가 APK를 만들어 아래 링크에 덮어쓴다. PC는 필요 없다.

- 다운로드: https://github.com/kimlin70-spec/fieldcam/releases/download/apk-latest/fieldcam.apk
- 릴리스 페이지: https://github.com/kimlin70-spec/fieldcam/releases/tag/apk-latest

1. 폰 브라우저에서 다운로드 링크를 연다. Chrome이 "해를 끼칠 수 있는 파일"이라고 물으면 다운로드를 누른다.
2. 받은 `fieldcam.apk`를 열면 "출처를 알 수 없는 앱" 설정으로 간다. APK를 연 앱(Chrome이나 내 파일)의 **이 출처 허용**을 켜고 돌아온다. 삼성은 설정 → 보안 및 개인정보 보호 → 자동 차단이 켜져 있으면 먼저 끈다.
3. Play 프로텍트가 막으면 **자세히 알아보기** 아래의 **무시하고 설치**를 고른다.
4. 업데이트도 같은 링크에서 받아 설치하면 덮어쓰기 된다. 설정한 이름은 그대로 남는다.

설정 화면 오른쪽 위의 `v1.0.N`이 지금 깔린 빌드 번호다. 릴리스 페이지의 앱 버전과 같으면 최신이다.
링크가 404면 Actions 탭에서 마지막 실행이 빨간 X인지 본다.

### 서명 키 (처음 한 번)

안드로이드는 같은 키로 서명된 APK만 덮어쓰기 설치를 허용한다. 키는 공개 저장소에 두지 않고 GitHub의 비공개 설정(Secrets)에 둔다.
저장소 Settings → Secrets and variables → Actions → **New repository secret**으로 두 개를 넣는다.

| 이름 | 값 |
|---|---|
| `ANDROID_KEYSTORE_B64` | 키스토어 파일을 base64로 바꾼 한 줄 문자열 |
| `ANDROID_KEYSTORE_PASSWORD` | 키스토어 비밀번호 |

넣은 뒤에는 Actions → **Build Android APK** → **Run workflow**(main)로 한 번 다시 빌드한다.
비밀이 없으면 빌드마다 임시 키로 서명된다. 설치는 되지만 업데이트할 때마다 앱을 지우고 깔아야 하고, 그때 설정한 이름도 지워진다. 릴리스 설명의 "서명" 줄에서 어느 키인지 보인다.
키스토어 파일과 비밀번호는 따로 보관한다. 잃어버리면 새 키를 만들어야 하고, 그때 한 번은 앱을 지우고 깔아야 한다.

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
- 찍으면 화면 흰색 깜빡 + 진동 + "분류 N장" 1초 표시.

## 저장 위치와 파일명

| 모드 | 폴더 | 파일명 | 예 |
|---|---|---|---|
| 경상 | `Pictures/FieldCam/Routine/` | `{mmddyy}_{위치}_{순번}.jpg` | `110426_Boiler2_007.jpg` |
| 오버홀 | `Pictures/FieldCam/{공사}/` | `{공사}_{1차}_{2차}_{mmddyy}_{순번}.jpg` | `B2OH26_ESP_1A3_110426_002.jpg` |

- 순번은 날짜·분류별로 매일 001부터. 폴더 안에서 이 앱이 찍은 파일을 세서 다음 번호를 정한다.
- 앱을 지웠다 다시 깔면 안드로이드가 예전 사진을 이 앱 것으로 보지 않는다. 그래서 그날 이미 찍은 분류는 번호가 001부터 다시 시작하고, 이름이 겹치면 갤러리가 파일명 뒤에 ` (1)`을 붙여 저장한다. 사진이 지워지거나 덮어써지지는 않고, 다음 날부터는 겹치지 않는다.
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
storeFile=C:/Users/me/keys/fieldcam.p12
storePassword=키스토어 비밀번호
keyAlias=fieldcam
```

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
./gradlew :app:assembleRelease -PversionCode=1012 -PversionName=1.0.12
```

PC 빌드의 버전 번호는 기본값 `1.0-local`, versionCode 1 이다. GitHub 빌드보다 낮아서, GitHub APK가 깔린 폰에 PC APK를 파일로 덮어 깔면 "앱이 설치되지 않았습니다"가 뜬다. 그때는 위의 `-PversionCode`를 더 크게 주고 빌드한다.

Android 10 이상에서는 `Pictures/` 아래에 자기 사진을 넣는 데 저장소 권한이 필요 없다. 앱이 요구하는 권한은 카메라뿐이다.

## 구조

```
.github/workflows/apk.yml   GitHub 자동 빌드: 단위 테스트, release APK, 서명 대조, 릴리스 apk-latest 갱신
app/src/main/java/com/fieldcam/
├── MainActivity.kt         화면 전환, 카메라 권한, 하드웨어 키 → 셔터
├── core/                   안드로이드 의존성 없는 순수 로직 (단위 테스트 대상)
│   ├── Config.kt           설정 모델과 기본값
│   ├── Naming.kt           날짜 표기, 이름 정리, 파일명·순번 규칙
│   └── Target.kt           찍을 사진의 분류 → 폴더·파일명 접두어
├── data/
│   ├── ConfigStore.kt      DataStore 에 설정 JSON 저장
│   └── PhotoStore.kt       MediaStore 조회로 다음 순번 결정, 저장 메타 생성
└── ui/
    ├── SelectScreen.kt     [경상][오버홀] 탭 + 타일
    ├── CaptureScreen.kt    CameraX 프리뷰, 셔터, 손전등, 피드백
    ├── SettingsScreen.kt   이름 편집
    ├── Tiles.kt            장갑용 큰 타일 격자 (≤6개 2열, 그 외 3열)
    └── Theme.kt
```

## 팁

갤럭시 설정 → 유용한 기능 → 측면 버튼 → "두 번 누르기"를 이 앱으로 바꿔두면
주머니에서 꺼내자마자 선택 화면이 뜬다.
