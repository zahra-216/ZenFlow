package com.example.zenflow.ui.moods

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.zenflow.R
import com.example.zenflow.data.Mood
import com.example.zenflow.data.MoodSharedPreferencesManager
import com.example.zenflow.databinding.ActivityMoodChartBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.graphics.toColorInt

class MoodChartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoodChartBinding
    private lateinit var moodPrefsManager: MoodSharedPreferencesManager
    private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        moodPrefsManager = MoodSharedPreferencesManager(this)

        setupToolbar()
        setupChart()
        loadMoodData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.mood_chart)
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupChart() {
        val textColor = if (isDarkMode()) {
            ContextCompat.getColor(this, R.color.zen_dark_text)
        } else {
            ContextCompat.getColor(this, R.color.zen_light_text)
        }

        binding.lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)
            setDrawGridBackground(false)
            legend.isEnabled = true
            legend.textColor = textColor
            setExtraOffsets(0f, 20f, 0f, 10f) // Add padding to prevent cutoff

            // X-Axis
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                this.textColor = textColor
                textSize = 10f
            }

            // Left Y-Axis
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = if (isDarkMode()) {
                    "#444444".toColorInt()
                } else {
                    Color.LTGRAY
                }
                this.textColor = textColor
                axisMinimum = 0f
                axisMaximum = 6.5f // Increased to prevent cutoff
                granularity = 1f
                textSize = 12f
            }

            // Right Y-Axis
            axisRight.isEnabled = false
        }
    }

    private fun isDarkMode(): Boolean {
        return (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    private fun loadMoodData() {
        val allMoods = moodPrefsManager.loadMoods()
        val last7Days = getLast7Days()

        val moodsByDate = last7Days.associateWith { date ->
            allMoods.filter { mood ->
                isSameDay(mood.timestamp, date)
            }
        }

        val entries = mutableListOf<Entry>()
        val dateLabels = mutableListOf<String>()

        last7Days.forEachIndexed { index, date ->
            val dayMoods = moodsByDate[date] ?: emptyList()
            if (dayMoods.isNotEmpty()) {
                val avgScore = dayMoods.map { getMoodScore(it.moodName) }.average().toFloat()
                entries.add(Entry(index.toFloat(), avgScore))
            } else {
                entries.add(Entry(index.toFloat(), 0f))
            }
            dateLabels.add(dateFormat.format(Date(date)))
        }

        updateSummary(allMoods, last7Days)

        // Determine colors based on theme
        val isDark = isDarkMode()
        val chartLineColor = ContextCompat.getColor(this, R.color.zen_primary)
        val chartFillColor = ContextCompat.getColor(this, R.color.zen_accent_green)
        val chartValueTextColor = if (isDark) {
            ContextCompat.getColor(this, R.color.zen_dark_text)
        } else {
            ContextCompat.getColor(this, R.color.zen_light_text)
        }

        val dataSet = LineDataSet(entries, getString(R.string.mood_trend)).apply {
            color = chartLineColor
            setCircleColor(chartLineColor)
            lineWidth = 2f
            circleRadius = 4f
            setDrawCircleHole(false)
            valueTextSize = 10f
            valueTextColor = chartValueTextColor
            setDrawFilled(true)
            fillColor = chartFillColor
            fillAlpha = 50
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        val lineData = LineData(dataSet)
        binding.lineChart.data = lineData

        binding.lineChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value.toInt() in dateLabels.indices) {
                    dateLabels[value.toInt()]
                } else ""
            }
        }

        binding.lineChart.axisLeft.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return when (value.toInt()) {
                    1 -> "😢"
                    2 -> "😰"
                    3 -> "😡"
                    4 -> "😴"
                    5 -> "😌"
                    6 -> "😊"
                    else -> ""
                }
            }
        }

        binding.lineChart.invalidate()
    }

    private fun updateSummary(allMoods: List<Mood>, last7Days: List<Long>) {
        val recentMoods = allMoods.filter { mood ->
            last7Days.any { date -> isSameDay(mood.timestamp, date) }
        }

        val totalEntries = recentMoods.size
        val moodCounts = recentMoods.groupingBy { it.moodName }.eachCount()
        val mostFrequentMood = moodCounts.maxByOrNull { it.value }?.key ?: "N/A"

        binding.textTotalEntries.text = getString(R.string.total_entries_format, totalEntries)
        binding.textMostFrequentMood.text = getString(R.string.most_frequent_mood_format,
            mostFrequentMood, Mood.getMoodEmoji(mostFrequentMood))
    }

    private fun getMoodScore(moodName: String): Int {
        return when (moodName) {
            "Sad" -> 1
            "Anxious" -> 2
            "Angry" -> 3
            "Tired" -> 4
            "Calm" -> 5
            "Happy" -> 6
            else -> 0
        }
    }

    private fun getLast7Days(): List<Long> {
        val calendar = Calendar.getInstance()
        val days = mutableListOf<Long>()

        for (i in 6 downTo 0) {
            calendar.apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            days.add(calendar.timeInMillis)
        }
        return days
    }

    private fun isSameDay(time1: Long, time2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}