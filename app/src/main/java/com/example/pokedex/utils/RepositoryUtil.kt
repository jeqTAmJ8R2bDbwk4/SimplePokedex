package com.example.pokedex.utils

import androidx.paging.PagingSource
import java.util.Locale
import kotlin.math.max

object RepositoryUtil {
    fun <E : Any> getLoadResult(count: Int, offset: Int, data: List<E>): PagingSource.LoadResult<Int, E> {
        val itemsAfter = count - (offset + data.size)
        val prevKey = if (offset == 0) null else max(offset - data.size, 0)
        val nextKey = if (itemsAfter == 0) null else offset + data.size

        return PagingSource.LoadResult.Page(
            data = data,
            prevKey = prevKey,
            nextKey = nextKey,
            itemsBefore = offset,
            itemsAfter = itemsAfter,
        )
    }

    fun getRefreshKey(prevKey: Int?, nextKey: Int?, pageSize: Int): Int? {
        return prevKey?.let { key -> key + pageSize } ?: nextKey?.let { key -> key - pageSize }
    }

    fun sanitizeSearchQuery(searchQuery: String): String {
        return searchQuery.replace("%", "").trim()
    }

    @Throws(IllegalArgumentException::class)
    fun languageIdToLocale(languageId: @LanguageId Int): Locale {
        return when(languageId) {
            GERMAN_LANGUAGE_ID -> Locale.GERMAN
            ENGLISH_LANGUAGE_ID -> Locale.ENGLISH
            else -> throw IllegalArgumentException()
        }
    }
}