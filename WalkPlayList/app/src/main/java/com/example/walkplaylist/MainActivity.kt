package com.example.walkplaylist

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.walkplaylist.ui.theme.WalkPlayListTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PlayBox(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxWidth().height(56.dp).padding(8.dp), contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp).clickable { navController.navigate("player") }) {
                Image(
                    painter = painterResource(id = R.drawable.ic_album),  // R.drawable.ic_album 리소스를 사용
                    contentDescription = "Album Cover",  // 접근성을 위한 설명
                    modifier = Modifier.size(40.dp)  // 크기를 조절할 수 있음
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "어벤디 facilitation summary")
            }
            IconButton(onClick = { /* TODO: Add play/pause action */ }) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
            }
            IconButton(onClick = { /* TODO: Add next action */ }) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
            }
        }
    }
}


class MainActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WalkPlayListTheme {
                val navController = rememberNavController()
                val playlists = remember { mutableStateOf(mutableMapOf("즐겨찾기한 항목" to mutableListOf<String>())) }

                NavHost(navController = navController, startDestination = "main") {
                    composable("main") { MainScreen(navController, playlists) }
                    composable("player") { PlayerScreen(navController) }
                    composable("details") { DetailScreen(navController) }
                    composable("shorts") { ShortsScreen(navController) }
                    composable("news") { NewsScreen(navController, playlists) }
                    composable("custom") { CustomScreen(navController) }
                    composable("library") { LibraryScreen(navController, playlists) }
                    composable("create_playlist") { CreatePlaylistScreen(navController, playlists) }
                    composable("playlist/{playlistName}") { backStackEntry ->
                        PlaylistScreen(
                            navController, backStackEntry.arguments?.getString("playlistName") ?: "", playlists
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}


@Composable
fun MainScreen(navController: NavController, playlists: MutableState<MutableMap<String, MutableList<String>>>) {
    val scope = rememberCoroutineScope()
    var showNotification by remember { mutableStateOf(false) }
    var notificationMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("조회수 높은", "유머", "로맨스", "공포").forEach { label ->
                Button(
                    onClick = { /* TODO: Add action */ }, modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(text = label)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "최근 재생된 항목")
        repeat(4) { index ->
            val songTitle = "노래 제목 $index"
            ListItem(headlineContent = { Text(songTitle) }, leadingContent = {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
            }, trailingContent = {
                var isFavorite = remember { mutableStateOf(false) }
                Icon(imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = if (isFavorite.value) Color.Red else Color.Gray,
                    modifier = Modifier.clickable {
                        isFavorite.value = !isFavorite.value
                        if (isFavorite.value) {
                            playlists.value.getOrPut("즐겨찾기한 항목") { mutableListOf() }.add(songTitle)
                            notificationMessage = "$songTitle 이(가) 즐겨찾기한 항목에 추가되었습니다."
                            showNotification = true
                            scope.launch {
                                delay(2000)
                                showNotification = false
                            }
                        }
                    })
            }, modifier = Modifier.clickable { navController.navigate("details") })
        }

        if (showNotification) {
            Text(
                text = notificationMessage, color = Color.Green, modifier = Modifier.padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        PlayBox(navController)
        BottomNavBar(navController, currentTab = "main")
    }
}


@Composable
fun PlayerScreen(navController: NavController) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0f) }
    var duration by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.sample).apply {
            setOnCompletionListener {
                isPlaying = false
            }
        }
    }

    fun toggleAudio() {
        if (isPlaying) {
            mediaPlayer?.pause()
        } else {
            mediaPlayer.start()
        }
        isPlaying = !isPlaying
    }

