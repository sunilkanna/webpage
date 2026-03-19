package com.simats.genecare.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.genecare.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SystemSettingsViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _settings = MutableStateFlow<Map<String, String>>(emptyMap())
    val settings: StateFlow<Map<String, String>> = _settings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getSystemSettings()
                if (response.isSuccessful && response.body()?.status == "success") {
                    _settings.value = response.body()?.settings ?: emptyMap()
                } else {
                    _error.value = "Failed to load settings"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSetting(key: String, value: Boolean) {
        val stringValue = if (value) "1" else "0"
        updateStringSetting(key, stringValue)
    }

    fun updateStringSetting(key: String, value: String) {
        viewModelScope.launch {
            try {
                // Optimistically update UI
                val currentSettings = _settings.value.toMutableMap()
                currentSettings[key] = value
                _settings.value = currentSettings

                val response = repository.updateSystemSetting(key, value)
                if (!response.isSuccessful || response.body()?.status != "success") {
                    // Revert if failed
                    loadSettings()
                    _error.value = "Failed to update setting"
                }
            } catch (e: Exception) {
                loadSettings()
                _error.value = e.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
