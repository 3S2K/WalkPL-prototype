package com.example.walkplaylist

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.collection.emptyLongSet
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
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
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.widget.Toast
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle

class MusicViewModel : ViewModel() {
    // 현재 재생 중인 노래 제목
    private val _currentSongTitle = mutableStateOf("어벤디 Facilition")
    val currentSongTitle: State<String> = _currentSongTitle

    // 현재 재생 중인 노래 제목 업데이트 함수
    fun updateCurrentSongTitle(newTitle: String) {
        _currentSongTitle.value = newTitle
    }
}


@Composable
fun PlayBox(navController: NavController, musicViewModel: MusicViewModel) {
    val currentSongTitle by musicViewModel.currentSongTitle

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(8.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { navController.navigate("player") }, // 전체 바 클릭 시 PlayerScreen으로 이동
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_album),
                    contentDescription = "Album",
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = currentSongTitle)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* TODO: Add play/pause action */ }) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(3.dp))
                IconButton(onClick = { /* TODO: Add next action */ }) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_skip_next),
                        contentDescription = "Next",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private val musicViewModel: MusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WalkPlayListTheme {
                val navController = rememberNavController()
                val playlists = remember {
                    mutableStateOf(
                        mutableMapOf(
                            "즐겨찾기한 항목" to mutableListOf<String>(),
                            "내가 만든 곡" to mutableListOf<String>() // 디폴트 항목 추가
                        )
                    )
                }

                Scaffold(
                    bottomBar = {
                        val currentBackStackEntry = navController.currentBackStackEntryAsState()
                        val currentRoute = currentBackStackEntry.value?.destination?.route

                        if (currentRoute != "player") {
                            Column {
                                PlayBox(navController = navController, musicViewModel = musicViewModel)
                                BottomNavBar(
                                    navController = navController,
                                    currentTab = currentRoute ?: "main"
                                )
                            }
                        } else {
                            BottomNavBar(
                                navController = navController,
                                currentTab = currentRoute ?: "main"
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "main",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("main") { MainScreen(navController, playlists, musicViewModel) }
                        composable("player") { PlayerScreen(navController, musicViewModel) }
                        composable("details") { DetailScreen(navController) }
                        composable("Pedometer") { StepCounter(navController) }
                        composable("news") { NewsScreen(navController, playlists) }
                        composable("custom") { CustomScreen(navController) }
                        composable("library") { LibraryScreen(navController, playlists) }
                        composable("create_playlist") { CreatePlaylistScreen(navController, playlists) }
                        composable("playlist/{playlistName}") { backStackEntry ->
                            PlaylistScreen(
                                navController,
                                backStackEntry.arguments?.getString("playlistName") ?: "",
                                playlists
                            )
                        }
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
fun MainScreen(navController: NavController, playlists: MutableState<MutableMap<String, MutableList<String>>>, musicViewModel: MusicViewModel) {
    val scope = rememberCoroutineScope()
    var showNotification by remember { mutableStateOf(false) }
    var notificationMessage by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header 부분
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "WalkPL", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = { /* TODO: 사용자 프로필 액션 */ }) {
                Icon(Icons.Default.Person, contentDescription = "Profile")
            }
        }

        // 전체 내용 스크롤 가능
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                // 최근 재생 섹션
                Text(
                    text = "최근 재생",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
                    items(8) { index ->
                        val songTitle = "노래 제목 $index"
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_album),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clickable {
                                        musicViewModel.updateCurrentSongTitle(songTitle)
                                    }
                            )
                            Text(text = songTitle, fontSize = 12.sp)
                        }
                    }
                }
            }

            item {
                Divider()
                // 숏츠 섹션
                Text(
                    text = "숏츠",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
                    items(8) { index ->
                        val shortTitle = "숏츠 제목 $index"
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_album),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clickable {
                                        musicViewModel.updateCurrentSongTitle(shortTitle)
                                    }
                            )
                            Text(text = shortTitle, fontSize = 12.sp)
                        }
                    }
                }
            }

            item {
                Divider()
                // 뉴스 섹션
                Text(
                    text = "뉴스",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
                    items(8) { index ->
                        val newsTitle = "뉴스 제목 $index"
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_album),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clickable {
                                        musicViewModel.updateCurrentSongTitle(newsTitle)
                                    }
                            )
                            Text(text = newsTitle, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerScreen(navController: NavController, musicViewModel: MusicViewModel) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0f) }
    var duration by remember { mutableStateOf(0) }
    val currentSongTitle by musicViewModel.currentSongTitle

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
        Text(text = currentSongTitle, color = Color.White, fontSize = 16.sp)
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
            Spacer(modifier = Modifier.width(8.dp))
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
            Spacer(modifier = Modifier.width(8.dp))
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
            Spacer(modifier = Modifier.width(8.dp))
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
            "커스텀" to Pair(R.drawable.ic_custom, "custom"),
            "만보기" to Pair(R.drawable.ic_footstep, "pedometer"),
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
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // 배경을 검은색으로 설정
            .padding(16.dp)
    ) {
        // 입력 필드: 내용 입력
        val contentState = remember { mutableStateOf(TextFieldValue()) }
        Text(
            text = "가사",
            color = Color.White // 설명 텍스트 흰색
        )
        BasicTextField(
            value = contentState.value,
            onValueChange = { contentState.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp) // 텍스트 필드 높이를 크게 설정
                .background(Color(0xFFFFC0CB), shape = RoundedCornerShape(8.dp)) // 핑크 배경 및 모서리 둥글게 설정
                .padding(8.dp), // 내부 여백 설정
            textStyle = TextStyle(
                color = Color.White, // 입력 텍스트 색상 핑크
                fontSize = 18.sp // 텍스트 크기 증가
            ),
            cursorBrush = SolidColor(Color.White) // 커서 색상 흰색
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* TODO: Add create action */ },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White, // 버튼 배경색을 흰색으로 설정
                contentColor = Color.Black    // 버튼 텍스트 색상을 검은색으로 설정
            )
        ) {
            Text(text = "가사 생성")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 입력 필드: 키워드 입력
        val keywordState = remember { mutableStateOf(TextFieldValue()) }
        Text(
            text = "주제",
            color = Color.White
        )
        BasicTextField(
            value = keywordState.value,
            onValueChange = { keywordState.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color(0xFFFFC0CB), shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 18.sp
            ),
            cursorBrush = SolidColor(Color.White)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 입력 필드: 곡 분위기 입력
        val moodState = remember { mutableStateOf(TextFieldValue()) }
        Text(
            text = "스타일",
            color = Color.White
        )
        BasicTextField(
            value = moodState.value,
            onValueChange = { moodState.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color(0xFFFFC0CB), shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 18.sp
            ),
            cursorBrush = SolidColor(Color.White)
        )

        Spacer(modifier = Modifier.height(16.dp))

        val title = remember { mutableStateOf(TextFieldValue()) }
        Text(
            text = "제목",
            color = Color.White
        )
        BasicTextField(
            value = title.value,
            onValueChange = { title.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color(0xFFFFC0CB), shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 18.sp
            ),
            cursorBrush = SolidColor(Color.White)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 생성 버튼 추가
        Button(
            onClick = { /* TODO: Add create action */ },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White, // 버튼 배경색을 흰색으로 설정
                contentColor = Color.Black    // 버튼 텍스트 색상을 검은색으로 설정
            )
        ) {
            Text(text = "음악 생성")
        }
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

    }
}

