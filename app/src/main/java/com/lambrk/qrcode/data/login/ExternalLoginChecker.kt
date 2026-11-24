package com.lambrk.qrcode.data.login

import com.lambrk.qrcode.domain.model.LoginState
import kotlinx.coroutines.delay

/**
 * Detects whether the user is logged in through the other app.
 * Implement [check] later (ContentProvider query, bound service, shared account, token, ...).
 * It must return [LoginState.LoggedIn] or [LoginState.NotLoggedIn], never [LoginState.Checking].
 */
fun interface ExternalLoginChecker {
    suspend fun check(): LoginState
}

/** Placeholder until the real integration exists: always reports "not logged in". */
class PlaceholderLoginChecker : ExternalLoginChecker {
    override suspend fun check(): LoginState {
        delay(1200) // simulate the lookup so the "checking" state is visible
        // TODO: ask ExternalAppConfig.PACKAGE_NAME whether a user is signed in.
        return LoginState.NotLoggedIn
    }
}
