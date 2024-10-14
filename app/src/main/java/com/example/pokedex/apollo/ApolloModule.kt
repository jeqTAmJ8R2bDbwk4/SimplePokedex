package com.example.pokedex.apollo

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.okHttpClient
import com.example.pokedex.BuildConfig
import com.example.pokedex.utils.DelayInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object ApolloModule {
    private const val SIMULATED_DELAY = 1000L

    private val loggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    private val delayInterceptor = DelayInterceptor(SIMULATED_DELAY)


    @Singleton
    @Provides
    fun getApolloClient(): ApolloClient {
        val okHttpClient = with(OkHttpClient.Builder()) {
            if (BuildConfig.DEBUG) {
                Timber.w("Simulate delay of %d ms to OkHttpClient.", SIMULATED_DELAY)
                addInterceptor(delayInterceptor)

                addInterceptor(loggingInterceptor)

                // addInterceptor(notFoundInterceptor)
            }
            build()
        }
        return ApolloClient.Builder()
            .serverUrl("https://beta.pokeapi.co/graphql/v1beta")
            .okHttpClient(okHttpClient)
            .build()
    }
}