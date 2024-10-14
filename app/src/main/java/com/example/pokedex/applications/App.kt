package com.example.pokedex.applications

import android.app.Application
import android.graphics.Bitmap
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.util.DebugLogger
import com.example.pokedex.BuildConfig
import com.example.pokedex.utils.DataSavingInterceptor
import com.example.pokedex.utils.DebugTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class App : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }

    override fun newImageLoader(): ImageLoader {
        return with(ImageLoader.Builder(this)) {
            if (BuildConfig.DEBUG) {
                logger(DebugLogger())
            }
            respectCacheHeaders(false)
            diskCache(
                with(DiskCache.Builder()) {
                    directory(cacheDir.resolve("image_cache"))
                    build()
                }
            )
            allowHardware(false)
            bitmapConfig(Bitmap.Config.ARGB_8888)
            components {
                add(DataSavingInterceptor(applicationContext))
            }
            crossfade(true)
            build()
        }
    }
}