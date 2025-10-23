package com.example.zenflow.ui.habits

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zenflow.R
import com.example.zenflow.data.Habit
import com.example.zenflow.databinding.FragmentHabitsBinding

class HabitsFragment : Fragment(), HabitActionListener, HabitDialogListener {

    private var _binding: FragmentHabitsBinding? = null
    private val binding get() = _binding!!

    private val habitsViewModel: HabitsViewModel by viewModels()
    private lateinit var habitAdapter: HabitAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter(this)
        binding.recyclerHabits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = habitAdapter
        }
    }

    private fun setupObservers() {
        habitsViewModel.habits.observe(viewLifecycleOwner) { habits ->
            if (habits.isEmpty()) {
                binding.recyclerHabits.visibility = View.GONE
                binding.textEmptyState.visibility = View.VISIBLE
            } else {
                binding.recyclerHabits.visibility = View.VISIBLE
                binding.textEmptyState.visibility = View.GONE
                habitAdapter.submitList(habits)
            }
            updateSummary(habits)
        }
    }

    private fun setupClickListeners() {
        binding.fabAddHabit.setOnClickListener {
            showAddHabitDialog()
        }
    }

    private fun showAddHabitDialog() {
        val dialog = AddEditHabitDialogFragment.newInstance(null)
        dialog.setTargetFragment(this, 0)
        dialog.show(parentFragmentManager, AddEditHabitDialogFragment.TAG)
    }

    @SuppressLint("SetTextI18n")
    private fun updateSummary(habits: List<Habit>) {
        val completedCount = habits.count { it.isCompletedToday }
        val totalCount = habits.size
        binding.textHabitsSummary.text = "$completedCount of $totalCount completed today"
    }

    // HabitActionListener Implementation
    override fun onToggleCompletion(habit: Habit) {
        habitsViewModel.toggleHabitCompletion(habit)
    }

    override fun onEditHabit(habit: Habit) {
        val dialog = AddEditHabitDialogFragment.newInstance(habit)
        dialog.setTargetFragment(this, 0)
        dialog.show(parentFragmentManager, AddEditHabitDialogFragment.TAG)
    }

    override fun onDeleteHabit(habit: Habit) {
        habitsViewModel.deleteHabit(habit)
        Toast.makeText(context, "${habit.name} deleted", Toast.LENGTH_SHORT).show()
    }

    // HabitDialogListener Implementation
    override fun onHabitSaved(habit: Habit) {
        val existingHabit = habitsViewModel.habits.value?.find { it.id == habit.id }

        if (existingHabit == null) {
            habitsViewModel.addHabit(habit)
            Toast.makeText(context, getString(R.string.habit_added_success), Toast.LENGTH_SHORT).show()
        } else {
            habitsViewModel.updateHabit(habit)
            Toast.makeText(context, getString(R.string.habit_updated_success), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}