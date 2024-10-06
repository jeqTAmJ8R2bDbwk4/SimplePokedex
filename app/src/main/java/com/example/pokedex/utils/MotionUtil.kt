package com.example.pokedex.utils

import android.animation.TimeInterpolator
import android.content.Context
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.annotation.AttrRes
import com.example.pokedex.R
import com.google.android.material.motion.MotionUtils


/* Source: https://m3.material.io/styles/motion/easing-and-duration/applying-easing-and-duration */
sealed class MotionUtil(
    @get:AttrRes val durationRes: Int,
    val defaultDuration: Int,
    @get:AttrRes val interpolatorRes: Int,
    val defaultInterpolator: TimeInterpolator
) {
    fun duration(context: Context) = MotionUtils.resolveThemeDuration(
        context,
        durationRes,
        defaultDuration
    )

    fun interpolator(context: Context) = MotionUtils.resolveThemeInterpolator(
        context,
        interpolatorRes,
        defaultInterpolator
    )

    sealed class BeginAndEndOnScreen private constructor() {
        class Standard() {
            companion object : MotionUtil(
                R.attr.motionDurationMedium2,
                300,
                R.attr.motionEasingStandard,
                LinearInterpolator()
            )
        }
        class Emphasised private constructor() {
            companion object: MotionUtil(
                R.attr.motionDurationLong2,
                500,
                R.attr.motionEasingEmphasized,
                LinearInterpolator()
            )
        }
    }

    sealed class EnterTheScreen private constructor() {
        class Standard private constructor() {
            companion object: MotionUtil(
                R.attr.motionDurationMedium1,
                250,
                R.attr.motionEasingStandardDecelerateInterpolator,
                DecelerateInterpolator()
            )
        }
        class Emphasised private constructor() {
            companion object: MotionUtil(
                R.attr.motionDurationMedium4,
                400,
                R.attr.motionEasingEmphasizedDecelerateInterpolator,
                DecelerateInterpolator()
            )
        }
    }

    sealed class ExitTheScreen private constructor() {
        class Standard() {
            companion object: MotionUtil(
                R.attr.motionDurationShort4,
                200,
                R.attr.motionEasingStandardAccelerateInterpolator,
                AccelerateInterpolator()
            )
        }

        sealed class Emphasised private constructor() {
            companion object: MotionUtil(
                R.attr.motionDurationShort4,
                200,
                R.attr.motionEasingEmphasizedAccelerateInterpolator,
                AccelerateInterpolator()
            )
        }
    }
}


