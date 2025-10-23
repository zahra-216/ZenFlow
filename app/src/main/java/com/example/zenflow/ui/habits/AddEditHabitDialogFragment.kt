package com.example.zenflow.ui.habits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.zenflow.R
import com.example.zenflow.data.Habit
import com.example.zenflow.databinding.DialogAddEditHabitBinding
import com.example.zenflow.util.HabitIcon

interface HabitDialogListener {
    fun onHabitSaved(habit: Habit)
}

class AddEditHabitDialogFragment : DialogFragment() {

    private var _binding: DialogAddEditHabitBinding? = null
    private val binding get() = _binding!!

    private val habitToEdit: Habit? by lazy {
        arguments?.getParcelable(ARG_HABIT) as Habit?
    }

    private val listener: HabitDialogListener
        get() = targetFragment as HabitDialogListener

    private lateinit var iconAdapter: IconAdapter
    private var selectedIconResId: Int = HabitIcon.DEFAULT_ICON

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setStyle(STYLE_NORMAL, R.style.Theme_ZenFlow_Dialog)
        _binding = DialogAddEditHabitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupIconRecyclerView()

        if (habitToEdit != null) {
            binding.textDialogTitle.setText(R.string.dialog_edit_title)
            binding.editHabitName.setText(habitToEdit!!.name)
            binding.btnSaveHabit.setText(R.string.button_update)
            selectedIconResId = habitToEdit!!.iconResId
            iconAdapter.setSelectedIcon(selectedIconResId)
        } else {
            binding.textDialogTitle.setText(R.string.add_habit)
            binding.btnSaveHabit.setText(R.string.button_save)
            iconAdapter.setSelectedIcon(selectedIconResId)
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSaveHabit.setOnClickListener {
            saveHabit()
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.95).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            // Add rounded corners to dialog
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    private fun setupIconRecyclerView() {
        val iconList = HabitIcon.ICONS.values.toList()

        iconAdapter = IconAdapter(
            icons = iconList,
            onIconClicked = { iconId ->
                selectedIconResId = iconId
            }
        )

        binding.recyclerViewIcons.apply {
            layoutManager = GridLayoutManager(context, 5)
            adapter = iconAdapter

        }
    }

    private fun saveHabit() {
        val name = binding.editHabitName.text.toString().trim()

        if (name.isEmpty()) {
            binding.editHabitName.error = getString(R.string.error_habit_name_empty)
            return
        }

        val finalIconId = iconAdapter.getSelectedIconId() ?: selectedIconResId

        val newOrUpdatedHabit = habitToEdit?.copy(
            name = name,
            iconResId = finalIconId
        ) ?: Habit(
            name = name,
            iconResId = finalIconId
        )

        listener.onHabitSaved(newOrUpdatedHabit)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AddEditHabitDialog"
        private const val ARG_HABIT = "habit_to_edit"

        fun newInstance(habit: Habit? = null): AddEditHabitDialogFragment {
            val fragment = AddEditHabitDialogFragment()
            if (habit != null) {
                val args = Bundle().apply {
                    putParcelable(ARG_HABIT, habit)
                }
                fragment.arguments = args
            }
            return fragment
        }
    }
}

// Add this ItemDecoration class for proper spacing
class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean
) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: android.graphics.Rect,
        view: android.view.View,
        parent: androidx.recyclerview.widget.RecyclerView,
        state: androidx.recyclerview.widget.RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount

            if (position < spanCount) {
                outRect.top = spacing
            }
            outRect.bottom = spacing
        } else {
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount
            if (position >= spanCount) {
                outRect.top = spacing
            }
        }
    }
}