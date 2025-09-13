package com.example.renderwakeup.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.renderwakeup.ui.theme.Gray500
import com.example.renderwakeup.ui.theme.RenderWakeUpTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    // 상태 관리
    val autoStart = remember { mutableStateOf(true) }
    val foregroundService = remember { mutableStateOf(true) }
    val emailNotification = remember { mutableStateOf(true) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 일반 섹션
            SettingsSectionHeader(title = "일반")
            
            SettingsSwitchItem(
                title = "자동 시작",
                description = "기기 재부팅 시 자동으로 앱 실행",
                checked = autoStart.value,
                onCheckedChange = { autoStart.value = it }
            )
            
            SettingsSwitchItem(
                title = "포그라운드 서비스",
                description = "앱이 항상 실행되도록 유지",
                checked = foregroundService.value,
                onCheckedChange = { foregroundService.value = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 알림 섹션
            SettingsSectionHeader(title = "알림")
            
            SettingsSwitchItem(
                title = "이메일 알림",
                description = "서버 응답 실패 시 이메일 알림 발송",
                checked = emailNotification.value,
                onCheckedChange = { emailNotification.value = it }
            )
            
            SettingsNavigationItem(
                title = "SMTP 설정",
                description = "이메일 발송을 위한 SMTP 서버 설정",
                onClick = { /* SMTP 설정 화면으로 이동 */ }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 정보 섹션
            SettingsSectionHeader(title = "정보")
            
            SettingsInfoItem(
                title = "앱 버전",
                value = "1.0.0"
            )
            
            SettingsNavigationItem(
                title = "개발자 정보",
                description = "앱 개발자 및 오픈소스 라이선스",
                onClick = { /* 개발자 정보 화면으로 이동 */ }
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
    Divider()
}

@Composable
fun SettingsSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
    Divider()
}

@Composable
fun SettingsNavigationItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Gray500
        )
    }
    Divider()
}

@Composable
fun SettingsInfoItem(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray500
        )
    }
    Divider()
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    RenderWakeUpTheme {
        SettingsScreen(
            onNavigateBack = {}
        )
    }
}

// clickable modifier 추가
fun Modifier.clickable(onClick: () -> Unit): Modifier {
    return this.then(androidx.compose.foundation.clickable(onClick = onClick))
}
