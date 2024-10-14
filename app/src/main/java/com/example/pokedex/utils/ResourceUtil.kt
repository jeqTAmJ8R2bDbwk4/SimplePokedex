package com.example.pokedex.utils

import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import com.example.pokedex.R

object ResourceUtil {
    // Drawable source: https://github.com/duiker101/pokemon-type-svg-icons/tree/master
    @DrawableRes
    fun getDrawableResourceFromTypeId(typeId: Int): Int {
        return when(typeId) {
            1 -> R.drawable.normal
            10 -> R.drawable.fire
            11 -> R.drawable.water
            13 -> R.drawable.electric
            12 -> R.drawable.grass
            15 -> R.drawable.ice
            2 -> R.drawable.fighting
            4 -> R.drawable.poison
            5 -> R.drawable.ground
            3 -> R.drawable.flying
            14 -> R.drawable.psychic
            7 -> R.drawable.bug
            6 -> R.drawable.rock
            8 -> R.drawable.ghost
            16 -> R.drawable.dragon
            17 -> R.drawable.dark
            9 -> R.drawable.steel
            18 -> R.drawable.fairy
            19 -> R.drawable.question_mark_16dp
            10001 -> R.drawable.question_mark_16dp
            10002 -> R.drawable.question_mark_16dp
            else -> R.drawable.question_mark_16dp
        }
    }

    @AttrRes
    fun getAttrResFromTypeId(typeId: Int): Int {
        return when(typeId) {
            1 -> R.attr.colorTypeNormal
            10 -> R.attr.colorTypeFire
            11 -> R.attr.colorTypeWater
            13 -> R.attr.colorTypeElectric
            12 -> R.attr.colorTypeGrass
            15 -> R.attr.colorTypeIce
            2 -> R.attr.colorTypeFighting
            4 -> R.attr.colorTypePoison
            5 -> R.attr.colorTypeGround
            3 -> R.attr.colorTypeFlying
            14 -> R.attr.colorTypePsychic
            7 -> R.attr.colorTypeBug
            6 -> R.attr.colorTypeRock
            8 -> R.attr.colorTypeGhost
            16 -> R.attr.colorTypeDragon
            17 -> R.attr.colorTypeDark
            9 -> R.attr.colorTypeSteel
            18 -> R.attr.colorTypeFairy
            10001 -> R.attr.colorTypeQuestionMark
            10002 -> R.attr.colorTypeQuestionMark
            else -> R.attr.colorTypeQuestionMark
        }
    }
}