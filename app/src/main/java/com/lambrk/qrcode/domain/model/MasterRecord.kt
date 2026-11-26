package com.lambrk.qrcode.domain.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

/** One row of the getMasterData response. Nullable fields can come back as JSON null. */
data class MasterRecord(
    @SerializedName("EQP_ID") val eqpId: String? = null,
    @SerializedName("MODEL") val model: String? = null,
    @SerializedName("ITEM") val item: String? = null,
    /** 1-based, inclusive position in the scanned material QR where KEY_VALUE_1 starts. */
    @SerializedName("START_DIGIT") @JsonAdapter(LenientIntAdapter::class) val startDigit: Int? = null,
    /** 1-based, inclusive position in the scanned material QR where KEY_VALUE_1 ends. */
    @SerializedName("END_DIGIT") @JsonAdapter(LenientIntAdapter::class) val endDigit: Int? = null,
    @SerializedName("KEY_VALUE_1") val keyValue1: String? = null,
    @SerializedName("UPLOADED_ON") val uploadedOn: String? = null,
    @SerializedName("UPLOADED_BY") val uploadedBy: String? = null,
    @SerializedName("EDITED_BY") val editedBy: String? = null,
    @SerializedName("EDITED_ON") val editedOn: String? = null,
    @SerializedName("USE_FLAG") val useFlag: String? = null,
    @SerializedName("KITTING_ID") val kittingId: String? = null
) {
    val isActive: Boolean get() = useFlag.equals("Y", ignoreCase = true)
}
