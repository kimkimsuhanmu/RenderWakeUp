# 렌더웨이크 디자인 토큰 문서

## 색상 팔레트

### 기본 색상
- **Primary**: #0D47A1 (진한 파란색) - 앱의 주요 브랜드 색상
- **Secondary**: #00BFA5 (청록색) - 액션 및 강조 요소
- **Accent**: #FF6D00 (주황색) - 중요 알림 및 경고

### 그레이스케일
- **Gray 100**: #F5F5F5 - 배경, 경계선
- **Gray 200**: #EEEEEE - 비활성화된 요소
- **Gray 300**: #E0E0E0 - 구분선
- **Gray 400**: #BDBDBD - 비활성화된 텍스트
- **Gray 500**: #9E9E9E - 보조 텍스트
- **Gray 600**: #757575 - 기본 텍스트
- **Gray 700**: #616161 - 강조 텍스트
- **Gray 800**: #424242 - 제목 텍스트
- **Gray 900**: #212121 - 최상위 제목 텍스트

### 상태 색상
- **Success**: #4CAF50 - 성공 상태
- **Error**: #F44336 - 오류 상태
- **Warning**: #FF9800 - 경고 상태
- **Info**: #2196F3 - 정보 상태

## 타이포그래피 & 폰트

### 폰트 패밀리
- **기본 폰트**: Noto Sans KR - 한글 지원 최적화
- **대체 폰트**: Roboto - 영문 및 숫자용

### 제목 스타일
- **H1**: 
  - 크기: 24sp
  - 두께: Bold (700)
  - 행간: 32sp
  - 용도: 화면 제목, 주요 섹션 제목

- **H2**: 
  - 크기: 20sp
  - 두께: Bold (700)
  - 행간: 28sp
  - 용도: 섹션 제목

- **H3**: 
  - 크기: 18sp
  - 두께: Medium (500)
  - 행간: 24sp
  - 용도: 카드 제목, 목록 제목

### 본문 텍스트
- **Body1**: 
  - 크기: 16sp
  - 두께: Regular (400)
  - 행간: 24sp
  - 용도: 기본 본문 텍스트

- **Body2**: 
  - 크기: 14sp
  - 두께: Regular (400)
  - 행간: 20sp
  - 용도: 보조 본문 텍스트

### 캡션 / 오버라인
- **Caption**: 
  - 크기: 12sp
  - 두께: Regular (400)
  - 행간: 16sp
  - 용도: 보조 정보, 타임스탬프

- **Overline**: 
  - 크기: 10sp
  - 두께: Medium (500)
  - 행간: 14sp
  - 용도: 레이블, 상태 표시

## 구현 예시

### XML (Android)

#### colors.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- 기본 색상 -->
    <color name="primary">#0D47A1</color>
    <color name="secondary">#00BFA5</color>
    <color name="accent">#FF6D00</color>
    
    <!-- 그레이스케일 -->
    <color name="gray_100">#F5F5F5</color>
    <color name="gray_200">#EEEEEE</color>
    <color name="gray_300">#E0E0E0</color>
    <color name="gray_400">#BDBDBD</color>
    <color name="gray_500">#9E9E9E</color>
    <color name="gray_600">#757575</color>
    <color name="gray_700">#616161</color>
    <color name="gray_800">#424242</color>
    <color name="gray_900">#212121</color>
    
    <!-- 상태 색상 -->
    <color name="success">#4CAF50</color>
    <color name="error">#F44336</color>
    <color name="warning">#FF9800</color>
    <color name="info">#2196F3</color>
