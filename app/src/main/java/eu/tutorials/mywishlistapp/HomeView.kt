package eu.tutorials.mywishlistapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Sort
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import eu.tutorials.mywishlistapp.data.Wish
import eu.tutorials.mywishlistapp.data.Priority
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeView(
    navController: NavController,
    viewModel: WishViewModel
) {
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    // State'ler
    var isListening by remember { mutableStateOf(false) }
    var lastVoiceResult by remember { mutableStateOf("") }

    // Modern renk paleti
    val primaryPurple = Color(0xFF6C63FF)
    val darkBackground = Color(0xFF1A1A2E)
    val cardBackground = Color(0xFF16213E)
    val cardAccent = Color(0xFF0F3460)

    // Mikrofon animasyonu
    val infiniteTransition = rememberInfiniteTransition(label = "mic_animation")
    val micScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ), label = "mic_scale"
    )

    // 1. Sesli komut başlatıcısı - GELİŞTİRİLMİŞ
    val speechInputLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            isListening = false

            // Detaylı hata ayıklama
            println("🎤 Speech Result Code: ${result.resultCode}")
            println("🎤 Result Data: ${result.data}")

            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val recognizedTexts = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    val confidence = result.data?.getFloatArrayExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES)

                    println("🎤 Recognized texts: $recognizedTexts")
                    println("🎤 Confidence scores: ${confidence?.toList()}")

                    if (!recognizedTexts.isNullOrEmpty()) {
                        val spokenText = recognizedTexts[0]
                        lastVoiceResult = spokenText

                        println("🎤 Spoken text: '$spokenText' (length: ${spokenText.length})")

                        if (spokenText.isNotBlank() && spokenText.length > 1) {
                            // Wish ekle
                            val cleanTitle = spokenText.trim()
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

                            viewModel.addWish(
                                Wish(
                                    title = cleanTitle,
                                    description = "🎤 Sesli komutla eklendi",
                                    priority = Priority.MEDIUM.level // Varsayılan olarak orta öncelik
                                )
                            )

                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(
                                    "✅ '$cleanTitle' eklendi!",
                                    duration = SnackbarDuration.Long
                                )
                            }
                        } else {
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(
                                    "⚠️ Çok kısa ses algılandı: '$spokenText'",
                                    duration = SnackbarDuration.Long
                                )
                            }
                        }
                    } else {
                        scope.launch {
                            scaffoldState.snackbarHostState.showSnackbar(
                                "❌ Hiç ses algılanamadı - Mikrofon çalışıyor mu?",
                                duration = SnackbarDuration.Long
                            )
                        }
                    }
                }
                Activity.RESULT_CANCELED -> {
                    scope.launch {
                        scaffoldState.snackbarHostState.showSnackbar(
                            "🚫 Sesli komut iptal edildi",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
                SpeechRecognizer.ERROR_AUDIO -> {
                    scope.launch {
                        scaffoldState.snackbarHostState.showSnackbar(
                            "🎤 Mikrofon hatası - Cihazı kontrol edin",
                            duration = SnackbarDuration.Long
                        )
                    }
                }
                SpeechRecognizer.ERROR_NO_MATCH -> {
                    scope.launch {
                        scaffoldState.snackbarHostState.showSnackbar(
                            "👂 Konuşma algılanamadı - Daha yüksek konuşun",
                            duration = SnackbarDuration.Long
                        )
                    }
                }
                SpeechRecognizer.ERROR_NETWORK -> {
                    scope.launch {
                        scaffoldState.snackbarHostState.showSnackbar(
                            "🌐 İnternet bağlantısı gerekli",
                            duration = SnackbarDuration.Long
                        )
                    }
                }
                else -> {
                    scope.launch {
                        scaffoldState.snackbarHostState.showSnackbar(
                            "❌ Sesli komut hatası (Kod: ${result.resultCode})",
                            duration = SnackbarDuration.Long
                        )
                    }
                }
            }
        }
    )

    // 2. İzin isteği başlatıcısı - GELİŞTİRİLMİŞ
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                println("🎤 Mikrofon izni verildi")
                startSpeechRecognition(speechInputLauncher, context) {
                    isListening = true
                }
            } else {
                println("🎤 Mikrofon izni reddedildi")
                Toast.makeText(context, "🎤 Mikrofon izni gerekli! Ayarlardan izin verin.", Toast.LENGTH_LONG).show()
            }
        }
    )

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🌟 WishList",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                backgroundColor = primaryPurple,
                elevation = 8.dp,
                actions = {
                    // Sıralama butonu
                    IconButton(
                        onClick = { viewModel.toggleSortByPriority() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = if (viewModel.sortByPriority)
                                "Öncelik Sıralaması" else "Normal Sıralama",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                // Sesli Komut butonu - GELİŞTİRİLMİŞ
                FloatingActionButton(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .scale(micScale),
                    contentColor = Color.White,
                    backgroundColor = if (isListening) Color(0xFFE53E3E) else Color(0xFF38A169),
                    elevation = FloatingActionButtonDefaults.elevation(8.dp),
                    onClick = {
                        if (!isListening) {
                            println("🎤 Mikrofon butonuna tıklandı")

                            // İzin kontrolü - DÜZELTME
                            when (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)) {
                                PackageManager.PERMISSION_GRANTED -> {
                                    println("🎤 İzin mevcut, speech recognizer kontrol ediliyor")
                                    // Speech recognizer mevcut mu kontrol et
                                    if (SpeechRecognizer.isRecognitionAvailable(context)) {
                                        println("🎤 Speech recognizer mevcut, başlatılıyor")
                                        startSpeechRecognition(speechInputLauncher, context) {
                                            isListening = true
                                        }
                                    } else {
                                        println("🎤 Speech recognizer mevcut değil")
                                        Toast.makeText(context, "⚠️ Konuşma tanıma bu cihazda desteklenmiyor", Toast.LENGTH_LONG).show()
                                    }
                                }
                                else -> {
                                    println("🎤 İzin yok, izin isteniyor")
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        } else {
                            println("🎤 Zaten dinliyor, durdurulmadı")
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = if (isListening) "Dinliyor..." else "Sesli Ekle",
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Normal Ekle butonu
                FloatingActionButton(
                    modifier = Modifier,
                    contentColor = Color.White,
                    backgroundColor = primaryPurple,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp),
                    onClick = {
                        navController.navigate(Screen.AddScreen.route + "/0L")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Manuel Ekle",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        backgroundColor = darkBackground
    ) { paddingValues ->
        val wishlist = viewModel.getAllWishes.collectAsState(initial = listOf())

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            darkBackground,
                            Color(0xFF16213E).copy(alpha = 0.8f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Dinleme durumu göstergesi
                if (isListening) {
                    ListeningIndicator()
                }

                // Son sesli komut göstergesi
                if (lastVoiceResult.isNotEmpty() && !isListening) {
                    LastVoiceCommandCard(lastVoiceResult)
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(wishlist.value, key = { wish -> wish.id }) { wish ->
                        val dismissState = rememberDismissState(
                            confirmStateChange = {
                                if (it == DismissValue.DismissedToEnd || it == DismissValue.DismissedToStart) {
                                    viewModel.deleteWish(wish)
                                }
                                true
                            }
                        )

                        SwipeToDismiss(
                            state = dismissState,
                            background = {
                                val color by animateColorAsState(
                                    targetValue = if (dismissState.dismissDirection == DismissDirection.EndToStart)
                                        Color(0xFFE53E3E).copy(alpha = 0.8f) else Color.Transparent,
                                    label = ""
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            color = color,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            },
                            dismissContent = {
                                WishItem(
                                    wish = wish,
                                    cardBackground = cardBackground,
                                    cardAccent = cardAccent,
                                    primaryPurple = primaryPurple
                                ) {
                                    navController.navigate(Screen.AddScreen.route + "/${wish.id}")
                                }
                            },
                            directions = setOf(DismissDirection.EndToStart)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ListeningIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "listening")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        backgroundColor = Color(0xFFE53E3E).copy(alpha = alpha),
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Listening",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = " Dinliyorum... Lütfen konuşun!",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun LastVoiceCommandCard(voiceResult: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        backgroundColor = Color(0xFF38A169).copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Son sesli komut:",
                color = Color(0xFF38A169),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "\"$voiceResult\"",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// Sesli komut başlatma fonksiyonu - GELİŞTİRİLMİŞ
fun startSpeechRecognition(
    launcher: androidx.activity.compose.ManagedActivityResultLauncher<Intent, androidx.activity.result.ActivityResult>,
    context: android.content.Context,
    onStart: () -> Unit
) {
    try {
        println("🎤 Speech recognition başlatılıyor...")

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            // Dil modeli
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)

            // Dil ayarları - Türkçe ve İngilizce
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "tr-TR")

            // Alternatif diller
            val languages = arrayListOf("tr-TR", "en-US", "tr")
            putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, languages)

            // Prompt mesajı
            putExtra(RecognizerIntent.EXTRA_PROMPT, "🎤 Wish'inizi söyleyin...")

            // Sonuç sayısı
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)

            // Kısmi sonuçlar
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

            // Güven skorları
            putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES, true)

            // Sessizlik timeout'u - ARTIRILMIŞ
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 2000)

            // Çevrimdışı tanıma (mümkünse)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)

            // Güvenli mod kapatma
            putExtra(RecognizerIntent.EXTRA_SECURE, false)

            // Web arama sonuçları
            putExtra(RecognizerIntent.EXTRA_WEB_SEARCH_ONLY, false)
        }

        // Intent'in çözülebilir olduğunu kontrol et
        val packageManager = context.packageManager
        val activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

        if (activities.isNotEmpty()) {
            println("🎤 Speech recognizer bulundu: ${activities.size} aktivite")
            onStart()
            launcher.launch(intent)
        } else {
            println("🎤 Hiç speech recognizer bulunamadı")
            Toast.makeText(context, "❌ Sesli komut uygulaması bulunamadı. Google uygulamasını güncelleyin.", Toast.LENGTH_LONG).show()
        }

    } catch (e: Exception) {
        println("🎤 Speech recognition hatası: ${e.message}")
        e.printStackTrace()
        Toast.makeText(context, "❌ Sesli komut başlatılamadı: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

@Composable
fun WishItem(
    wish: Wish,
    cardBackground: Color,
    cardAccent: Color,
    primaryPurple: Color,
    onClick: () -> Unit
) {
    val priority = wish.getPriorityEnum()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = 8.dp,
        shape = RoundedCornerShape(16.dp),
        backgroundColor = cardBackground
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            cardBackground,
                            cardAccent.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {
            // Priority renk şeridi
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(80.dp)
                    .background(
                        color = priority.color,
                        shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                    )
                    .align(Alignment.CenterStart)
            )

            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .padding(start = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = wish.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )

                    // Priority badge
                    Card(
                        backgroundColor = priority.color.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = priority.displayName,
                            color = priority.color,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = wish.description,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}