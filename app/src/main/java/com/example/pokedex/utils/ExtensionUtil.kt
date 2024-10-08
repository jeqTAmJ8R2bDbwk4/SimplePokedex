package com.example.pokedex.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.provider.Settings
import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.Toolbar
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
import com.example.pokedex.models.ConsecutiveRange
import com.google.android.material.motion.MotionUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import com.example.pokedex.R
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import timber.log.Timber


fun TextView.setLeftDrawable(@DrawableRes id: Int = 0) {
    this.setCompoundDrawablesWithIntrinsicBounds(id, 0, 0, 0)
}

fun Activity.openSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", this@openSettings.packageName, null)
    }
    startActivity(intent)
}

fun Activity.openLicenses() {
    OssLicensesMenuActivity.setActivityTitle(getString(R.string.menu_name_licenses))
    startActivity(Intent(this, OssLicensesMenuActivity::class.java))
}

fun Activity.setRootMenuListener(toolbar: Toolbar) {
    toolbar.setOnMenuItemClickListener { menuItem ->
        return@setOnMenuItemClickListener when (menuItem.itemId) {
            R.id.settings -> {
                openSettings()
                true
            }
            R.id.licenses -> {
                openLicenses()
                true
            }
            else -> {
                Timber.e("Menu Item %s unknown.", menuItem.title)
                false
            }
        }
    }
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


