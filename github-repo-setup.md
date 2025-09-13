# GitHub 저장소 설정 및 코드 업로드 가이드

## 1. GitHub 저장소 생성
1. 웹 브라우저에서 GitHub(https://github.com)에 로그인합니다.
2. 오른쪽 상단의 "+" 아이콘을 클릭하고 "New repository"를 선택합니다.
3. 저장소 이름을 "RenderWakeUp"으로 입력합니다.
4. 설명(선택사항)을 입력합니다: "Render 무료 플랜 서버를 슬립모드에서 깨우는 안드로이드 앱"
5. 저장소를 Public 또는 Private으로 설정합니다.
6. "Create repository" 버튼을 클릭합니다.

## 2. 로컬 저장소 연결 및 코드 푸시
GitHub에서 저장소를 생성한 후, 다음 명령어를 실행하여 로컬 저장소를 연결하고 코드를 푸시합니다:

```bash
# 원격 저장소 추가 (YOUR_USERNAME을 GitHub 사용자명으로 변경)
git remote add origin https://github.com/YOUR_USERNAME/RenderWakeUp.git

# 코드 푸시
git push -u origin master
```

## 3. GitHub Actions 워크플로우 확인
1. GitHub 저장소 페이지에서 "Actions" 탭을 클릭합니다.
2. "Android CI" 워크플로우가 자동으로 실행되는 것을 확인합니다.
3. 워크플로우가 완료될 때까지 기다립니다.

## 4. APK 다운로드
1. 워크플로우가 완료되면 해당 워크플로우를 클릭합니다.
2. "Artifacts" 섹션에서 "app-debug" 또는 "app-release"를 클릭하여 APK 파일을 다운로드합니다.