    LaunchedEffect(isPlaying) {
        duration = mediaPlayer?.duration ?: 0
        while (isPlaying) {
            currentPosition =
                (mediaPlayer?.currentPosition?.toFloat() ?: 0f) / (duration.coerceAtLeast(1).toFloat())
            delay(1000L)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(300.dp).clip(RoundedCornerShape(75.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_album),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier.fillMaxSize().background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.6f)), startY = 200f
                    )
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "어벤디 Facilitation summary", color = Color.White, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_plus), // 임의의 첫 번째 아이콘
                contentDescription = "Plus", tint = Color.White, modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(44.dp)) // 아이콘 간 간격

            Icon(
                painter = painterResource(id = R.drawable.ic_favorite), // 임의의 두 번째 아이콘
                contentDescription = "Favorite", tint = Color.White, modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(44.dp)) // 아이콘 간 간격
            Icon(
                painter = painterResource(id = R.drawable.ic_share), // 임의의 세 번째 아이콘
                contentDescription = "Share", tint = Color.White, modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp)) // 아이콘과 플레이바 간 간격

        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTime((currentPosition * duration).toInt()), color = Color.Gray, fontSize = 14.sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            Slider(
                value = currentPosition, onValueChange = { value ->
                    currentPosition = value
                    mediaPlayer?.seekTo((duration * value).toInt())
                }, modifier = Modifier.weight(1f), colors = SliderDefaults.colors(
                    thumbColor = Color.White, activeTrackColor = Color.Red, inactiveTrackColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = formatTime(duration), color = Color.Gray, fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 재생/일시정지 버튼과 아이콘들을 사각형 모양으로 표시
        Row(
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.padding(8.dp).size(40.dp).background(Color.Transparent)
                    .clickable { /* 이전 곡 기능 추가 가능 */ }, contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_shuffle),
                    contentDescription = "Shuffle",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Box(
                modifier = Modifier.padding(8.dp).size(40.dp).background(Color.Transparent)
                    .clickable { /* 이전 곡 기능 추가 가능 */ }, contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_skip_previous),
                    contentDescription = "Previous",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Box(
                modifier = Modifier.padding(8.dp).size(38.dp).background(Color.Transparent)
                    .clickable { toggleAudio() }, // 재생/일시정지 클릭 이벤트 추가
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
                modifier = Modifier.padding(8.dp).size(40.dp).background(Color.Transparent)
                    .clickable { /* 다음 곡 기능 추가 가능 */ }, contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_skip_next),
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Box(
                modifier = Modifier.padding(8.dp).size(40.dp).background(Color.Transparent)
                    .clickable { /* 다음 곡 기능 추가 가능 */ }, contentAlignment = Alignment.Center
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

private fun formatTime(milliseconds: Int): String {
    val minutes = (milliseconds / 1000) / 60
    val seconds = (milliseconds / 1000) % 60
    return "%02d:%02d".format(minutes, seconds)
}


@Composable
fun BottomNavBar(navController: NavController, currentTab: String) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.Black).padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 각 탭 아이템 정의
        val tabs = listOf(
            "홈" to Pair(R.drawable.ic_home, "main"),
            "숏츠" to Pair(R.drawable.ic_shorts, "shorts"),
            "뉴스" to Pair(R.drawable.ic_news, "news"),
            "커스텀" to Pair(R.drawable.ic_custom, "custom"),
            "보관함" to Pair(R.drawable.ic_box, "library")
        )

        // 탭 아이템을 반복 처리하여 UI 생성
        tabs.forEach { (label, iconAndRoute) ->
            val (iconResId, route) = iconAndRoute
            val isSelected = route == currentTab
            val textColor = if (isSelected) Color(0xFFFFC0CB) else Color.Gray
            val iconTint = if (isSelected) Color(0xFFFFC0CB) else Color.White

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                if (navController.currentBackStackEntry?.destination?.route != route) {
                    navController.navigate(route) {
                        popUpTo("main") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = label, color = textColor, fontSize = 10.sp
                )
            }
        }
    }
}


@Composable
fun ShortsScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Shorts 화면 내용")

        BottomNavBar(navController, currentTab = "shorts")
    }
}

@Composable
fun DetailScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "상세 페이지 내용")
    }
}

