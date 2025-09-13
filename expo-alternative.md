# Expo를 사용한 렌더웨이크 앱 빌드 방법

## Expo란?
Expo는 React Native 앱을 더 쉽게 개발하고 빌드할 수 있는 도구 세트입니다. Expo를 사용하면 네이티브 코드를 직접 건드리지 않고도 React Native 앱을 개발할 수 있으며, 클라우드에서 앱을 빌드하여 APK 파일을 생성할 수 있습니다.

## Expo를 사용한 빌드 과정

### 1. Expo CLI 설치
```bash
npm install -g expo-cli
```

### 2. 새 Expo 프로젝트 생성
```bash
expo init RenderWakeUp
cd RenderWakeUp
```

### 3. 필요한 패키지 설치
```bash
expo install expo-background-fetch expo-task-manager expo-notifications react-native-url-polyfill
```

### 4. 앱 구성 파일 수정
- `app.json` 파일에 필요한 권한 추가
- 백그라운드 작업 설정

### 5. 백그라운드 작업 구현
- 백그라운드 작업 등록
- 주기적인 URL 핑 기능 구현

### 6. APK 빌드
```bash
expo build:android -t apk
```

## 장점
1. **간편한 빌드 과정**: 로컬에 Android SDK나 개발 환경을 설정할 필요 없음
2. **클라우드 빌드**: Expo 서버에서 빌드를 처리하므로 로컬 컴퓨터의 성능에 영향을 받지 않음
3. **OTA 업데이트**: 앱 스토어 승인 없이 앱 업데이트 가능

## 단점
1. **제한된 네이티브 기능**: 일부 네이티브 기능은 Expo에서 지원하지 않을 수 있음
2. **백그라운드 작업 제한**: Android의 백그라운드 작업 제한으로 인해 15분마다 실행되는 것이 최소 주기일 수 있음
3. **앱 크기 증가**: Expo 런타임이 포함되어 앱 크기가 커질 수 있음

## 구현 시 고려사항
1. **백그라운드 작업 제한**: Android의 백그라운드 작업 제한을 고려해야 함
2. **배터리 최적화 예외**: 사용자에게 배터리 최적화 예외 설정을 안내해야 함
3. **Foreground Service**: 지속적인 실행을 위해 Foreground Service를 구현해야 함

## 결론
Expo는 빠르게 앱을 개발하고 빌드하는 데 유용하지만, 지속적인 백그라운드 작업이 필요한 렌더웨이크 앱의 경우 네이티브 Android 개발이 더 적합할 수 있습니다. 그러나 개발 환경 설정이 어렵거나 빠른 프로토타이핑이 필요한 경우 Expo를 사용하는 것도 고려해볼 만합니다.
