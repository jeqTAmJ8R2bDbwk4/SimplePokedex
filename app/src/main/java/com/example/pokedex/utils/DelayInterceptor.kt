package com.example.pokedex.utils

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber


class DelayInterceptor(private val delayMillis: Long) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            Thread.sleep(delayMillis)
        } catch (e: InterruptedException) {
            Timber.w(e, "Interruption during simulated delay.")
        }
        return chain.proceed(chain.request())
    }
}
