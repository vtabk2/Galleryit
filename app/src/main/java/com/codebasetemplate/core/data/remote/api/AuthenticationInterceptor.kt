package com.codebasetemplate.core.data.remote.api

import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class AuthenticationInterceptor(private val authToken: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original: Request = chain.request()
        val request: Request = original.newBuilder()
            .header("Authorization", authToken)
            .build()
        return try {
            chain.proceed(request)
        } catch (e: Throwable) {
            return Response.Builder()
                .request(original)
                .protocol(Protocol.HTTP_1_1)
                .code(400)
                .message("test_23072024____" + e.message)
                .body("test_23072024__".toResponseBody(null))
                .build()
        }
    }
}