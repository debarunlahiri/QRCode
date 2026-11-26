package com.lambrk.qrcode.data.remote

import com.lambrk.qrcode.domain.model.MasterRecord
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * ASSUMED contract (adjust to your real API):
 *   POST verify   {"equipment":"...","material":"..."}
 *   -> {"pass":true|false,"message":"reason"}
 */
interface VerifyApi {
    @POST("verify")
    suspend fun verify(@Body request: VerifyRequest): VerifyResponse

    /** Full master list (EQP_ID, KEY_VALUE_1, START/END_DIGIT, ...). Path is assumed. */
    @GET("getMasterData")
    suspend fun getMasterData(): List<MasterRecord>
}

data class VerifyRequest(val equipment: String, val material: String)

data class VerifyResponse(val pass: Boolean, val message: String? = null)
