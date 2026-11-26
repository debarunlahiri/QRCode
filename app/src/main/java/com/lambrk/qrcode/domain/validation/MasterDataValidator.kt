package com.lambrk.qrcode.domain.validation

import com.lambrk.qrcode.domain.model.MasterRecord

/** Result of checking a scanned equipment + material pair against the master data. */
sealed interface MasterCheck {
    data class Pass(val record: MasterRecord) : MasterCheck
    data class Fail(val reason: String) : MasterCheck
}

/**
 * Validates scans against master data.
 *
 * 1. Equipment: the scanned code must match an active record's EQP_ID.
 * 2. Material: the QR (16 or 20 chars) is cut at START_DIGIT..END_DIGIT (1-based, inclusive)
 *    and must equal KEY_VALUE_1 of a record belonging to that same equipment.
 *
 * Both conditions must hold on the SAME record, otherwise it fails with a reason.
 * Records are indexed by EQP_ID once, so lookups stay fast with a large master list.
 */
class MasterDataValidator(records: List<MasterRecord>) {

    private val byEqp: Map<String, List<MasterRecord>> = records
        .filter { it.isActive && !it.eqpId.isNullOrBlank() }
        .groupBy { it.eqpId!!.trim() }

    fun validate(scannedEquipment: String, scannedMaterial: String): MasterCheck {
        val eqp = scannedEquipment.trim()
        val material = scannedMaterial.trim()

        val candidates = byEqp[eqp]
            ?: return MasterCheck.Fail("Equipment \"$eqp\" not found in master data.")

        if (material.length !in VALID_QR_LENGTHS) {
            return MasterCheck.Fail(
                "Material QR must be 16 or 20 characters, but it has ${material.length}."
            )
        }

        val misses = mutableListOf<String>()
        for (record in candidates) {
            val key = record.keyValue1?.trim().orEmpty()
            val start = record.startDigit
            val end = record.endDigit
            if (key.isEmpty() || start == null || end == null || start < 1 || end < start) {
                misses += "master record for ${record.item ?: "item"} has an invalid key or digit range"
                continue
            }
            if (end > material.length) {
                misses += "digits $start-$end are outside the ${material.length}-character QR"
                continue
            }
            val extracted = material.substring(start - 1, end)
            if (extracted == key) return MasterCheck.Pass(record)
            misses += "digits $start-$end are \"$extracted\", expected \"$key\""
        }

        return MasterCheck.Fail(
            "Material does not match equipment \"$eqp\": ${misses.distinct().joinToString("; ")}."
        )
    }

    private companion object {
        val VALID_QR_LENGTHS = setOf(16, 20)
    }
}
