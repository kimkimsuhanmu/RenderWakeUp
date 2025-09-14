# APK 빌드 방법

## GitHub Actions를 통한 빌드

GitHub Actions를 통해 APK를 빌드하려면 다음 단계를 따르세요:

1. 웹 브라우저에서 다음 URL로 이동합니다:
   ```
   https://github.com/kimkimsuhanmu/RenderWakeUp/actions
   ```

2. "Run workflow" 버튼을 클릭합니다.
   - "Android CI" 워크플로우를 선택합니다.
   - "Run workflow" 버튼을 다시 클릭하여 빌드를 시작합니다.

3. 빌드가 완료되면 해당 워크플로우를 클릭하고 "Artifacts" 섹션에서 APK 파일을 다운로드합니다.

## 로컬 빌드 문제 해결

현재 로컬 빌드에서 "Unsupported class file major version 68" 오류가 발생하는 이유는 시스템에 설치된 Java 버전이 Gradle과 호환되지 않기 때문입니다. 이 문제를 해결하려면:

1. Java 17 설치:
   - [AdoptOpenJDK](https://adoptium.net/) 또는 [Oracle JDK](https://www.oracle.com/java/technologies/downloads/#java17) 에서 Java 17을 다운로드하여 설치합니다.

2. JAVA_HOME 환경 변수 설정:
   ```
   setx JAVA_HOME "C:\Program Files\Java\jdk-17"
   ```

3. 새 명령 프롬프트 창을 열고 다음 명령어로 빌드:
   ```
   ./gradlew assembleDebug
   ```

## Docker를 사용한 빌드

Docker가 설치되어 있다면, 다음 명령어로 APK를 빌드할 수 있습니다:

```
docker run --rm -v "%cd%":/project -w /project openjdk:17-slim ./gradlew assembleDebug
```

이 명령어는 Java 17이 설치된 Docker 컨테이너 내에서 빌드를 실행합니다.
