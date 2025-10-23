package com.example.zenflow.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MoodSharedPreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "mood_preferences"
        private const val KEY_MOODS = "moods_list"
    }

    fun saveMoods(moods: List<Mood>) {
        val json = gson.toJson(moods)
        sharedPreferences.edit().putString(KEY_MOODS, json).apply()
    }

    fun loadMoods(): MutableList<Mood> {
        val json = sharedPreferences.getString(KEY_MOODS, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Mood>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    fun addMood(mood: Mood) {
        val moods = loadMoods()
        moods.add(0, mood) // Add at the beginning for chronological order
        saveMoods(moods)
    }

    fun deleteMood(moodId: Long) {
        val moods = loadMoods()
        moods.removeAll { it.id == moodId }
        saveMoods(moods)
    }

    fun getMoodsForDate(dateInMillis: Long): List<Mood> {
        val moods = loadMoods()
        val startOfDay = getStartOfDay(dateInMillis)
        val endOfDay = getEndOfDay(dateInMillis)

        return moods.filter { it.timestamp in startOfDay..endOfDay }
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfDay(timestamp: Long): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
        calendar.set(java.util.Calendar.MINUTE, 59)
        calendar.set(java.util.Calendar.SECOND, 59)
        calendar.set(java.util.Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}