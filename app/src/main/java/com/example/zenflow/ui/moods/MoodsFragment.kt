package com.example.zenflow.ui.moods

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zenflow.R
import com.example.zenflow.data.Mood
import com.example.zenflow.databinding.FragmentMoodsBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class MoodsFragment : Fragment(), MoodActionListener {

    private var _binding: FragmentMoodsBinding? = null
    private val binding get() = _binding!!

    private val moodViewModel: MoodsViewModel by viewModels()
    private lateinit var moodAdapter: MoodAdapter

    private var isCalendarViewVisible = false
    private val dateFormat = SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault())
    private val dateOnlyFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

    // Map to hold mood card views
    private val moodCards = mutableMapOf<String, MaterialCardView>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMoodSelectors()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        setupCalendarView()
    }

    private fun setupMoodSelectors() {
        // Map mood names to their card views
        moodCards["Happy"] = binding.moodHappy
        moodCards["Calm"] = binding.moodCalm
        moodCards["Sad"] = binding.moodSad
        moodCards["Anxious"] = binding.moodAnxious
        moodCards["Angry"] = binding.moodAngry
        moodCards["Tired"] = binding.moodTired

        // Set click listeners for each mood
        moodCards.forEach { (moodName, card) ->
            card.setOnClickListener {
                moodViewModel.selectMood(moodName)
            }
        }
    }

    private fun setupRecyclerView() {
        moodAdapter = MoodAdapter(this)
        binding.recyclerPastMoods.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = moodAdapter
        }
    }

    private fun setupObservers() {
        // Observe selected mood
        moodViewModel.selectedMood.observe(viewLifecycleOwner) { selectedMood ->
            updateMoodSelection(selectedMood)
            binding.btnSaveMood.isEnabled = selectedMood != null
        }

        // Observe moods list
        moodViewModel.moods.observe(viewLifecycleOwner) { moods ->
            if (moods.isEmpty()) {
                binding.recyclerPastMoods.visibility = View.GONE
                binding.textEmptyState.visibility = View.VISIBLE
            } else {
                binding.recyclerPastMoods.visibility = View.VISIBLE
                binding.textEmptyState.visibility = View.GONE
                moodAdapter.submitList(moods)
            }
        }
    }

    private fun updateMoodSelection(selectedMood: String?) {
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.zen_primary)
        val transparentColor = android.graphics.Color.TRANSPARENT

        moodCards.forEach { (moodName, card) ->
            if (moodName == selectedMood) {
                card.strokeColor = primaryColor
                card.strokeWidth = 8
            } else {
                card.strokeColor = transparentColor
                card.strokeWidth = 0
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSaveMood.setOnClickListener {
            saveMoodEntry()
        }

        binding.btnToggleView.setOnClickListener {
            toggleView()
        }

        binding.btnShareSummary.setOnClickListener {
            shareMoodSummary()
        }
    }

    private fun setupCalendarView() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val selectedDate = calendar.timeInMillis

            val moodsForDate = moodViewModel.getMoodsForDate(selectedDate)
            displayMoodsForDate(moodsForDate, selectedDate)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayMoodsForDate(moods: List<Mood>, dateInMillis: Long) {
        val dateStr = dateOnlyFormat.format(Date(dateInMillis))

        if (moods.isEmpty()) {
            binding.textSelectedDateMoods.text = getString(R.string.no_moods_for_date, dateStr)
        } else {
            val moodsSummary = moods.joinToString(", ") { "${it.emoji} ${it.moodName}" }
            binding.textSelectedDateMoods.text = "$dateStr:\n$moodsSummary"
        }
    }

    private fun saveMoodEntry() {
        val selectedMood = moodViewModel.selectedMood.value
        if (selectedMood == null) {
            Toast.makeText(context, R.string.select_mood_prompt, Toast.LENGTH_SHORT).show()
            return
        }

        val note = binding.editMoodNote.text.toString().trim()
        moodViewModel.saveMood(selectedMood, note)

        // Clear input
        binding.editMoodNote.text?.clear()

        Toast.makeText(context, R.string.mood_saved_success, Toast.LENGTH_SHORT).show()
    }

    private fun toggleView() {
        isCalendarViewVisible = !isCalendarViewVisible

        if (isCalendarViewVisible) {
            binding.calendarContainer.visibility = View.VISIBLE
            binding.recyclerPastMoods.visibility = View.GONE
            binding.btnToggleView.text = getString(R.string.list)
            binding.btnToggleView.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_list_24, 0, 0, 0
            )
        } else {
            binding.calendarContainer.visibility = View.GONE
            binding.recyclerPastMoods.visibility = View.VISIBLE
            binding.btnToggleView.text = getString(R.string.calendar)
            binding.btnToggleView.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_calendar_24, 0, 0, 0
            )
        }
    }

    private fun shareMoodSummary() {
        val moods = moodViewModel.moods.value
        if (moods.isNullOrEmpty()) {
            Toast.makeText(context, R.string.no_moods_to_share, Toast.LENGTH_SHORT).show()
            return
        }

        val summary = buildString {
            appendLine("📊 My Mood Journal Summary")
            appendLine()
            appendLine("Total entries: ${moods.size}")
            appendLine()
            moods.take(5).forEach { mood ->
                appendLine("${mood.emoji} ${mood.moodName} - ${dateFormat.format(Date(mood.timestamp))}")
                if (mood.note.isNotEmpty()) {
                    appendLine("   \"${mood.note}\"")
                }
                appendLine()
            }
            if (moods.size > 5) {
                appendLine("... and ${moods.size - 5} more entries")
            }
        }

        shareText(summary)
    }

    override fun onShareMood(mood: Mood) {
        val message = buildString {
            appendLine("${mood.emoji} ${mood.moodName}")
            appendLine()
            appendLine("Date: ${dateFormat.format(Date(mood.timestamp))}")
            if (mood.note.isNotEmpty()) {
                appendLine()
                appendLine("Note: ${mood.note}")
            }
        }

        shareText(message)
    }

    override fun onDeleteMood(mood: Mood) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_mood_title)
            .setMessage(R.string.delete_mood_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                moodViewModel.deleteMood(mood)
                Toast.makeText(context, R.string.mood_deleted, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun shareText(text: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}