@Composable
fun LibraryScreen(
    navController: NavController,
    playlists: MutableState<MutableMap<String, MutableList<String>>>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Artists Name",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        // Play & Shuffle Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { /* TODO: Add play all action */ },
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

        // Button to add a new playlist
        Button(onClick = { navController.navigate("create_playlist") }) {
            Text(text = "플레이리스트 추가")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display each playlist including default ones
        playlists.value.forEach { (playlistName, songs) ->
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Navigate to the playlist details
                            navController.navigate("playlist/$playlistName")
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Placeholder for playlist image
                    Image(
                        painter = painterResource(id = R.drawable.ic_album), // Use ic_album for all default lists
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    // Display playlist name
                    Text(
                        text = playlistName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Add a visual divider between playlists
                Divider(
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}


@Composable
fun CreatePlaylistScreen(
    navController: NavController, playlists: MutableState<MutableMap<String, MutableList<String>>>) {
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

    }
}


@Composable
fun StepCounter(navController: NavController) {
    val context = LocalContext.current
    val stepCount = remember { mutableStateOf(0) }

    val permissionGranted = remember { mutableStateOf(false) }
    val activityResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        permissionGranted.value = isGranted
    }

    LaunchedEffect(Unit) {
        val permissionCheck = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACTIVITY_RECOGNITION
        )
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            permissionGranted.value = true
        } else {
            activityResultLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }

    if (permissionGranted.value) {
        val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
        val stepCounterSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) }
        val initialStepCount = remember { mutableStateOf(-1) }

        if (stepCounterSensor == null) {
            Toast.makeText(context, "걸음 수 센서를 지원하지 않는 기기입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        DisposableEffect(Unit) {
            val stepListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
                        val steps = event.values[0].toInt()
                        if (initialStepCount.value == -1) {
                            initialStepCount.value = steps
                        }
                        stepCount.value = steps - initialStepCount.value
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sensorManager.registerListener(stepListener, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)

            onDispose {
                sensorManager.unregisterListener(stepListener)
            }
        }

        // 칼로리 소모량과 이산화탄소 절감량 계산
        val caloriesBurned = stepCount.value / 20.0  // 20걸음당 1kcal 소모
        val co2SavedToday = stepCount.value * 0.04  // 1000걸음당 0.04kg CO2 절감
        val co2SavedTotal = (stepCount.value * 0.04) // 누적 CO2 절감량 가정

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(16.dp)
        ) {
            // 상단 바 (왼쪽에 고정된 아이콘, 텍스트, 그리고 오른쪽에 유저 아이콘)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 왼쪽: Walk 아이콘과 WalkPL 텍스트
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_footstep), // 상단 고정 아이콘 리소스 설정
                        contentDescription = "Top Icon",
                        tint = Color(0xFFFFC0CB),
                        modifier = Modifier.size(30.dp) // 아이콘 크기 조절
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // 아이콘과 텍스트 간격
                    Text(
                        text = "WalkPL",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                }
                // 오른쪽: 유저 아이콘
                IconButton(onClick = { /* TODO: 사용자 프로필 액션 */ }) {
                    Icon(Icons.Default.Person, contentDescription = "Profile")
                }
            }
            // 중앙에서 왼쪽으로 이동하며 "오늘 걸음 수"와 계산된 데이터 표시
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = (-70).dp, y = (-50).dp), // 위치 조정
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "오늘 걸음 수",
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 26.sp,
                    color = Color(0xFFFFC0CB)
                )
                Spacer(modifier = Modifier.height(1.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_footstep),
                        contentDescription = "Walk_icon",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${stepCount.value}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontSize = 26.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "하루 동안 지킨 CO2",
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 26.sp,
                    color = Color(0xFFFFC0CB)
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "${"%.2f".format(co2SavedToday)} g",
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 26.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "오늘까지 지킨 CO2",
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 26.sp,
                    color = Color(0xFFFFC0CB)
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "${"%.2f".format(co2SavedTotal)} g",
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 26.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "칼로리 소모량",
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 26.sp,
                    color = Color(0xFFFFC0CB)
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "${"%.2f".format(caloriesBurned)} kcal",
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 26.sp,
                    color = Color.White
                )
            }
        }
    } else {
        Text("걸음 수 추적 권한이 필요합니다.", color = Color.White)
    }
}
