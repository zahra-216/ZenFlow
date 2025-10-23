package com.example.zenflow.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Habit(
    val id: Long = System.currentTimeMillis(),
    var name: String,
    var iconResId: Int, // Drawable resource ID
    var streakCount: Int = 0,
    var isCompletedToday: Boolean = false,
    var dateCreated: Long = System.currentTimeMillis(),
    var lastCompletedDate: Long = 0L, // Timestamp of the last day this habit was successfully completed

    // Track daily completion history
    // Key format: "2025-10-19", Value: true (completed) or false (not completed)
    var completionHistory: MutableMap<String, Boolean> = mutableMapOf()
) : Parcelable