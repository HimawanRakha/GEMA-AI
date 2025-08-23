package com.example.stecu.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stecu.data.db.CertificateEntity
import com.example.stecu.data.repository.CertificateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AmberPointsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CertificateRepository(application)

    private val _certificates = MutableStateFlow<List<CertificateEntity>>(emptyList())
    val certificates = _certificates.asStateFlow()

    // State untuk mengelola UI (misal: loading)
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing = _isProcessing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()


    init {
        loadCertificates()
    }

    private fun loadCertificates() {
        viewModelScope.launch {
            repository.allCertificates
                .catch { e -> _errorMessage.value = "Failed to load certificates: ${e.message}" }
                .collect { certList ->
                    _certificates.value = certList
                }
        }
    }

    fun addCertificate(uri: Uri) {
        viewModelScope.launch {
            _isProcessing.value = true
            _errorMessage.value = null
            val result = repository.processAndSaveCertificate(uri)
            result.onFailure {
                _errorMessage.value = "Failed to process certificate: ${it.message}"
            }
            _isProcessing.value = false
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun deleteCertificate(id: Int) {
        viewModelScope.launch {
            repository.deleteCertificate(id)
        }
    }
}