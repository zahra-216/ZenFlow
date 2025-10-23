package com.example.zenflow.ui.habits

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.zenflow.R
import com.example.zenflow.databinding.ItemIconSelectorBinding

class IconAdapter(
    private val icons: List<Int>,
    private val onIconClicked: (Int) -> Unit
) : RecyclerView.Adapter<IconAdapter.IconViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION
    private var selectedIconId: Int? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedIcon(iconId: Int) {
        selectedIconId = iconId
        selectedPosition = icons.indexOf(iconId)
        if (selectedPosition != RecyclerView.NO_POSITION) {
            notifyDataSetChanged()
        }
    }

    fun getSelectedIconId(): Int? = selectedIconId

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        val binding = ItemIconSelectorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IconViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.bind(icons[position], position)
    }

    override fun getItemCount(): Int = icons.size

    inner class IconViewHolder(private val binding: ItemIconSelectorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(iconResId: Int, position: Int) {
            // Display emoji
            binding.imageHabitIcon.text = getEmoji(iconResId)

            // Set the selection state with stroke
            if (position == selectedPosition) {
                binding.iconContainer.strokeColor = ContextCompat.getColor(
                    itemView.context,
                    R.color.zen_primary
                )
                binding.iconContainer.strokeWidth = 6
            } else {
                binding.iconContainer.strokeColor = android.graphics.Color.TRANSPARENT
                binding.iconContainer.strokeWidth = 2
            }

            binding.iconContainer.setOnClickListener {
                if (bindingAdapterPosition != selectedPosition) {
                    val previousSelectedPosition = selectedPosition
                    selectedPosition = bindingAdapterPosition
                    selectedIconId = iconResId

                    notifyItemChanged(previousSelectedPosition)
                    notifyItemChanged(selectedPosition)

                    onIconClicked(iconResId)
                }
            }
        }

        private fun getEmoji(iconResId: Int): String {
            return when (iconResId) {
                R.drawable.ic_water_drop -> "💧"
                R.drawable.ic_running_person -> "🏃"
                R.drawable.ic_meditation_person -> "🧘"
                R.drawable.ic_books -> "📚"
                R.drawable.ic_salad_bowl -> "🥗"
                R.drawable.ic_sleeping_face -> "😴"
                R.drawable.ic_muscle_arm -> "💪"
                R.drawable.ic_target_dart -> "🎯"
                R.drawable.ic_brain -> "🧠"
                R.drawable.ic_heart -> "❤️"
                else -> "📌"
            }
        }
    }
}