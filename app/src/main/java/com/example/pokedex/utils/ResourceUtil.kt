package com.example.pokedex.utils

import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import com.example.pokedex.R

object ResourceUtil {
    // Drawable source: https://github.com/duiker101/pokemon-type-svg-icons/tree/master
    @DrawableRes
    fun getDrawableResourceFromTypeId(typeId: Int): Int {
        return when(typeId) {
            1 -> R.drawable.pokemon_type_normal
            10 -> R.drawable.pokemon_type_fire
            11 -> R.drawable.pokemon_type_water
            13 -> R.drawable.pokemon_type_electric
            12 -> R.drawable.pokemon_type_grass
            15 -> R.drawable.pokemon_type_ice
            2 -> R.drawable.pokemon_type_fighting
            4 -> R.drawable.pokemon_type_poison
            5 -> R.drawable.pokemon_type_ground
            3 -> R.drawable.pokemon_type_flying
            14 -> R.drawable.pokemon_type_psychic
            7 -> R.drawable.pokemon_type_bug
            6 -> R.drawable.pokemon_type_rock
            8 -> R.drawable.pokemon_type_ghost
            16 -> R.drawable.pokemon_type_dragon
            17 -> R.drawable.pokemon_type_dark
            9 -> R.drawable.pokemon_type_steel
            18 -> R.drawable.pokemon_type_fairy
            19 -> R.drawable.pokemon_type_unknown
            10001 -> R.drawable.pokemon_type_unknown
            10002 -> R.drawable.pokemon_type_unknown
            else -> R.drawable.pokemon_type_unknown
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