@Composable
fun CustomScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // 입력 필드: 내용 입력
        val contentState = remember { mutableStateOf(TextFieldValue()) }
        Text(text = "내용 입력")
        TextField(
            value = contentState.value,
            onValueChange = { contentState.value = it },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        // 입력 필드: 키워드 입력
        val keywordState = remember { mutableStateOf(TextFieldValue()) }
        Text(text = "키워드 입력")
        TextField(
            value = keywordState.value,
            onValueChange = { keywordState.value = it },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        // 입력 필드: 곡 분위기 입력
        val moodState = remember { mutableStateOf(TextFieldValue()) }
        Text(text = "곡 분위기 입력")
        TextField(
            value = moodState.value,
            onValueChange = { moodState.value = it },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 생성 버튼 추가
        Button(
            onClick = { /* TODO: Add create action */ }, modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "생성")
        }

        Spacer(modifier = Modifier.weight(1f))

        // 하단 재생 박스
        PlayBox(navController)

        // 하단 탭 메뉴
        BottomNavBar(navController, currentTab = "custom")
    }
}

@Composable
fun NewsScreen(navController: NavController, playlists: MutableState<MutableMap<String, MutableList<String>>>) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(text = "오늘의 이슈", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        repeat(4) { index -> // 예제: 리스트 4개
            val songTitle = "뉴스 노래 제목 $index"
            ListItem(headlineContent = { Text(songTitle) }, leadingContent = {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Gray)
            }, trailingContent = {
                var isFavorite = remember { mutableStateOf(false) }
                Icon(imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = if (isFavorite.value) Color.Red else Color.Gray,
                    modifier = Modifier.clickable {
                        isFavorite.value = !isFavorite.value
                        if (isFavorite.value) {
                            playlists.value.getOrPut("즐겨찾기한 항목") { mutableListOf() }.add(songTitle)
                        }
                    })
            }, modifier = Modifier.clickable { navController.navigate("details") })
        }

        Spacer(modifier = Modifier.weight(1f))

        // 하단 재생 박스
        PlayBox(navController)

        // 하단 탭 메뉴
        BottomNavBar(navController, currentTab = "news")
    }
}

@Composable
fun LibraryScreen(
    navController: NavController, playlists: MutableState<MutableMap<String, MutableList<String>>>
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(text = "Artists Name", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { /* TODO: Add play action */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(text = "Play", color = Color.White)
            }
            Button(
                onClick = { /* TODO: Add shuffle action */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text(text = "Shuffle", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigate("create_playlist") }) {
            Text(text = "플레이리스트 추가")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 즐겨찾기한 항목과 생성된 플레이리스트 목록
        playlists.value.forEach { (playlistName, songs) ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clickable { navController.navigate("playlist/$playlistName") }.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = playlistName, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        // 하단 재생 박스
        PlayBox(navController)

        // 하단 탭 메뉴
        BottomNavBar(navController, currentTab = "library")
    }
}

@Composable
fun CreatePlaylistScreen(
    navController: NavController, playlists: MutableState<MutableMap<String, MutableList<String>>>
) {
    val playlistNameState = remember { mutableStateOf(TextFieldValue()) }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "플레이리스트 이름:")
        TextField(
            value = playlistNameState.value,
            onValueChange = { playlistNameState.value = it },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
        Button(onClick = {
            if (playlistNameState.value.text.isNotBlank()) {
                playlists.value[playlistNameState.value.text] = mutableListOf()
                navController.popBackStack()
            }
        }) {
            Text(text = "확인")
        }
    }
}

@Composable
fun PlaylistScreen(
    navController: NavController,
    playlistName: String,
    playlists: MutableState<MutableMap<String, MutableList<String>>>
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(text = playlistName, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        playlists.value[playlistName]?.forEach { song ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = song, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        // 하단 재생 박스
        PlayBox(navController)

        // 하단 탭 메뉴
        BottomNavBar(navController, currentTab = playlistName)
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    WalkPlayListTheme {
        val navController = rememberNavController()
        val playlists = remember { mutableStateOf(mutableMapOf("즐겨찾기한 항목" to mutableListOf<String>())) }
        MainScreen(navController, playlists)
    }
}
