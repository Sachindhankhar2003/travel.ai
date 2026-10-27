package com.citygo.app.ui.screens.settings

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citygo.app.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val selectedCity: String = "Delhi",
    val tempUnit: String = "C",     // "C" | "F"
    val language: String = "en"     // "en" | "hi"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val cityKey = stringPreferencesKey(AppConstants.PREF_SELECTED_CITY)
    private val unitKey = stringPreferencesKey(AppConstants.PREF_TEMP_UNIT)
    private val langKey = stringPreferencesKey(AppConstants.PREF_LANGUAGE)

    val state = dataStore.data.map { prefs ->
        SettingsUiState(
            selectedCity = prefs[cityKey] ?: "Delhi",
            tempUnit     = prefs[unitKey] ?: "C",
            language     = prefs[langKey] ?: "en"
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setCity(city: String) = viewModelScope.launch {
        dataStore.edit { it[cityKey] = city }
    }

    fun setTempUnit(unit: String) = viewModelScope.launch {
        dataStore.edit { it[unitKey] = unit }
    }

    fun setLanguage(lang: String) = viewModelScope.launch {
        dataStore.edit { it[langKey] = lang }
        applyLocale(lang)
    }

    /**
     * Apply the chosen locale at runtime.
     * - API 33+: use the per-app LocaleManager (persisted automatically).
     * - API 24–32: use AppCompatDelegate (persisted by AndroidX).
     */
    private fun applyLocale(lang: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java)
                ?.applicationLocales = LocaleList.forLanguageTags(lang)
        } else {
            val localeList = LocaleListCompat.forLanguageTags(lang)
            AppCompatDelegate.setApplicationLocales(localeList)
        }
    }
}
