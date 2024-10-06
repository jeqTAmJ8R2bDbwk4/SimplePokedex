package com.example.pokedex.utils

import androidx.annotation.IntDef
import androidx.annotation.StringDef
import com.example.pokedex.utils.ALL_LIST_ID
import com.example.pokedex.utils.ENGLISH_LANGUAGE_CODE
import com.example.pokedex.utils.ENGLISH_LANGUAGE_ID
import com.example.pokedex.utils.FAVOURITE_LIST_ID
import com.example.pokedex.utils.GERMAN_LANGUAGE_ID
import com.example.pokedex.utils.GERMAN_LENGUAGE_CODE

@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.SOURCE)
@IntDef(GERMAN_LANGUAGE_ID, ENGLISH_LANGUAGE_ID)
annotation class LanguageId

@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.SOURCE)
@StringDef(ENGLISH_LANGUAGE_CODE, GERMAN_LENGUAGE_CODE)
annotation class LanguageCode

@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.SOURCE)
@IntDef(ALL_LIST_ID, FAVOURITE_LIST_ID)
annotation class ListId


@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class NonEmpty