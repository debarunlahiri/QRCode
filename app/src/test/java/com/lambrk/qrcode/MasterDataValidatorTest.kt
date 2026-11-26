package com.lambrk.qrcode

import com.lambrk.qrcode.domain.model.MasterRecord
import com.lambrk.qrcode.domain.validation.MasterCheck
import com.lambrk.qrcode.domain.validation.MasterDataValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MasterDataValidatorTest {

    private val validator = MasterDataValidator(
        listOf(
            MasterRecord("EQP001", "MODEL_A", "ITEM001", 1, 8, "ABC12345", useFlag = "Y"),
            MasterRecord("EQP002", "MODEL_B", "ITEM002", 2, 10, "XYZ123456", useFlag = "Y"),
            MasterRecord("EQP003", "MODEL_C", "ITEM003", 3, 8, "DEF123", useFlag = "N")
        )
    )

    @Test
    fun passes_when_equipment_and_key_match_same_record() {
        // 16 chars, digits 1-8 = ABC12345
        val result = validator.validate("EQP001", "ABC12345ZZZZZZZZ")
        assertTrue(result is MasterCheck.Pass)
    }

    @Test
    fun passes_with_20_char_qr_and_offset_range() {
        // digits 2-10 = XYZ123456
        val result = validator.validate("EQP002", "9XYZ123456ZZZZZZZZZZ")
        assertTrue(result is MasterCheck.Pass)
    }

    @Test
    fun fails_when_key_belongs_to_another_equipment() {
        val result = validator.validate("EQP002", "ABC12345ZZZZZZZZ")
        assertTrue(result is MasterCheck.Fail)
    }

    @Test
    fun fails_for_unknown_equipment() {
        val result = validator.validate("NOPE", "ABC12345ZZZZZZZZ") as MasterCheck.Fail
        assertTrue(result.reason.contains("not found"))
    }

    @Test
    fun fails_for_inactive_record() {
        assertTrue(validator.validate("EQP003", "ZZDEF123ZZZZZZZZ") is MasterCheck.Fail)
    }

    @Test
    fun fails_for_wrong_qr_length() {
        val result = validator.validate("EQP001", "ABC12345") as MasterCheck.Fail
        assertEquals(true, result.reason.contains("16 or 20"))
    }
}
