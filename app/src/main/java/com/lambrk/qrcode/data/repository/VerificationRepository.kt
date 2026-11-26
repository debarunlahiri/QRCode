package com.lambrk.qrcode.data.repository

import com.lambrk.qrcode.data.network.toUserMessage
import com.lambrk.qrcode.data.remote.VerifyApi
import com.lambrk.qrcode.data.remote.VerifyRequest
import com.lambrk.qrcode.domain.model.Verdict
import com.lambrk.qrcode.domain.validation.MasterCheck
import com.lambrk.qrcode.domain.validation.MasterDataValidator

/**
 * Uses the backend when configured and reachable; otherwise decides on the device,
 * so the app keeps working with no internet.
 */
class VerificationRepository(private val remote: VerifyApi?) {

    @Volatile
    private var validator: MasterDataValidator? = null

    /** Downloads getMasterData and indexes it. Call once at startup / on refresh. */
    suspend fun refreshMasterData(): Result<Int> = runCatching {
        val records = remote?.getMasterData() ?: error("API not configured")
        validator = MasterDataValidator(records)
        records.size
    }

    suspend fun verify(equipment: String, material: String): Verdict {
        // Master data is the source of truth once loaded: EQP_ID + KEY_VALUE_1 must match one record.
        validator?.let { v ->
            return when (val r = v.validate(equipment, material)) {
                is MasterCheck.Pass -> Verdict(true, null, online = remote != null)
                is MasterCheck.Fail -> Verdict(false, r.reason, online = remote != null)
            }
        }
        var serverError: String? = null
        if (remote != null) {
            try {
                val response = remote.verify(VerifyRequest(equipment, material))
                return Verdict(response.pass, response.message?.ifBlank { null }, online = true)
            } catch (e: Exception) {
                // No wifi / server down / bad response: fall back to the offline check.
                serverError = e.toUserMessage()
            }
        }
        return localCheck(equipment, material).copy(serverError = serverError)
    }

    /** Offline rule. Replace with whatever can be decided without the server. */
    private fun localCheck(equipment: String, material: String): Verdict =
        if (equipment == material)
            Verdict(false, "Material QR is the same as the equipment QR. Please scan the correct material.", false)
        else Verdict(true, null, false)
}
