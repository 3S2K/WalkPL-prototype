package com.example.myapplication

import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.clickable
import android.os.Bundle
import android.media.MediaPlayer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import com.example.walkplaylist.R
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isPlaying by remember { mutableStateOf(false) }

            // 재생/정지 상태를 제어하는 함수
            fun toggleAudio() {
                if (isPlaying) {
                    stopAudio()
                } else {
                    playAudio()
                }
                isPlaying = !isPlaying
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                MusicPlayerScreen(
                    modifier = Modifier.weight(1f),
                    isPlaying = isPlaying,
                    onTogglePlay = { toggleAudio() }
                )
                BottomIconBar()
            }
        }
    }

    @Composable
    fun MusicPlayerScreen(
        modifier: Modifier = Modifier,
        isPlaying: Boolean,
        onTogglePlay: () -> Unit
    ) {
        var currentPosition by remember { mutableStateOf(0f) }
        var duration by remember { mutableStateOf(0) }

        LaunchedEffect(isPlaying) {
            if (isPlaying) {
                duration = mediaPlayer?.duration ?: 0
            }
            while (isPlaying) {
                currentPosition = (mediaPlayer?.currentPosition?.toFloat() ?: 0f) / (duration.toFloat().coerceAtLeast(1f))
                delay(1000L)
            }
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 앨범 이미지에 그라디언트 오버레이 추가
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(75.dp))
            ) {
                // 앨범 이미지
                Image(
                    painter = painterResource(id = R.drawable.ic_album),
                    contentDescription = "Album Art",
                    modifier = Modifier.fillMaxSize()
                )
                // 끝부분에 그라디언트 추가
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.6f)),
                                startY = 200f // 시작 위치 조정
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "어벤디 Facilitation summary", color = Color.White, fontSize = 16.sp)

            Spacer(modifier = Modifier.height(24.dp)) // 텍스트와 아이콘 간격

            // 3개의 아이콘을 추가하는 Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_plus), // 임의의 첫 번째 아이콘
                    contentDescription = "Plus",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(44.dp)) // 아이콘 간 간격

                Icon(
                    painter = painterResource(id = R.drawable.ic_favorite), // 임의의 두 번째 아이콘
                    contentDescription = "Favorite",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(44.dp)) // 아이콘 간 간격
                Icon(
                    painter = painterResource(id = R.drawable.ic_share), // 임의의 세 번째 아이콘
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp)) // 아이콘과 플레이바 간 간격

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTime((currentPosition * duration).toInt()),
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.width(8.dp))

                Slider(
                    value = currentPosition,
                    onValueChange = { value ->
                        currentPosition = value
                        mediaPlayer?.seekTo((duration * value).toInt())
                    },
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.Red,
                        inactiveTrackColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = formatTime(duration),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 재생/일시정지 버튼과 아이콘들을 사각형 모양으로 표시
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(40.dp)
                        .background(Color.Transparent)
                        .clickable { /* 이전 곡 기능 추가 가능 */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_shuffle),
                        contentDescription = "Shuffle",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(40.dp)
                        .background(Color.Transparent)
                        .clickable { /* 이전 곡 기능 추가 가능 */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_skip_previous),
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(38.dp)
                        .background(Color.Transparent)
                        .clickable { onTogglePlay() }, // 재생/일시정지 클릭 이벤트 추가
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                        ),
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(40.dp)
                        .background(Color.Transparent)
                        .clickable { /* 다음 곡 기능 추가 가능 */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_skip_next),
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(40.dp)
                        .background(Color.Transparent)
                        .clickable { /* 다음 곡 기능 추가 가능 */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_repeat),
                        contentDescription = "Repeat",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(48.dp)) // 재생 아이콘과 첫 번째 텍스트 사이 간격

            // 텍스트 1과 텍스트 2 사이에 넉넉한 간격을 두기 위해 Spacer 사용
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "다음 트랙", color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(40.dp)) // 두 텍스트 사이의 간격
                Text(text = "가사", color = Color.White, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp)) // 두 번째 텍스트와 하단 아이콘 바 사이 간격
        }
    }



    private fun playAudio() {
        mediaPlayer = MediaPlayer.create(this, R.raw.sample)
        mediaPlayer?.start()
    }

    private fun stopAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    @Composable
    fun BottomIconBar() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home 아이콘과 텍스트
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { /* 첫 번째 아이콘 클릭 동작 */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_home), // ic_home 아이콘이 res/drawable에 있어야 함
                        contentDescription = "Home",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text("Home", color = Color.White, fontSize = 10.sp)
            }

            // Shorts 아이콘과 텍스트
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { /* 두 번째 아이콘 클릭 동작 */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_shorts), // ic_shorts 아이콘 필요
                        contentDescription = "Shorts",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text("Shorts", color = Color.White, fontSize = 10.sp)
            }

            // News 아이콘과 텍스트
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { /* 세 번째 아이콘 클릭 동작 */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_news), // ic_news 아이콘 필요
                        contentDescription = "News",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text("News", color = Color.White, fontSize = 10.sp)
            }

            // Custom 아이콘과 텍스트
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { /* 네 번째 아이콘 클릭 동작 */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_custom), // ic_custom 아이콘 필요
                        contentDescription = "Custom",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text("Custom", color = Color.White, fontSize = 10.sp)
            }

            // Box 아이콘과 텍스트
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { /* 다섯 번째 아이콘 클릭 동작 */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_box), // ic_box 아이콘 필요
                        contentDescription = "Box",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text("Box", color = Color.White, fontSize = 10.sp)
            }
        }
    }


    private fun formatTime(milliseconds: Int): String {
        val minutes = (milliseconds / 1000) / 60
        val seconds = (milliseconds / 1000) % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}