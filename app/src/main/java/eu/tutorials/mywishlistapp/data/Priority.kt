package eu.tutorials.mywishlistapp.data


import androidx.compose.ui.graphics.Color

enum class Priority(
    val displayName: String,
    val color: Color,
    val level: Int
) {
    LOW("Düşük", Color(0xFF4CAF50), 1),
    MEDIUM("Orta", Color(0xFFFF9800), 2),
    HIGH("Yüksek", Color(0xFFF44336), 3),
    URGENT("Acil", Color(0xFF9C27B0), 4);

    companion object {
        fun fromLevel(level: Int): Priority {
            return values().find { it.level == level } ?: LOW
        }
    }
}
