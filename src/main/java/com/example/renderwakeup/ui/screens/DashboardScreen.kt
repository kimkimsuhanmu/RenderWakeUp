package com.example.renderwakeup.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.renderwakeup.ui.theme.Error
import com.example.renderwakeup.ui.theme.RenderWakeUpTheme
import com.example.renderwakeup.ui.theme.Success
import com.example.renderwakeup.ui.theme.Warning

// 더미 데이터 모델
data class UrlItem(
    val id: Int,
    val url: String,
    val interval: Int,
    val lastPing: String,
    val status: String,
    val failCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAddUrl: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    // 더미 데이터
    val urlItems = remember {
        mutableStateOf(
            listOf(
                UrlItem(
                    id = 1,
                    url = "https://example.render.com",
                    interval = 5,
                    lastPing = "1분 전",
                    status = "success",
                    failCount = 0
                ),
                UrlItem(
                    id = 2,
                    url = "https://myapp.render.com",
                    interval = 10,
                    lastPing = "3분 전",
                    status = "success",
                    failCount = 0
                ),
                UrlItem(
                    id = 3,
                    url = "https://api.myproject.com",
                    interval = 15,
                    lastPing = "실패",
                    status = "error",
                    failCount = 3
                )
            )
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("렌더웨이크") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "설정"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddUrl,
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "URL 추가",
                    tint = Color.White
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // 서비스 상태 배너
            ServiceStatusBanner(isRunning = true)
            
            // URL 목록
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(urlItems.value) { item ->
                    UrlCard(item = item)
                }
            }
        }
    }
}

@Composable
fun ServiceStatusBanner(isRunning: Boolean) {
    val backgroundColor = if (isRunning) Success else Warning
    val text = if (isRunning) "서비스 실행 중" else "서비스 중지됨"
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = backgroundColor,
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun UrlCard(item: UrlItem) {
    val statusColor = when (item.status) {
        "success" -> Success
        "error" -> Error
        else -> Warning
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // URL
            Text(
                text = item.url,
                style = MaterialTheme.typography.displaySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 간격 및 마지막 핑 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.interval}분 간격",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = " | ",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "마지막 핑: ${item.lastPing}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 상태 및 메뉴
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier
                            .width(12.dp)
                            .height(12.dp),
                        color = statusColor,
                        shape = MaterialTheme.shapes.small
                    ) {}
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = if (item.status == "success") "정상" else "오류",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                IconButton(onClick = { /* 메뉴 열기 */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "더보기 메뉴"
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    RenderWakeUpTheme {
        DashboardScreen(
            onNavigateToAddUrl = {},
            onNavigateToSettings = {}
        )
    }
}
