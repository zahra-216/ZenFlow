package com.example.zenflow.util

import com.example.zenflow.data.Habit
import java.text.SimpleDateFormat
import java.util.*

object HabitHelper {

    /**
     * Updates habit completion for today and tracks in history
     * Call this when user marks/unmarks a habit as complete
     */
    fun updateHabitCompletion(habit: Habit, isCompleted: Boolean): Habit {
        val today = getTodayDateKey()

        // Create a mutable copy of completion history, or initialize if null
        val updatedHistory = (habit.completionHistory ?: mutableMapOf()).toMutableMap()
        updatedHistory[today] = isCompleted

        // Calculate the new streak based on completion history
        val newStreak = if (isCompleted) calculateStreak(updatedHistory) else 0

        return habit.copy(
            isCompletedToday = isCompleted,
            completionHistory = updatedHistory,
            lastCompletedDate = if (isCompleted) System.currentTimeMillis() else habit.lastCompletedDate,
            streakCount = newStreak
        )
    }

    /**
     * Calculates current streak by counting consecutive days completed backwards from today
     */
    private fun calculateStreak(completionHistory: MutableMap<String, Boolean>): Int {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        var streak = 0

        // Start from today and go backwards
        while (true) {
            val dateKey = dateFormat.format(calendar.time)

            // If this day is marked as completed, increment streak
            if (completionHistory[dateKey] == true) {
                streak++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                // Stop counting when we hit a day that wasn't completed
                break
            }
        }

        return streak
    }

    /**
     * Gets today's date in format "yyyy-MM-dd"
     */
    fun getTodayDateKey(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    /**
     * Resets isCompletedToday for all habits at start of new day
     * Call this when app opens to check if it's a new day
     */
    fun resetDailyCompletion(habits: List<Habit>): List<Habit> {
        val today = getTodayDateKey()

        return habits.map { habit ->
            val lastCompletedKey = if (habit.lastCompletedDate > 0) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                dateFormat.format(Date(habit.lastCompletedDate))
            } else {
                ""
            }

            // If last completed date is not today, reset isCompletedToday
            if (lastCompletedKey != today) {
                habit.copy(isCompletedToday = false)
            } else {
                habit
            }
        }
    }
}