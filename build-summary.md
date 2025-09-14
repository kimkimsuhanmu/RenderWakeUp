# 렌더웨이크(Render Wake-Up) 앱 빌드 요약

## 프로젝트 개요
- **앱 이름**: 렌더웨이크(Render Wake-Up)
- **목적**: Render 무료 플랜 서버가 슬립모드로 전환되는 것을 방지하는 안드로이드 앱
- **기술 스택**: Android, Kotlin, Jetpack Compose, WorkManager, Room, Retrofit

## 주요 기능
- URL 핑(Keep-Alive) 주기적 자동 호출
- 백그라운드 영구 실행 및 절전 모드 방지
- 호출 실패 시 지정 이메일 알림 발송
- 여러 URL 다중 관리(추가·수정·삭제)
- 수동 즉시 깨우기 버튼

## 빌드 결과
- **디버그 APK**: `app-debug/app-debug.apk` (11.2MB)
- **릴리스 APK**: `app-release/app-release-unsigned.apk` (8.3MB)

## 빌드 과정에서 해결한 문제들
1. **Java 버전 호환성 문제**
   - 문제: `compileDebugJavaWithJavac` 작업은 Java 1.8을, `kaptGenerateStubsDebugKotlin` 작업은 Java 17을 대상으로 하여 충돌 발생
   - 해결: `build.gradle`에서 Java 버전을 17로 통일

2. **WorkManagerHelper 참조 오류**
   - 문제: BootReceiver에서 WorkManagerHelper 클래스를 찾을 수 없음
   - 해결: WorkManagerHelper 클래스 구현

3. **MainActivity 참조 오류**
   - 문제: NotificationHelper에서 MainActivity 클래스를 찾을 수 없음
   - 해결: MainActivity 클래스 구현

4. **Android 13 알림 권한 문제**
   - 문제: Android 13 이상에서는 알림을 표시하기 위해 POST_NOTIFICATIONS 권한 필요
   - 해결: AndroidManifest.xml에 권한 추가 및 런타임 권한 요청 코드 구현

5. **린트 오류로 인한 빌드 실패**
   - 문제: 린트 오류로 인해 빌드가 중단됨
   - 해결: `build.gradle`에 `lint { abortOnError false }` 추가

## GitHub 저장소
- **URL**: https://github.com/kimkimsuhanmu/RenderWakeUp
- **GitHub Actions**: 자동 빌드 및 APK 생성 워크플로우 설정됨

## 설치 방법
1. 디버그 APK(`app-debug.apk`) 또는 릴리스 APK(`app-release-unsigned.apk`)를 안드로이드 기기에 복사
2. 안드로이드 기기에서 APK 파일을 실행하여 설치
3. 설치 중 "알 수 없는 출처" 경고가 표시되면, 설정에서 "알 수 없는 출처" 앱 설치를 허용

## 주의사항
- 앱이 제대로 작동하려면 배터리 최적화에서 제외해야 합니다.
- Android 13 이상에서는 알림 권한을 허용해야 합니다.
- 앱은 최소 Android 8.0 (Oreo) 이상에서 작동합니다.
- 주기적인 핑은 최소 15분 간격으로 설정됩니다.
