package com.lambrk.qrcode.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lambrk.qrcode.QRCodeApp
import com.lambrk.qrcode.data.login.ExternalLoginChecker
import com.lambrk.qrcode.domain.model.LoginState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val checker: ExternalLoginChecker) : ViewModel() {
    private val _state = MutableStateFlow<LoginState>(LoginState.Checking)
    val state: StateFlow<LoginState> = _state.asStateFlow()
    private var job: Job? = null

    init {
        check()
    }

    fun check() {
        job?.cancel()
        _state.value = LoginState.Checking
        job = viewModelScope.launch { _state.value = checker.check() }
    }

    /** Called when the user comes back from the other app. */
    fun recheckIfNotLoggedIn() {
        if (_state.value is LoginState.NotLoggedIn) check()
    }

    /** Development builds only (the button is hidden in production). */
    fun continueWithoutLogin() {
        job?.cancel()
        _state.value = LoginState.LoggedIn()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                LoginViewModel((this[APPLICATION_KEY] as QRCodeApp).container.loginChecker)
            }
        }
    }
}
