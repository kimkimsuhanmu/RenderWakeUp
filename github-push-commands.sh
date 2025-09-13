#!/bin/bash

# GitHub 사용자명과 저장소 이름을 설정합니다.
# 아래 값을 실제 GitHub 사용자명과 원하는 저장소 이름으로 변경하세요.
GITHUB_USERNAME="your-username"
REPO_NAME="RenderWakeUp"

# 원격 저장소 추가
git remote add origin https://github.com/$GITHUB_USERNAME/$REPO_NAME.git

# 코드 푸시
git push -u origin master

echo "코드가 GitHub 저장소로 푸시되었습니다."
echo "https://github.com/$GITHUB_USERNAME/$REPO_NAME 에서 확인할 수 있습니다."
echo "GitHub Actions 워크플로우가 자동으로 실행되어 APK를 빌드합니다."
echo "Actions 탭에서 빌드 진행 상황을 확인하세요."
