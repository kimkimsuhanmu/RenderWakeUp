package com.example.renderwakeup.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.renderwakeup.ui.theme.RenderWakeUpTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUrlScreen(
    onNavigateBack: () -> Unit
) {
    // 상태 관리
    val url = remember { mutableStateOf("https://example.render.com") }
    val interval = remember { mutableStateOf("10") }
    val emailNotification = remember { mutableStateOf(true) }
    val emailAddress = remember { mutableStateOf("user@example.com") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("URL 추가") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // URL 입력 필드
            OutlinedTextField(
                value = url.value,
                onValueChange = { url.value = it },
                label = { Text("URL") },
                placeholder = { Text("https://example.render.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 호출 주기 입력 필드
            OutlinedTextField(
                value = interval.value,
                onValueChange = { interval.value = it },
                label = { Text("호출 주기 (분)") },
                placeholder = { Text("10") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 이메일 알림 토글
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "이메일 알림",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "실패 시 이메일 알림 받기",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Switch(
                        checked = emailNotification.value,
                        onCheckedChange = { emailNotification.value = it }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 이메일 주소 입력 필드 (토글이 켜져 있을 때만 표시)
            if (emailNotification.value) {
                OutlinedTextField(
                    value = emailAddress.value,
                    onValueChange = { emailAddress.value = it },
                    label = { Text("알림 이메일 주소") },
                    placeholder = { Text("user@example.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 저장 버튼
            Button(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("저장")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddUrlScreenPreview() {
    RenderWakeUpTheme {
        AddUrlScreen(
            onNavigateBack = {}
        )
    }
}
