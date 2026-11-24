package com.lambrk.qrcode.data.network

import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/** Short, human-readable reason for a network failure. */
fun Throwable.toUserMessage(): String = when (this) {
    is UnknownHostException -> "server address not found"
    is SocketTimeoutException -> "the server took too long to answer"
    is ConnectException -> "connection refused"
    is SSLException -> "secure connection problem"
    is HttpException -> "server error (HTTP ${code()})"
    else -> message?.takeIf { it.isNotBlank() } ?: "unknown error"
}
