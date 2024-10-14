package com.example.pokedex.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.NightMode
import androidx.fragment.app.viewModels
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import coil.imageLoader
import com.example.pokedex.BuildConfig
import com.example.pokedex.R
import com.example.pokedex.utils.applyTheme
import com.example.pokedex.utils.clearCache
import com.example.pokedex.utils.getDataSavingPreferenceValue
import com.example.pokedex.utils.getNightMode
import com.example.pokedex.utils.getThemePreferenceValue
import com.example.pokedex.utils.openLicenses
import com.example.pokedex.utils.openSystemSettings
import com.example.pokedex.utils.setDataSavingPreferenceValue
import com.example.pokedex.utils.setThemePreferenceValue
import com.example.pokedex.viewmodels.SettingsViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class SettingsFragment: PreferenceFragmentCompat() {
    private val viewModel: SettingsViewModel by viewModels()

    lateinit var preferenceVersionKey: String
    lateinit var preferenceThemeKey: String
    lateinit var preferenceThemeEntryValueAuto: String
    lateinit var preferenceCacheKey: String
    lateinit var preferenceHistoryKey: String
    lateinit var preferenceDataSavingKey: String
    lateinit var preferenceSystemSettingsKey: String
    lateinit var preferenceLicensesKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        preferenceVersionKey = getString(R.string.preference_version_key)
        preferenceThemeKey = getString(R.string.preference_theme_key)
        preferenceThemeEntryValueAuto = getString(R.string.preference_theme_entry_value_auto)
        preferenceCacheKey = getString(R.string.preference_cache_key)
        preferenceHistoryKey = getString(R.string.preference_history_key)
        preferenceDataSavingKey = getString(R.string.preference_data_saving_key)
        preferenceSystemSettingsKey = getString(R.string.preference_system_settings_key)
        preferenceLicensesKey = getString(R.string.preference_licenses_key)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    @DrawableRes fun getPreferenceThemeIcon(@NightMode nightMode: Int): Int {
        return when (nightMode) {
            AppCompatDelegate.MODE_NIGHT_YES -> R.drawable.preference_icon_theme_dark
            AppCompatDelegate.MODE_NIGHT_NO -> R.drawable.preference_icon_theme_light
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> R.drawable.preference_icon_theme_auto
            AppCompatDelegate.MODE_NIGHT_UNSPECIFIED -> {
                Timber.e("Unexpected defaultNightMode: MODE_NIGHT_UNSPECIFIED")
                assert(false)
                R.drawable.preference_icon_theme_auto
            }
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY -> {
                Timber.e("Unexpected defautlNightMode: MODE_NIGHT_AUTO_BATTERY")
                assert(false)
                R.drawable.preference_icon_theme_auto
            }
            else -> {
                Timber.e("Unexpected defautlNightMode: $nightMode")
                assert(false)
                R.drawable.preference_icon_theme_auto
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val preferenceVersion = findPreference<Preference>(preferenceVersionKey)!!
        val preferenceTheme = findPreference<ListPreference>(preferenceThemeKey)!!
        val preferenceCache = findPreference<Preference>(preferenceCacheKey)!!
        val preferenceHistory = findPreference<Preference>(preferenceHistoryKey)!!
        val preferenceDataSaving = findPreference<SwitchPreferenceCompat>(preferenceDataSavingKey)!!
        val preferenceSystemSettings = findPreference<Preference>(preferenceSystemSettingsKey)!!
        val preferenceLicenses = findPreference<Preference>(preferenceLicensesKey)!!

        preferenceVersion.summary = BuildConfig.VERSION_NAME

        val preferenceThemeIconResource = getPreferenceThemeIcon(AppCompatDelegate.getDefaultNightMode())
        preferenceTheme.setIcon(preferenceThemeIconResource)
        preferenceTheme.value = requireContext().getThemePreferenceValue()
        preferenceTheme.setOnPreferenceChangeListener { _, newValue ->
            if (newValue !is String) {
                Timber.e("Unexpected preferenceThemeEntryValue: %s.", newValue)
                assert(false)
                return@setOnPreferenceChangeListener false
            }

            val success = requireContext().applyTheme(newValue)
            if (!success) {
                return@setOnPreferenceChangeListener false
            }
            try {
                // Since no configuration change if actual theme does not change.
                val preferenceThemeIconResource = getPreferenceThemeIcon(requireContext().getNightMode(newValue))
                preferenceTheme.setIcon(preferenceThemeIconResource)
            } catch (e: IllegalArgumentException) {
                Timber.e(e)
                assert(false)
            }
            requireContext().setThemePreferenceValue(newValue)
            true
        }
        preferenceCache.setOnPreferenceClickListener {
            requireActivity().imageLoader.clearCache()
            viewModel.clearCache()
            Snackbar.make(view, R.string.snackbar_message_cache_cleared, Snackbar.LENGTH_SHORT).show()
            true
        }

        preferenceHistory.setOnPreferenceClickListener {
            viewModel.clearHistory()
            Snackbar.make(view, R.string.snackbar_message_history_cleared, Snackbar.LENGTH_SHORT).show()
            true
        }

        preferenceDataSaving.setDefaultValue(requireContext().getDataSavingPreferenceValue())
        preferenceDataSaving.setOnPreferenceChangeListener { _, newValue ->
            if (newValue !is Boolean) {
                Timber.e("Unexpected preferenceDataSavingValue: $newValue.")
                assert(false)
                return@setOnPreferenceChangeListener false
            }

            requireContext().setDataSavingPreferenceValue(newValue)
            true
        }

        preferenceSystemSettings.setOnPreferenceClickListener {
            requireActivity().openSystemSettings()
            true
        }

        preferenceLicenses.setOnPreferenceClickListener {
            requireActivity().openLicenses()
            true
        }
    }
}