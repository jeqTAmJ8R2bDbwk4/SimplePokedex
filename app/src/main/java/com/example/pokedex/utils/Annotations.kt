package com.example.pokedex.utils

import androidx.annotation.IntDef
import androidx.annotation.StringDef

@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.SOURCE)
@IntDef(GERMAN_LANGUAGE_ID, ENGLISH_LANGUAGE_ID)
annotation class LanguageId

@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.SOURCE)
@StringDef(ENGLISH_LANGUAGE_CODE, GERMAN_LENGUAGE_CODE)
annotation class LanguageCode


@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class NonEmpty