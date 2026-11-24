package com.lambrk.qrcode.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * ASSUMED contract (adjust to your real API):
 *   POST verify   {"equipment":"...","material":"..."}
 *   -> {"pass":true|false,"message":"reason"}
 */
interface VerifyApi {
    @POST("verify")
    suspend fun verify(@Body request: VerifyRequest): VerifyResponse
}

data class VerifyRequest(val equipment: String, val material: String)

data class VerifyResponse(val pass: Boolean, val message: String? = null)
