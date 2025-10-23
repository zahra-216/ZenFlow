package com.example.zenflow.util

import com.example.zenflow.R

// A simplified map of all available habit icons
object HabitIcon {
    val ICONS = mapOf(
        "Water" to R.drawable.ic_water_drop,
        "Running" to R.drawable.ic_running_person,
        "Meditation" to R.drawable.ic_meditation_person,
        "Books" to R.drawable.ic_books,
        "Salad" to R.drawable.ic_salad_bowl,
        "Sleep" to R.drawable.ic_sleeping_face,
        "Strength" to R.drawable.ic_muscle_arm,
        "Target" to R.drawable.ic_target_dart,
        "Brain" to R.drawable.ic_brain,
        "Heart" to R.drawable.ic_heart
    )

    // Default icon to use if none is selected
    val DEFAULT_ICON = R.drawable.ic_meditation_person

    // Function to get a random one for quick testing
    fun getRandomIcon(): Int {
        return ICONS.values.random()
    }
}