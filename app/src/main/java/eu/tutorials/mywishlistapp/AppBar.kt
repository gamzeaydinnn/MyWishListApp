package eu.tutorials.mywishlistapp

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBarView(
    title: String,
    onBackNavClicked: (() -> Unit)? = null // Opsiyonel hale getirdik
) {
    // Modern renk paleti - diğer ekranlarla uyumlu
    val primaryPurple = Color(0xFF6C63FF)

    val navigationIcon: @Composable (() -> Unit) = {
        if (onBackNavClicked != null) {
            IconButton(onClick = {
                onBackNavClicked()
            }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    tint = Color.White,
                    contentDescription = "Back"
                )
            }
        }
    }

    TopAppBar(
        title = {
            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .heightIn(max = 24.dp)
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = primaryPurple
        ),
        navigationIcon = navigationIcon
    )
}