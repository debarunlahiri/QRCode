package com.lambrk.qrcode.domain.model

/** Outcome of the final equipment + material check. [online] = answered by the backend. */
data class Verdict(
    val pass: Boolean,
    val message: String?,
    val online: Boolean,
    /** Set when the server call failed and the result was decided offline instead. */
    val serverError: String? = null
)
