package com.example.pokedex.utils

import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import kotlin.random.Random

class HttpResponseInterceptor(val code: HttpStatusCode): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return Response.Builder()
            .request(chain.request())
            .protocol(Protocol.HTTP_1_1)
            .code(code.code) // HTTP status code to simulate
            .message("Simulated ${code.code} reponse.")
            .body("Simulated ${code.code} response for debugging".toResponseBody(null))
            .build()
    }
}