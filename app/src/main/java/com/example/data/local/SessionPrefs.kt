package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "chashi_session_prefs")

class SessionPrefs(private val context: Context) {
    private val KEY_GUEST_MODE = booleanPreferencesKey("is_guest_mode")
    private val KEY_GUEST_NAME = stringPreferencesKey("guest_name")

    val isGuestMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_GUEST_MODE] ?: false
    }

    val guestName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_GUEST_NAME] ?: "অতিথি চাষি"
    }

    suspend fun setGuestMode(enabled: Boolean, name: String = "অতিথি চাষি") {
        context.dataStore.edit { prefs ->
            prefs[KEY_GUEST_MODE] = enabled
            if (name.isNotBlank()) {
                prefs[KEY_GUEST_NAME] = name
            }
        }
    }
}
