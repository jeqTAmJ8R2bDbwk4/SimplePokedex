package com.example.pokedex.applications

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.util.DebugLogger
import com.example.pokedex.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class App : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun newImageLoader(): ImageLoader {
        return with(ImageLoader.Builder(this)) {
            logger(DebugLogger())
            respectCacheHeaders(false)
            diskCache(
                with(DiskCache.Builder()) {
                    directory(cacheDir.resolve("image_cache"))
                    maxSizePercent(0.02)
                    build()
                }
            )
            crossfade(true)
            build()
        }
    }
}