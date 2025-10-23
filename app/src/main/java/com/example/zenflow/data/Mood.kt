package com.example.zenflow.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Mood(
    val id: Long = System.currentTimeMillis(),
    val emoji: String,
    val moodName: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable {

    companion object {
        const val MOOD_HAPPY = "😊"
        const val MOOD_CALM = "😌"
        const val MOOD_SAD = "😢"
        const val MOOD_ANXIOUS = "😰"
        const val MOOD_ANGRY = "😡"
        const val MOOD_TIRED = "😴"

        fun getMoodEmoji(moodName: String): String {
            return when (moodName) {
                "Happy" -> MOOD_HAPPY
                "Calm" -> MOOD_CALM
                "Sad" -> MOOD_SAD
                "Anxious" -> MOOD_ANXIOUS
                "Angry" -> MOOD_ANGRY
                "Tired" -> MOOD_TIRED
                else -> MOOD_HAPPY
            }
        }
    }
}