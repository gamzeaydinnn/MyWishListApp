package eu.tutorials.mywishlistapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="wish-table")
data class Wish(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name="wish-title")
    val title: String="",
    @ColumnInfo(name="wish-desc")
    val description:String="",
    @ColumnInfo(name="wish-priority")
    val priority: Int = Priority.LOW.level // Priority enum'ının level değeri
) {
    // Priority enum'ına dönüştürme helper'ı
    fun getPriorityEnum(): Priority = Priority.fromLevel(priority)
}

object DummyWish{
    val wishList = listOf(
        Wish(title="Google Watch 2",
            description =  "An android Watch",
            priority = Priority.HIGH.level),
        Wish(title = "Oculus Quest 2",
            description = "A VR headset for playing games",
            priority = Priority.URGENT.level),
        Wish(title = "A Sci-fi, Book",
            description= "A science friction book from any best seller",
            priority = Priority.MEDIUM.level),
        Wish(title = "Bean bag",
            description = "A comfy bean bag to substitute for a chair",
            priority = Priority.LOW.level)
    )
}
