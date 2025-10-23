package com.example.zenflow.ui.habits

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.zenflow.R
import com.example.zenflow.data.Habit
import com.example.zenflow.databinding.ItemHabitBinding

interface HabitActionListener {
    fun onToggleCompletion(habit: Habit)
    fun onEditHabit(habit: Habit)
    fun onDeleteHabit(habit: Habit)
}

class HabitAdapter(private val listener: HabitActionListener) :
    RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    private var habits: List<Habit> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newHabits: List<Habit>) {
        habits = newHabits
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(habits[position])
    }

    override fun getItemCount(): Int = habits.size

    inner class HabitViewHolder(private val binding: ItemHabitBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(habit: Habit) {
            // Set habit info
            binding.textHabitName.text = habit.name
            binding.textStreakCount.text = "${habit.streakCount} day streak"

            // Display emoji based on habit icon resource
            binding.imageHabitIcon.text = getHabitEmoji(habit.iconResId)

            // Show completed indicator (left border)
            if (habit.isCompletedToday) {
                binding.viewCompletedIndicator.visibility = View.VISIBLE
            } else {
                binding.viewCompletedIndicator.visibility = View.GONE
            }

            // Update completion button state
            if (habit.isCompletedToday) {
                binding.btnToggleComplete.setImageResource(R.drawable.ic_check_circle_filled_24)
                binding.btnToggleComplete.setColorFilter(
                    ContextCompat.getColor(itemView.context, R.color.zen_accent_green)
                )
            } else {
                binding.btnToggleComplete.setImageResource(R.drawable.ic_check_circle_outline_24)
                binding.btnToggleComplete.setColorFilter(
                    ContextCompat.getColor(itemView.context, R.color.zen_light_text_secondary)
                )
            }

            // Click listeners
            binding.btnToggleComplete.setOnClickListener {
                listener.onToggleCompletion(habit)
            }
            binding.btnEdit.setOnClickListener {
                listener.onEditHabit(habit)
            }
            binding.btnDelete.setOnClickListener {
                listener.onDeleteHabit(habit)
            }
        }

        private fun getHabitEmoji(iconResId: Int): String {
            return when (iconResId) {
                com.example.zenflow.R.drawable.ic_water_drop -> "💧"
                com.example.zenflow.R.drawable.ic_running_person -> "🏃"
                com.example.zenflow.R.drawable.ic_meditation_person -> "🧘"
                com.example.zenflow.R.drawable.ic_books -> "📚"
                com.example.zenflow.R.drawable.ic_salad_bowl -> "🥗"
                com.example.zenflow.R.drawable.ic_sleeping_face -> "😴"
                com.example.zenflow.R.drawable.ic_muscle_arm -> "💪"
                com.example.zenflow.R.drawable.ic_target_dart -> "🎯"
                com.example.zenflow.R.drawable.ic_brain -> "🧠"
                com.example.zenflow.R.drawable.ic_heart -> "❤️"
                else -> "📌"
            }
        }
    }
}