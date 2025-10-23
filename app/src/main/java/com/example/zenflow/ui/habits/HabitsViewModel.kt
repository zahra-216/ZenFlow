package com.example.zenflow.ui.habits

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.zenflow.data.Habit
import com.example.zenflow.data.HabitSharedPreferencesManager
import com.example.zenflow.util.HabitHelper
import java.util.Calendar

// AndroidViewModel is used because we need the Application Context for SharedPreferences
class HabitsViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefsManager = HabitSharedPreferencesManager(application)

    // LiveData to hold and expose the list of habits to the Fragment
    private val _habits = MutableLiveData<MutableList<Habit>>()
    val habits: LiveData<MutableList<Habit>> = _habits

    init {
        // 1. Load data when the ViewModel is first initialized
        val loadedHabits = sharedPrefsManager.loadHabits()

        // 2. Crucial: Check and update the completion status and streaks immediately
        _habits.value = checkHabitCompletionStatus(loadedHabits)
    }

    // --- Date and Time Helper Functions ---

    /** Checks if two timestamps fall on the same calendar day (Year and DayOfYear). */
    private fun isSameDay(time1: Long, time2: Long): Boolean {
        // If the habit has never been completed, it can't be today
        if (time1 == 0L || time2 == 0L) return false

        val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    /** Checks if the day has changed, resetting completion status and checking for broken streaks. */
    private fun checkHabitCompletionStatus(habits: MutableList<Habit>): MutableList<Habit> {
        val today = System.currentTimeMillis()

        habits.forEach { habit ->
            // If the last completion was NOT today (i.e., the user opened the app on a new day)
            if (!isSameDay(habit.lastCompletedDate, today)) {

                // 1. Daily Reset: Reset the checkmark for today
                habit.isCompletedToday = false

                // 2. Streak Check: Did the user miss yesterday?
                val yesterday = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -1)
                    // Clear time components to check against date only
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                // If last completion was NOT yesterday AND the streak count is > 0, the streak is broken
                if (habit.lastCompletedDate != 0L && !isSameDay(habit.lastCompletedDate, yesterday)) {
                    // Only break the streak if it wasn't already 0
                    if (habit.streakCount > 0) {
                        habit.streakCount = 0
                    }
                }
            }
        }
        // Save the list with the updated (reset) statuses
        sharedPrefsManager.saveHabits(habits)
        return habits
    }

    // --- CRUD and Toggle Operations ---

    fun addHabit(habit: Habit) {
        val currentList = _habits.value ?: mutableListOf()
        currentList.add(habit)
        _habits.value = currentList
        sharedPrefsManager.saveHabits(currentList)
    }

    fun updateHabit(updatedHabit: Habit) {
        val currentList = _habits.value ?: mutableListOf()
        val index = currentList.indexOfFirst { it.id == updatedHabit.id }
        if (index != -1) {
            currentList[index] = updatedHabit
            _habits.value = currentList
            sharedPrefsManager.saveHabits(currentList)
        }
    }

    fun deleteHabit(habit: Habit) {
        val currentList = _habits.value ?: mutableListOf()
        currentList.remove(habit)
        _habits.value = currentList
        sharedPrefsManager.saveHabits(currentList)
    }

    fun toggleHabitCompletion(habit: Habit) {
        val currentList = _habits.value ?: mutableListOf()
        val index = currentList.indexOfFirst { it.id == habit.id }

        if (index != -1) {
            val isTogglingToComplete = !currentList[index].isCompletedToday
            val updatedHabit = HabitHelper.updateHabitCompletion(currentList[index], isTogglingToComplete)
            currentList[index] = updatedHabit
            _habits.value = currentList
            sharedPrefsManager.saveHabits(currentList)
        }
    }
}