package eu.tutorials.mywishlistapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import eu.tutorials.mywishlistapp.data.Wish
import eu.tutorials.mywishlistapp.data.Priority
import kotlinx.coroutines.launch

@Composable
fun AddEditDetailView(
    id: Long,
    viewModel: WishViewModel,
    navController: NavController
) {
    val snackMessage = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    // Modern renk paleti - HomeView ile aynı
    val primaryPurple = Color(0xFF6C63FF)
    val lightPurple = Color(0xFF9C88FF)
    val darkBackground = Color(0xFF1A1A2E)
    val cardBackground = Color(0xFF16213E)
    val inputBackground = Color(0xFF0F3460)

    // Local state'ler - bu şekilde priority değişimi diğer alanları etkilemez
    var titleState by remember { mutableStateOf("") }
    var descriptionState by remember { mutableStateOf("") }
    var priorityState by remember { mutableStateOf(Priority.LOW) }

    // Sadece ilk yüklemede wish'i yükle
    LaunchedEffect(id) {
        if (id != 0L) {
            viewModel.getAWishById(id).collect { wish ->
                titleState = wish.title
                descriptionState = wish.description
                priorityState = wish.getPriorityEnum()
                return@collect // Bir kez yükle ve çık
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (id != 0L) "✏️ Wish Düzenle" else "➕ Yeni Wish",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                backgroundColor = primaryPurple,
                elevation = 8.dp,
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            tint = Color.White,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        scaffoldState = scaffoldState,
        backgroundColor = darkBackground
    ) { paddingValues ->
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
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Title Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = cardBackground
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "📝 Başlık",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        ModernTextField(
                            value = titleState,
                            onValueChanged = { titleState = it },
                            placeholder = "Wish başlığını girin...",
                            primaryColor = primaryPurple,
                            inputBackground = inputBackground
                        )
                    }
                }

                // Description Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = cardBackground
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "📄 Açıklama",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        ModernTextField(
                            value = descriptionState,
                            onValueChanged = { descriptionState = it },
                            placeholder = "Wish açıklamasını girin...",
                            primaryColor = primaryPurple,
                            inputBackground = inputBackground,
                            singleLine = false
                        )
                    }
                }

                // Priority Selection Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = cardBackground
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "⭐ Öncelik",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(Priority.LOW, Priority.MEDIUM, Priority.HIGH, Priority.URGENT).forEach { priority ->
                                PriorityChip(
                                    priority = priority,
                                    isSelected = priorityState == priority,
                                    onSelected = { selectedPriority ->
                                        priorityState = selectedPriority
                                    },
                                    modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Button
                Button(
                    onClick = {
                        if (titleState.isNotEmpty()) {
                            if (id != 0L) {
                                viewModel.updateWish(
                                    Wish(
                                        id = id,
                                        title = titleState.trim(),
                                        description = descriptionState.trim(),
                                        priority = priorityState.level
                                    )
                                )
                                snackMessage.value = "✅ Wish güncellendi!"
                            } else {
                                viewModel.addWish(
                                    Wish(
                                        title = titleState.trim(),
                                        description = descriptionState.trim(),
                                        priority = priorityState.level
                                    )
                                )
                                snackMessage.value = "✅ Wish eklendi!"
                            }
                        } else {
                            snackMessage.value = "⚠️ Lütfen başlık girin!"
                        }

                        scope.launch {
                            scaffoldState.snackbarHostState.showSnackbar(snackMessage.value)
                            navController.navigateUp()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = primaryPurple,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.elevation(8.dp)
                ) {
                    Text(
                        text = if (id != 0L) "✏️ Güncelle" else "➕ Ekle",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ModernTextField(
    value: String,
    onValueChanged: (String) -> Unit,
    placeholder: String,
    primaryColor: Color,
    inputBackground: Color,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChanged,
        placeholder = {
            Text(
                text = placeholder,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 16.sp
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = inputBackground,
                shape = RoundedCornerShape(12.dp)
            ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        singleLine = singleLine,
        maxLines = if (singleLine) 1 else 3,
        textStyle = TextStyle(
            color = Color.White,
            fontSize = 16.sp
        ),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = Color.White,
            backgroundColor = Color.Transparent,
            focusedBorderColor = primaryColor,
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
            cursorColor = primaryColor,
            placeholderColor = Color.White.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun PriorityChip(
    priority: Priority,
    isSelected: Boolean,
    onSelected: (Priority) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable {
                onSelected(priority)
            }
            .padding(2.dp),
        elevation = if (isSelected) 8.dp else 4.dp,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = if (isSelected) priority.color else priority.color.copy(alpha = 0.2f)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = priority.displayName,
                color = if (isSelected) Color.White else priority.color,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}