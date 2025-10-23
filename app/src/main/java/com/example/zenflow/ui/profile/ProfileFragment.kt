package com.example.zenflow.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.zenflow.R
import com.example.zenflow.databinding.FragmentProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.work.*
import java.util.concurrent.TimeUnit
import com.example.zenflow.workers.HydrationReminderWorker
import androidx.core.net.toUri
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.zenflow.SigninActivity
import com.example.zenflow.ui.moods.MoodChartActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by viewModels()

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                updateProfilePicture(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()
        checkNotificationPermission()
    }

    private fun setupObservers() {
        profileViewModel.user.observe(viewLifecycleOwner) { user ->
            binding.textUserName.text = user.name
            binding.textUserEmail.text = user.email

            user.profilePictureUri?.let { uriString ->
                Glide.with(this)
                    .load(uriString.toUri())
                    .placeholder(R.drawable.ic_person_placeholder)
                    .circleCrop()
                    .into(binding.imgProfilePicture)
            }
        }

        profileViewModel.hydrationInterval.observe(viewLifecycleOwner) { interval ->
            updateHydrationIntervalDisplay(interval)
        }
    }

    private fun updateHydrationIntervalDisplay(interval: Int) {
        if (interval > 0) {
            val hours = interval / 60
            val minutes = interval % 60
            binding.textHydrationInterval.text = when {
                hours > 0 && minutes > 0 -> getString(R.string.hydration_interval_format_full, hours, minutes)
                hours > 0 -> getString(R.string.hydration_interval_format_hours, hours)
                else -> getString(R.string.hydration_interval_format_minutes, minutes)
            }
        } else {
            binding.textHydrationInterval.text = getString(R.string.not_set)
        }
    }

    private fun setupClickListeners() {
        binding.btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        binding.layoutHydrationReminder.setOnClickListener {
            showHydrationReminderDialog()
        }

        binding.layoutAppSettings.setOnClickListener {
            Toast.makeText(context, R.string.coming_soon, Toast.LENGTH_SHORT).show()
        }

        binding.layoutWidgetConfig.setOnClickListener {
            showWidgetConfigDialog()
        }

        binding.layoutMoodChart.setOnClickListener {
            val intent = Intent(requireContext(), MoodChartActivity::class.java)
            startActivity(intent)
        }

        binding.layoutSensorIntegration.setOnClickListener {
            showSensorIntegrationDialog()
        }

        binding.layoutPrivacyPolicy.setOnClickListener {
            showPrivacyPolicy()
        }

        binding.layoutHelpSupport.setOnClickListener {
            showHelpSupport()
        }

        binding.btnTestNotification.setOnClickListener {
            sendTestNotification()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val editName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edit_profile_name)
        val editEmail = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edit_profile_email)
        val btnChangePicture = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_change_picture)

        editName.setText(profileViewModel.user.value?.name)
        editEmail.setText(profileViewModel.user.value?.email)

        btnChangePicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.edit_profile)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                val newName = editName.text.toString().trim()
                val newEmail = editEmail.text.toString().trim()

                if (newName.isNotEmpty()) {
                    profileViewModel.updateUserName(newName)
                }

                if (newEmail.isNotEmpty()) {
                    profileViewModel.updateUserEmail(newEmail)
                }

                Toast.makeText(context, R.string.profile_updated, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun updateProfilePicture(uri: Uri) {
        profileViewModel.updateProfilePicture(uri.toString())
        Toast.makeText(context, R.string.profile_picture_updated, Toast.LENGTH_SHORT).show()
    }

    private fun showHydrationReminderDialog() {
        val intervals = arrayOf(
            "Every 15 minutes",
            getString(R.string.interval_30_min),
            getString(R.string.interval_1_hour),
            getString(R.string.interval_2_hours),
            getString(R.string.interval_3_hours),
            getString(R.string.interval_4_hours),
            getString(R.string.interval_disable)
        )

        val intervalValues = intArrayOf(15, 30, 60, 120, 180, 240, 0)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.hydration_reminder)
            .setItems(intervals) { _, which ->
                val selectedInterval = intervalValues[which]
                profileViewModel.saveHydrationInterval(selectedInterval)

                if (selectedInterval > 0) {
                    scheduleHydrationReminder(selectedInterval)
                    Toast.makeText(context, R.string.hydration_reminder_enabled, Toast.LENGTH_SHORT).show()
                } else {
                    cancelHydrationReminder()
                    Toast.makeText(context, R.string.hydration_reminder_disabled, Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun scheduleHydrationReminder(intervalMinutes: Int) {
        val workManager = WorkManager.getInstance(requireContext())

        workManager.cancelUniqueWork(HydrationReminderWorker.WORK_NAME)

        val workRequest = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
            intervalMinutes.toLong(),
            TimeUnit.MINUTES,
            15,
            TimeUnit.MINUTES
        ).build()

        workManager.enqueueUniquePeriodicWork(
            HydrationReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun cancelHydrationReminder() {
        WorkManager.getInstance(requireContext())
            .cancelUniqueWork(HydrationReminderWorker.WORK_NAME)
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(
                context,
                "Notification permission is required for hydration reminders",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showWidgetConfigDialog() {
        val isEnabled = profileViewModel.isWidgetEnabled()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.habit_widget)
            .setMessage(R.string.widget_config_message)
            .setPositiveButton(if (isEnabled) R.string.disable else R.string.enable) { _, _ ->
                profileViewModel.setWidgetEnabled(!isEnabled)
                Toast.makeText(
                    context,
                    if (!isEnabled) R.string.widget_enabled else R.string.widget_disabled,
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showSensorIntegrationDialog() {
        val isEnabled = profileViewModel.isSensorEnabled()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.sensor_integration)
            .setMessage(R.string.sensor_config_message)
            .setPositiveButton(if (isEnabled) R.string.disable else R.string.enable) { _, _ ->
                profileViewModel.setSensorEnabled(!isEnabled)
                Toast.makeText(
                    context,
                    if (!isEnabled) R.string.sensor_enabled else R.string.sensor_disabled,
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showPrivacyPolicy() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.privacy_policy)
            .setMessage(R.string.privacy_policy_content)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    private fun showHelpSupport() {
        val options = arrayOf(
            getString(R.string.faq),
            getString(R.string.contact_support),
            getString(R.string.about_app)
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.help_support)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showFAQ()
                    1 -> contactSupport()
                    2 -> showAboutApp()
                }
            }
            .show()
    }

    private fun showFAQ() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.faq)
            .setMessage(R.string.faq_content)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    private fun contactSupport() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:support@zenflow.com")
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_email_subject))
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, R.string.no_email_app, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAboutApp() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.about_app)
            .setMessage(R.string.about_app_content)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    private fun sendTestNotification() {
        val testWorker = OneTimeWorkRequestBuilder<HydrationReminderWorker>().build()
        WorkManager.getInstance(requireContext()).enqueueUniqueWork(
            "test_notification",
            ExistingWorkPolicy.REPLACE,
            testWorker
        )
        Toast.makeText(context, "Test notification sent!", Toast.LENGTH_SHORT).show()
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_confirmation)
            .setPositiveButton(R.string.logout) { _, _ ->
                performLogout()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun performLogout() {
        val intent = Intent(requireContext(), SigninActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}