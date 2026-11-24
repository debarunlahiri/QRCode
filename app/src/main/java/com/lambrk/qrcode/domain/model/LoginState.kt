package com.lambrk.qrcode.domain.model

sealed interface LoginState {
    data object Checking : LoginState
    data class LoggedIn(val displayName: String? = null) : LoginState
    data object NotLoggedIn : LoginState
}
