package com.lambrk.qrcode.domain.model

/** Something wrong with connectivity that the user should be told about. */
sealed interface ConnectionIssue {
    /** Phone is not on Wi-Fi. */
    data object NoWifi : ConnectionIssue

    /** Wi-Fi is up but the backend did not answer the health check. */
    data class ServerUnreachable(val reason: String) : ConnectionIssue

    /** The final verification call failed, so the result was decided offline. */
    data class VerifyFailed(val reason: String) : ConnectionIssue
}
