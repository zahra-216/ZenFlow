package com.example.zenflow.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.zenflow.data.User
import com.example.zenflow.data.ProfileSharedPreferencesManager

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefsManager = ProfileSharedPreferencesManager(application)

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _hydrationInterval = MutableLiveData<Int>()
    val hydrationInterval: LiveData<Int> = _hydrationInterval

    init {
        loadUserProfile()
        loadHydrationSettings()
    }

    // User Profile Methods
    fun loadUserProfile() {
        _user.value = sharedPrefsManager.loadUser()
    }

    fun updateUserName(name: String) {
        sharedPrefsManager.saveUserName(name)
        loadUserProfile()
    }

    fun updateUserEmail(email: String) {
        sharedPrefsManager.saveUserEmail(email)
        loadUserProfile()
    }

    fun updateProfilePicture(uri: String) {
        sharedPrefsManager.saveProfilePictureUri(uri)
        loadUserProfile()
    }

    fun updateUser(user: User) {
        sharedPrefsManager.saveUser(user)
        loadUserProfile()
    }

    // Hydration Reminder Methods
    fun loadHydrationSettings() {
        _hydrationInterval.value = sharedPrefsManager.getHydrationInterval()
    }

    fun saveHydrationInterval(intervalMinutes: Int) {
        sharedPrefsManager.saveHydrationInterval(intervalMinutes)
        sharedPrefsManager.setHydrationEnabled(intervalMinutes > 0)
        _hydrationInterval.value = intervalMinutes
    }

    fun isHydrationEnabled(): Boolean {
        return sharedPrefsManager.isHydrationEnabled()
    }

    // Advanced Features Methods
    fun setWidgetEnabled(enabled: Boolean) {
        sharedPrefsManager.setWidgetEnabled(enabled)
    }

    fun isWidgetEnabled(): Boolean {
        return sharedPrefsManager.isWidgetEnabled()
    }

    fun setSensorEnabled(enabled: Boolean) {
        sharedPrefsManager.setSensorEnabled(enabled)
    }

    fun isSensorEnabled(): Boolean {
        return sharedPrefsManager.isSensorEnabled()
    }
}