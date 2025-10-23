package com.example.zenflow.ui.moods

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.zenflow.data.Mood
import com.example.zenflow.databinding.ItemMoodEntryBinding
import java.text.SimpleDateFormat
import java.util.*

interface MoodActionListener {
    fun onShareMood(mood: Mood)
    fun onDeleteMood(mood: Mood)
}

class MoodAdapter(private val listener: MoodActionListener) :
    RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    private var moods: List<Mood> = emptyList()
    private val dateFormat = SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault())

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newMoods: List<Mood>) {
        moods = newMoods
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val binding = ItemMoodEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        holder.bind(moods[position])
    }

    override fun getItemCount(): Int = moods.size

    inner class MoodViewHolder(private val binding: ItemMoodEntryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(mood: Mood) {
            binding.textEntryEmoji.text = mood.emoji
            binding.textEntryMoodName.text = mood.moodName
            binding.textEntryTimestamp.text = dateFormat.format(Date(mood.timestamp))

            if (mood.note.isNotEmpty()) {
                binding.textEntryNote.visibility = View.VISIBLE
                binding.textEntryNote.text = mood.note
            } else {
                binding.textEntryNote.visibility = View.GONE
            }

            binding.btnShareEntry.setOnClickListener {
                listener.onShareMood(mood)
            }

            binding.btnDeleteEntry.setOnClickListener {
                listener.onDeleteMood(mood)
            }
        }
    }
}