package com.example.renderwakeup.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.renderwakeup.R
import com.example.renderwakeup.ui.theme.RenderWakeUpTheme

@Composable
fun OnboardingScreen(
    onNavigateToDashboard: () -> Unit,
    onOpenBatterySettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 이미지 영역 (실제 이미지 리소스 필요)
        Image(
            painter = painterResource(id = R.drawable.onboarding_image),
            contentDescription = "배터리 최적화 설명 이미지",
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 32.dp)
        )
        
        // 제목
        Text(
            text = "배터리 최적화",
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 설명 텍스트
        Text(
            text = "앱이 백그라운드에서 계속 실행되려면 배터리 최적화 제외 설정이 필요합니다. 이 설정을 통해 앱이 서버를 깨우는 작업을 중단 없이 수행할 수 있습니다.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 배터리 설정 버튼
        Button(
            onClick = onOpenBatterySettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("배터리 설정 열기")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 건너뛰기 버튼
        TextButton(
            onClick = onNavigateToDashboard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("건너뛰기")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    RenderWakeUpTheme {
        OnboardingScreen(
            onNavigateToDashboard = {},
            onOpenBatterySettings = {}
        )
    }
}
