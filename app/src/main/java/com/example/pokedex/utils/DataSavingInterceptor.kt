package com.example.pokedex.utils

import android.content.Context
import android.net.Uri
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import coil.intercept.Interceptor
import coil.request.ImageResult
import com.example.pokedex.R
import timber.log.Timber

class DataSavingInterceptor(
    private val context: Context
): Interceptor {
    @OptIn(ExperimentalCoilApi::class)
    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val request = chain.request
        val sharedPreferenceSettings = context.getSharedSettingsPreferences()
        val preferenceDataSavingKey = context.getString(R.string.preference_data_saving_key)
        val handleDataSaving = sharedPreferenceSettings.getBoolean(preferenceDataSavingKey, true)

        if (!handleDataSaving) {
            return chain.proceed(request)
        }

        val url = request.data.toString()
        val redirectUrlMediumCompression = Uri.Builder()
            .scheme("https")
            .authority("wsrv.nl")
            .appendPath("")
            .appendQueryParameter("output", "webp")
            .appendQueryParameter("q", "80")
            .appendQueryParameter("url", url)
            .build()
            .toString()
        val redirectedRequestMediumCompression = request.newBuilder()
            .diskCacheKey(redirectUrlMediumCompression)
            .memoryCacheKey(redirectUrlMediumCompression)
            .data(redirectUrlMediumCompression)
            .build()

        if (!context.isDataSaving()) {
            return chain.proceed(redirectedRequestMediumCompression)
        }

        if (context.imageLoader.isCacheHit(url)) {
            Timber.i("%s is already cached.", url)
            return chain.proceed(request)
        }

        if (context.imageLoader.isCacheHit(redirectUrlMediumCompression)) {
            Timber.i("%s is already cached.", redirectUrlMediumCompression)
            return chain.proceed(redirectedRequestMediumCompression)
        }

        val redirectUrlHighCompression = Uri.Builder()
            .scheme("https")
            .authority("wsrv.nl")
            .appendPath("")
            .appendQueryParameter("output", "webp")
            .appendQueryParameter("q", "5")
            .appendQueryParameter("url", url)
            .build()
            .toString()
        val redirectedRequestHighCompression = request.newBuilder()
            .diskCacheKey(redirectUrlHighCompression)
            .memoryCacheKey(redirectUrlHighCompression)
            .data(redirectUrlHighCompression)
            .build()
        return chain.proceed(redirectedRequestHighCompression)
    }
}