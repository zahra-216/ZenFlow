package com.example.zenflow.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.zenflow.R
import com.example.zenflow.data.HabitSharedPreferencesManager
import com.example.zenflow.data.ProfileSharedPreferencesManager
import com.example.zenflow.databinding.FragmentHomeBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var profilePrefs: ProfileSharedPreferencesManager
    private lateinit var habitPrefs: HabitSharedPreferencesManager
    private var timeUpdateJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profilePrefs = ProfileSharedPreferencesManager(requireContext())
        habitPrefs = HabitSharedPreferencesManager(requireContext())

        setupGreeting()
        startTimeUpdates()
        loadHabitStats()
        loadMotivationalQuote()
        setupQuickActions()
    }

    override fun onResume() {
        super.onResume()
        // Refresh stats when returning to home fragment
        loadHabitStats()
    }

    private fun setupGreeting() {
        val user = profilePrefs.loadUser()
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val greeting = when (hour) {
            in 5..11 -> getString(R.string.good_morning)
            in 12..16 -> getString(R.string.good_afternoon)
            in 17..20 -> getString(R.string.good_evening)
            else -> getString(R.string.good_night)
        }

        binding.textGreeting.text = getString(R.string.greeting_with_name, greeting, user.name)
    }

    private fun startTimeUpdates() {
        timeUpdateJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                updateDateTime()
                delay(60000) // Update every minute
            }
        }
    }

    private fun updateDateTime() {
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val currentDate = Date()

        binding.textDate.text = dateFormat.format(currentDate)
        binding.textTime.text = timeFormat.format(currentDate)
    }

    private fun loadHabitStats() {
        val habits = habitPrefs.loadHabits()
        val totalHabits = habits.size
        val completedToday = habits.count { it.isCompletedToday }

        // Calculate progress percentage
        val percentage = if (totalHabits > 0) {
            (completedToday.toFloat() / totalHabits.toFloat() * 100).toInt()
        } else {
            0
        }

        // Update UI
        binding.progressCircular.progress = percentage
        binding.textProgressPercentage.text = getString(R.string.percentage_format, percentage)
        binding.textHabitsCompleted.text = getString(
            R.string.habits_completed_format,
            completedToday,
            totalHabits
        )

        // Current streak (longest active streak)
        val longestStreak = habits.maxOfOrNull { it.streakCount } ?: 0
        binding.textCurrentStreak.text = longestStreak.toString()

        // Completed today
        binding.textCompletedToday.text = completedToday.toString()

        // Weekly completion rate
        val weeklyRate = calculateWeeklyCompletionRate(habits)
        binding.textWeeklyRate.text = getString(R.string.percentage_format, weeklyRate)
    }

    private fun calculateWeeklyCompletionRate(habits: List<com.example.zenflow.data.Habit>): Int {
        if (habits.isEmpty()) return 0

        val calendar = Calendar.getInstance()
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysPassedThisWeek = if (currentDayOfWeek == Calendar.SUNDAY) 7 else currentDayOfWeek - 1

        // Simple calculation: average of current streaks as a percentage
        val avgStreak = habits.map { it.streakCount }.average()
        val rate = ((avgStreak / 7.0) * 100).toInt().coerceIn(0, 100)

        return rate
    }

    private fun loadMotivationalQuote() {
        val prefs = requireContext().getSharedPreferences("home_prefs", android.content.Context.MODE_PRIVATE)
        val lastQuoteTime = prefs.getLong("last_quote_time", 0)
        val lastQuoteIndex = prefs.getInt("last_quote_index", -1)
        val currentTime = System.currentTimeMillis()

        // Check if 12 hours have passed
        val twelveHours = 12 * 60 * 60 * 1000
        if (currentTime - lastQuoteTime > twelveHours || lastQuoteIndex == -1) {
            // Get new quote
            val quotes = resources.getStringArray(R.array.motivational_quotes)
            var newIndex = Random.nextInt(quotes.size)

            // Ensure we don't show the same quote twice in a row
            while (newIndex == lastQuoteIndex && quotes.size > 1) {
                newIndex = Random.nextInt(quotes.size)
            }

            binding.textMotivationalQuote.text = quotes[newIndex]

            // Save to preferences
            prefs.edit().apply {
                putLong("last_quote_time", currentTime)
                putInt("last_quote_index", newIndex)
                apply()
            }
        } else {
            // Use the existing quote
            val quotes = resources.getStringArray(R.array.motivational_quotes)
            if (lastQuoteIndex < quotes.size) {
                binding.textMotivationalQuote.text = quotes[lastQuoteIndex]
            }
        }
    }

    private fun setupQuickActions() {
        binding.btnViewHabits.setOnClickListener {
            val activity = requireActivity() as? com.example.zenflow.MainActivity
            activity?.navigateToHabitsTab()
        }

        binding.btnAddHabit.setOnClickListener {
            val activity = requireActivity() as? com.example.zenflow.MainActivity
            activity?.navigateToHabitsTab()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timeUpdateJob?.cancel()
        _binding = null
    }
}