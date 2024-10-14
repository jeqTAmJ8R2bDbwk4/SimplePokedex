package com.example.pokedex.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.ConnectivityManager.RESTRICT_BACKGROUND_STATUS_DISABLED
import android.net.ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED
import android.net.ConnectivityManager.RESTRICT_BACKGROUND_STATUS_WHITELISTED
import android.net.Uri
import android.provider.Settings
import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.NightMode
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.window.core.layout.WindowSizeClass
import androidx.window.layout.WindowMetricsCalculator
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.memory.MemoryCache
import com.example.pokedex.R
import com.example.pokedex.models.ConsecutiveRange
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties


fun TextView.setLeftDrawable(@DrawableRes id: Int = 0) {
    this.setCompoundDrawablesWithIntrinsicBounds(id, 0, 0, 0)
}

fun Activity.openSystemSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", this@openSystemSettings.packageName, null)
    }
    startActivity(intent)
}

fun Activity.openLicenses() {
    OssLicensesMenuActivity.setActivityTitle(getString(R.string.menu_name_licenses))
    startActivity(Intent(this, OssLicensesMenuActivity::class.java))
}

fun <T> List<T>.squeeze(): T {
    return when (size) {
        1 -> first() // Return the single element if exactly one exists
        0 -> throw NoSuchElementException("List is empty") // Throw error if list is empty
        else -> throw IllegalArgumentException("List contains more than one element") // Throw error if more than one element exists
    }
}

fun <T> T.unsqueeze(): List<T> {
    return listOf(this)
}

fun Context.resolveAttribute(@AttrRes attrId: Int): Int {
    val value = TypedValue()
    theme.resolveAttribute(
        attrId, value, true
    )
    return value.resourceId
}

fun Context.isDarkMode(): Boolean {
    val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
}

fun Context.isLandscape(): Boolean {
    return resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

fun Context.getAppName(): CharSequence {
    return packageManager.getApplicationLabel(applicationInfo)
}

fun Activity.computeWindowSizeClasses(): WindowSizeClass {
    val metrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this)
    val width = metrics.bounds.width()
    val height = metrics.bounds.height()
    val density = resources.displayMetrics.density
    val windowSizeClass = WindowSizeClass.compute(width/density, height/density)
    return windowSizeClass
}

fun WindowInsetsCompat.fragmentInsets(): Insets {
    val systemBarInsets = getInsets(WindowInsetsCompat.Type.systemBars())
    val cutoutInsets = getInsets(WindowInsetsCompat.Type.displayCutout())
    return Insets.max(systemBarInsets, cutoutInsets)
}

fun <T> Flow<T>.collectWithLifecycle(
    lifecycleOwner: LifecycleOwner,
    context: CoroutineContext = EmptyCoroutineContext,
    collector: FlowCollector<T>
) {
    lifecycleOwner.lifecycleScope.launch(context) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            collect(collector)
        }
    }
}

fun Collection<Int>.consequtives(): List<ConsecutiveRange> {
    return fold(mutableListOf<ConsecutiveRange.Mutable>()) { acc, item ->
        acc.apply {
            val last = lastOrNull()
            if (last == null || last.start + last.size != item) {
                add(ConsecutiveRange.Mutable(start = item, size = 1))
                return@apply
            }
            last.size += 1
        }
    }
}

fun <T: ViewHolder> Adapter<T>.notifyPositionChanged(positions: Collection<Int>, payload: Any?) {
    positions.sorted().consequtives().forEach { range ->
        notifyItemRangeChanged(range.start, range.size, payload)
    }
}

@Throws(IllegalArgumentException::class, NonEmptyException::class)
inline fun <reified T : Any> T.validateNonEmpty() {
    for (property in T::class.memberProperties) {
        val nonEmpty = property.findAnnotation<NonEmpty>()
        if (nonEmpty == null) {
            continue
        }
        val value = property.get(this)
        if (value !is String) {
            throw IllegalArgumentException("${property.name} is not of type String but is annotated with @NonEmpty")
        }
        if (value.isNotEmpty()) {
            continue
        }
        throw NonEmptyException(property.name)
    }
}


inline val RecyclerView.ViewHolder.context get() = itemView.context

fun Int.formatPokedexNumber() = String.format(Locale.GERMAN, "#%04d", this)

fun MotionLayout.updateConstraintSet(stateId: Int, update: ConstraintSet.() -> Unit) {
    val constraintSet = getConstraintSet(stateId).apply(update)
    setConstraintSet(constraintSet)
}

