# ZenFlow - Wellness & Habit Tracker

An Android wellness app that helps users build healthy habits and track their emotional wellbeing. Built with Kotlin in Android Studio, ZenFlow is responsive across screen sizes and supports both light and dark themes.

## Features

- **Habit tracking** - create daily habits, mark them complete, and track consistency with streak counters and a daily completion percentage
- **Mood journal** - log emotions throughout the week (happy, calm, sad, anxious, angry, tired) with optional notes, viewable as a list or calendar
- **Weekly mood trend chart** - an interactive 7-day mood line graph built with MPAndroidChart, with a summary of total entries and most frequent mood, so users can spot patterns between habits and how they feel
- **Hydration reminders** - background notifications at user-chosen intervals (30 min to 4 hours), scheduled with WorkManager so they keep working even when the app is closed
- **Local data persistence** - all data (credentials, habits, mood entries, profile, and settings) stored on-device with SharedPreferences, so progress is never lost between sessions
- **Profile & settings** - manage personal info, view mood charts, configure reminders, and access help resources

## Tech Stack

- **Language:** Kotlin
- **IDE:** Android Studio
- **Charts:** MPAndroidChart
- **Background tasks:** WorkManager
- **Local storage:** SharedPreferences
- **UI:** Material 3 (DayNight), light & dark themes

## About

Individual project built for the Mobile Application Development module (IT2010) at SLIIT. Designed and developed end to end - UI/UX, habit and mood logic, charting, background notifications, and local data persistence.
