package com.example.zenflow.data

import android.content.Context
import android.content.SharedPreferences

class ProfileSharedPreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "profile_preferences"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PASSWORD = "user_password"
        private const val KEY_PROFILE_PICTURE_URI = "profile_picture_uri"
        private const val KEY_HYDRATION_INTERVAL = "hydration_interval"
        private const val KEY_HYDRATION_ENABLED = "hydration_enabled"
        private const val KEY_WIDGET_ENABLED = "widget_enabled"
        private const val KEY_SENSOR_ENABLED = "sensor_enabled"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    // Authentication
    fun saveUserCredentials(name: String, email: String, password: String) {
        sharedPreferences.edit().apply {
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_PASSWORD, password)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun verifyCredentials(email: String, password: String): Boolean {
        val storedEmail = sharedPreferences.getString(KEY_USER_EMAIL, null)
        val storedPassword = sharedPreferences.getString(KEY_USER_PASSWORD, null)
        return storedEmail == email && storedPassword == password
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun logout() {
        sharedPreferences.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
    }

    // User Profile
    fun saveUser(user: User) {
        sharedPreferences.edit().apply {
            putString(KEY_USER_NAME, user.name)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_PROFILE_PICTURE_URI, user.profilePictureUri)
            apply()
        }
    }

    fun loadUser(): User {
        return User(
            name = sharedPreferences.getString(KEY_USER_NAME, "User Name") ?: "User Name",
            email = sharedPreferences.getString(KEY_USER_EMAIL, "user@example.com") ?: "user@example.com",
            profilePictureUri = sharedPreferences.getString(KEY_PROFILE_PICTURE_URI, null)
        )
    }

    fun saveUserName(name: String) {
        sharedPreferences.edit().putString(KEY_USER_NAME, name).apply()
    }

    fun saveUserEmail(email: String) {
        sharedPreferences.edit().putString(KEY_USER_EMAIL, email).apply()
    }

    fun saveProfilePictureUri(uri: String) {
        sharedPreferences.edit().putString(KEY_PROFILE_PICTURE_URI, uri).apply()
    }

    // Hydration Reminder
    fun saveHydrationInterval(intervalMinutes: Int) {
        sharedPreferences.edit().putInt(KEY_HYDRATION_INTERVAL, intervalMinutes).apply()
    }

    fun getHydrationInterval(): Int {
        return sharedPreferences.getInt(KEY_HYDRATION_INTERVAL, 0)
    }

    fun setHydrationEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_HYDRATION_ENABLED, enabled).apply()
    }

    fun isHydrationEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_HYDRATION_ENABLED, false)
    }

    // Advanced Features
    fun setWidgetEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_WIDGET_ENABLED, enabled).apply()
    }

    fun isWidgetEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_WIDGET_ENABLED, false)
    }

    fun setSensorEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_SENSOR_ENABLED, enabled).apply()
    }

    fun isSensorEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_SENSOR_ENABLED, false)
    }
}