package com.example.zenflow.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HabitSharedPreferencesManager(context: Context) {

    private val sharedPrefs = context.getSharedPreferences("ZenFlowHabits", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val HABIT_KEY = "habit_list"

    // Load the list of habits from SharedPreferences
    fun loadHabits(): MutableList<Habit> {
        val json = sharedPrefs.getString(HABIT_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Habit>>() {}.type
            gson.fromJson(json, type) ?: mutableListOf()
        } else {
            mutableListOf()
        }
    }

    // Save the current list of habits back to SharedPreferences
    fun saveHabits(habits: List<Habit>) {
        val editor = sharedPrefs.edit()
        val json = gson.toJson(habits)
        editor.putString(HABIT_KEY, json)
        editor.apply() // Use apply for asynchronous save
    }
}