</resources>
```

#### styles.xml (Typography)
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- 제목 스타일 -->
    <style name="TextAppearance.RenderWake.Headline1" parent="TextAppearance.MaterialComponents.Headline1">
        <item name="fontFamily">@font/noto_sans_kr</item>
        <item name="android:fontFamily">@font/noto_sans_kr</item>
        <item name="android:textSize">24sp</item>
        <item name="android:textStyle">bold</item>
        <item name="android:lineSpacingExtra">8sp</item>
        <item name="android:textColor">@color/gray_900</item>
    </style>
    
    <style name="TextAppearance.RenderWake.Headline2" parent="TextAppearance.MaterialComponents.Headline2">
        <item name="fontFamily">@font/noto_sans_kr</item>
        <item name="android:fontFamily">@font/noto_sans_kr</item>
        <item name="android:textSize">20sp</item>
        <item name="android:textStyle">bold</item>
        <item name="android:lineSpacingExtra">8sp</item>
        <item name="android:textColor">@color/gray_900</item>
    </style>
    
    <style name="TextAppearance.RenderWake.Headline3" parent="TextAppearance.MaterialComponents.Headline3">
        <item name="fontFamily">@font/noto_sans_kr</item>
        <item name="android:fontFamily">@font/noto_sans_kr</item>
        <item name="android:textSize">18sp</item>
        <item name="android:textStyle">normal</item>
        <item name="android:lineSpacingExtra">6sp</item>
        <item name="android:textColor">@color/gray_800</item>
    </style>
    
    <!-- 본문 스타일 -->
    <style name="TextAppearance.RenderWake.Body1" parent="TextAppearance.MaterialComponents.Body1">
        <item name="fontFamily">@font/noto_sans_kr</item>
        <item name="android:fontFamily">@font/noto_sans_kr</item>
        <item name="android:textSize">16sp</item>
        <item name="android:textStyle">normal</item>
        <item name="android:lineSpacingExtra">8sp</item>
        <item name="android:textColor">@color/gray_700</item>
    </style>
    
    <style name="TextAppearance.RenderWake.Body2" parent="TextAppearance.MaterialComponents.Body2">
        <item name="fontFamily">@font/noto_sans_kr</item>
        <item name="android:fontFamily">@font/noto_sans_kr</item>
        <item name="android:textSize">14sp</item>
        <item name="android:textStyle">normal</item>
        <item name="android:lineSpacingExtra">6sp</item>
        <item name="android:textColor">@color/gray_600</item>
    </style>
    
    <!-- 캡션/오버라인 스타일 -->
    <style name="TextAppearance.RenderWake.Caption" parent="TextAppearance.MaterialComponents.Caption">
        <item name="fontFamily">@font/noto_sans_kr</item>
        <item name="android:fontFamily">@font/noto_sans_kr</item>
        <item name="android:textSize">12sp</item>
        <item name="android:textStyle">normal</item>
        <item name="android:lineSpacingExtra">4sp</item>
        <item name="android:textColor">@color/gray_500</item>
    </style>
    
    <style name="TextAppearance.RenderWake.Overline" parent="TextAppearance.MaterialComponents.Overline">
        <item name="fontFamily">@font/noto_sans_kr</item>
        <item name="android:fontFamily">@font/noto_sans_kr</item>
        <item name="android:textSize">10sp</item>
        <item name="android:textStyle">normal</item>
        <item name="android:lineSpacingExtra">4sp</item>
        <item name="android:textColor">@color/gray_500</item>
        <item name="android:textAllCaps">true</item>
    </style>
</resources>
```

### Jetpack Compose

```kotlin
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// 색상 정의
object RenderWakeColors {
    // 기본 색상
    val Primary = Color(0xFF0D47A1)
    val Secondary = Color(0xFF00BFA5)
    val Accent = Color(0xFFFF6D00)
    
    // 그레이스케일
    val Gray100 = Color(0xFFF5F5F5)
    val Gray200 = Color(0xFFEEEEEE)
    val Gray300 = Color(0xFFE0E0E0)
    val Gray400 = Color(0xFFBDBDBD)
    val Gray500 = Color(0xFF9E9E9E)
    val Gray600 = Color(0xFF757575)
    val Gray700 = Color(0xFF616161)
    val Gray800 = Color(0xFF424242)
    val Gray900 = Color(0xFF212121)
    
    // 상태 색상
    val Success = Color(0xFF4CAF50)
    val Error = Color(0xFFF44336)
    val Warning = Color(0xFFFF9800)
    val Info = Color(0xFF2196F3)
}

// 타이포그래피 정의
object RenderWakeTypography {
    // 폰트 정의 (실제 구현 시 Font 리소스 로드 필요)
    private val NotoSansKR = FontFamily.Default // 실제 구현 시 Noto Sans KR 폰트 로드
    
    // 제목 스타일
    val H1 = TextStyle(
        fontFamily = NotoSansKR,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        color = RenderWakeColors.Gray900
    )
    
    val H2 = TextStyle(
        fontFamily = NotoSansKR,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        color = RenderWakeColors.Gray900
    )
    
    val H3 = TextStyle(
        fontFamily = NotoSansKR,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = RenderWakeColors.Gray800
    )
    
    // 본문 스타일
    val Body1 = TextStyle(
        fontFamily = NotoSansKR,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = RenderWakeColors.Gray700
    )
    
    val Body2 = TextStyle(
        fontFamily = NotoSansKR,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = RenderWakeColors.Gray600
    )
    
    // 캡션/오버라인 스타일
    val Caption = TextStyle(
        fontFamily = NotoSansKR,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = RenderWakeColors.Gray500
    )
    
    val Overline = TextStyle(
        fontFamily = NotoSansKR,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        color = RenderWakeColors.Gray500
    )
}
```