// Following guide: https://developer.android.com/develop/connectivity/network-ops/data-saver
fun Context.isDataSaving(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (connectivityManager.isActiveNetworkMetered) {
        return true
    }
    val restrictBackgroundStatus = connectivityManager.restrictBackgroundStatus
    return when(restrictBackgroundStatus) {
        RESTRICT_BACKGROUND_STATUS_DISABLED -> false
        RESTRICT_BACKGROUND_STATUS_WHITELISTED -> false
        RESTRICT_BACKGROUND_STATUS_ENABLED -> true
        else -> {
            Timber.e("Unknown restrictBackgroundStatus $restrictBackgroundStatus.")
            assert(false)
            false
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
fun ImageLoader.isCacheHit(key: String): Boolean {
    if (memoryCache?.get(MemoryCache.Key(key)) != null) {
        Timber.d("MemoryCache-Hit: $key")
        return true
    }
    val snapshot = diskCache?.openSnapshot(key) ?: return false
    try {
        val exists = snapshot.data.toFile().exists()
        if (exists) {
            Timber.d("DiskCache-Hit: $key")
        }
    } catch (e: Exception) {
        Timber.e(e)
        assert(false)
    } finally {
        snapshot.close()
    }
    return false
}

@OptIn(ExperimentalCoilApi::class)
fun ImageLoader.clearCache() {
    memoryCache?.clear()
    diskCache?.clear()
}

fun Context.getSharedSettingsPreferences(): SharedPreferences {
    val sharedPreferenceNameSettings = getString(R.string.shared_preference_name_settings)
    return getSharedPreferences(sharedPreferenceNameSettings, Application.MODE_PRIVATE)
}

@Throws(ClassCastException::class)
fun Context.getThemePreferenceValue(): String {
    val preferenceThemeKey = getString(R.string.preference_theme_key)
    val preferenceThemeEntryValueAuto = getString(R.string.preference_theme_entry_value_auto)
    return getSharedSettingsPreferences()
        .getString(preferenceThemeKey, preferenceThemeEntryValueAuto)!!
}

@Throws(IllegalArgumentException::class)
fun Context.setThemePreferenceValue(value: String) {
    val preferenceThemeKey = getString(R.string.preference_theme_key)
    val preferenceThemeEntryValues = resources.getStringArray(R.array.preference_theme_entry_values)
    require(preferenceThemeEntryValues.contains(value)) {
        "$value is not a valid preference theme entry value: $preferenceThemeEntryValues."
    }
    getSharedSettingsPreferences().edit().putString(preferenceThemeKey, value).apply()
}

@Throws(ClassCastException::class)
fun Context.getDataSavingPreferenceValue(): Boolean {
    val preferenceDataSavingDefault = resources.getBoolean(R.bool.preference_data_saving_default)
    val preferenceDataSavingKey = getString(R.string.preference_data_saving_key)
    return getSharedSettingsPreferences()
        .getBoolean(preferenceDataSavingKey, preferenceDataSavingDefault)
}

fun Context.setDataSavingPreferenceValue(value: Boolean) {
    val preferenceDataSavingKey = getString(R.string.preference_data_saving_key)
    getSharedSettingsPreferences().edit().putBoolean(preferenceDataSavingKey, value).apply()
}

@Throws(IllegalArgumentException::class)
@NightMode fun Context.getNightMode(preferenceThemeEntryValue: String): Int {
    val preferenceThemeEntryValueAuto = getString(R.string.preference_theme_entry_value_auto)
    val preferenceThemeEntryValueLight = getString(R.string.preference_theme_entry_value_light)
    val preferenceThemeEntryValueDark = getString(R.string.preference_theme_entry_value_dark)

    return when(preferenceThemeEntryValue) {
        preferenceThemeEntryValueAuto -> {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        preferenceThemeEntryValueLight -> {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        preferenceThemeEntryValueDark -> {
            AppCompatDelegate.MODE_NIGHT_YES
        }
        else -> {
            throw IllegalArgumentException(
                "Unexpeccted preferenceThemeEntryValue: $preferenceThemeEntryValue"
            )
        }
    }
}

fun Context.applyTheme(preferenceThemeEntryValue: String): Boolean {
    val nightMode = try {
        getNightMode(preferenceThemeEntryValue)
    } catch (e: IllegalArgumentException) {
        Timber.e(e)
        assert(false)
        return false
    }
    AppCompatDelegate.setDefaultNightMode(nightMode)
    return true
}

fun Resources.isStringRes(@StringRes res: Int): Boolean {
    return try {
        getResourceTypeName(res) == "string"
    } catch (e: Resources.NotFoundException) {
        Timber.e(e)
        false
    }.also(::assert)
}