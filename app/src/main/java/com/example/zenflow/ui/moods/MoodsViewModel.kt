package com.example.zenflow.ui.moods

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.zenflow.data.Mood
import com.example.zenflow.data.MoodSharedPreferencesManager

class MoodsViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefsManager = MoodSharedPreferencesManager(application)

    private val _moods = MutableLiveData<MutableList<Mood>>()
    val moods: LiveData<MutableList<Mood>> = _moods

    private val _selectedMood = MutableLiveData<String?>()
    val selectedMood: LiveData<String?> = _selectedMood

    init {
        loadMoods()
    }

    fun loadMoods() {
        _moods.value = sharedPrefsManager.loadMoods()
    }

    fun selectMood(moodName: String) {
        _selectedMood.value = moodName
    }

    fun clearMoodSelection() {
        _selectedMood.value = null
    }

    fun saveMood(moodName: String, note: String) {
        val emoji = Mood.getMoodEmoji(moodName)
        val newMood = Mood(
            emoji = emoji,
            moodName = moodName,
            note = note
        )

        sharedPrefsManager.addMood(newMood)
        loadMoods()
        clearMoodSelection()
    }

    fun deleteMood(mood: Mood) {
        sharedPrefsManager.deleteMood(mood.id)
        loadMoods()
    }

    fun getMoodsForDate(dateInMillis: Long): List<Mood> {
        return sharedPrefsManager.getMoodsForDate(dateInMillis)
    }
}