package com.lambrk.qrcode.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lambrk.qrcode.QRCodeApp
import androidx.lifecycle.viewModelScope
import com.lambrk.qrcode.data.repository.VerificationRepository
import com.lambrk.qrcode.domain.model.ScanStep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScanUiState(
    val step: ScanStep = ScanStep.EQUIPMENT,
    val equipmentCode: String? = null,
    val materialCode: String? = null,
    val error: String? = null,
    val verifying: Boolean = false,
    /** null = pass, otherwise the failure message. Only meaningful when step == DONE. */
    val failureMessage: String? = null,
    val checkedOnline: Boolean = false,
    /** Set when the server call failed during verification; shown once in a dialog. */
    val serverError: String? = null,
    /** True when the material QR scanned is the same as the equipment QR; shown as a dialog. */
    val sameQrDialog: Boolean = false
) {
    val passed get() = step == ScanStep.DONE && failureMessage == null
}

class ScanViewModel(private val repository: VerificationRepository) : ViewModel() {
    private val _state = MutableStateFlow(ScanUiState())
    val state: StateFlow<ScanUiState> = _state.asStateFlow()

    /** Returns true if the scan was accepted. Scanned text may be any length. */
    fun onScanned(raw: String): Boolean {
        val s = _state.value
        if (s.verifying || s.sameQrDialog ||
            (s.step != ScanStep.EQUIPMENT && s.step != ScanStep.MATERIAL)
        ) return false
        val value = raw.trim()
        if (value.isEmpty()) {
            _state.update { it.copy(error = "Empty QR code") }
            return false
        }
        if (s.step == ScanStep.MATERIAL && value == s.equipmentCode) {
            _state.update { it.copy(sameQrDialog = true, error = null) }
            return false
        }
        when (s.step) {
            // Camera closes here; the user starts the material scan from the next screen.
            ScanStep.EQUIPMENT ->
                _state.update {
                    it.copy(
                        equipmentCode = value,
                        step = ScanStep.EQUIPMENT_DONE,
                        error = null
                    )
                }

            else -> {
                _state.update { it.copy(materialCode = value, verifying = true, error = null) }
                val equipment = s.equipmentCode.orEmpty()
                viewModelScope.launch {
                    val verdict = repository.verify(equipment, value)
                    _state.update {
                        it.copy(
                            verifying = false,
                            step = ScanStep.DONE,
                            checkedOnline = verdict.online,
                            serverError = verdict.serverError,
                            failureMessage = if (verdict.pass) null else verdict.message
                                ?: "Verification failed"
                        )
                    }
                }
            }
        }
        return true
    }

    fun startMaterialScan() {
        _state.update {
            if (it.step == ScanStep.EQUIPMENT_DONE) it.copy(
                step = ScanStep.MATERIAL,
                error = null
            ) else it
        }
    }

    fun backToEquipment() {
        _state.update {
            if (it.step == ScanStep.MATERIAL || it.step == ScanStep.EQUIPMENT_DONE)
                it.copy(step = ScanStep.EQUIPMENT, equipmentCode = null, error = null)
            else it
        }
    }

    fun dismissSameQrDialog() {
        _state.update { it.copy(sameQrDialog = false) }
    }

    fun dismissServerError() {
        _state.update { it.copy(serverError = null) }
    }

    fun reset() {
        _state.value = ScanUiState()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as QRCodeApp
                ScanViewModel(app.container.verificationRepository)
            }
        }
    }
}
