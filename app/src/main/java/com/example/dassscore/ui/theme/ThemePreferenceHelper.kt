package com.example.dassscore.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ThemePreferenceHelper(private val context: Context) {
    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    }

    val themeFlow: Flow<Boolean> =
            context.dataStore.data.map { preferences ->
                preferences[DARK_MODE_KEY] ?: false // Default to light mode (or use system later)
            }

    suspend fun setDarkMode(isDarkMode: Boolean) {
        context.dataStore.edit { prefs -> prefs[DARK_MODE_KEY] = isDarkMode }
    }

    fun isDarkMode(): Boolean {
        // Run blocking here just for initial sync, ideally use Flow collection
        return runBlocking { context.dataStore.data.first()[DARK_MODE_KEY] ?: false }
    }
}
