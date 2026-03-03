package com.simats.genecare.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.genecare.data.UserSession
import com.simats.genecare.data.model.SaveCounselorProfileRequest
import com.simats.genecare.data.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class EditProfileState(
    val isLoading: Boolean = true,
    val fullName: String = "",
    val specialization: String = "",
    val bio: String = "",
    val experienceYears: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class CounselorEditProfileViewModel : ViewModel() {

    private val _state = MutableStateFlow(EditProfileState())
    val state: StateFlow<EditProfileState> = _state

    init {
        loadProfile()
    }

    fun loadProfile() {
        val user = UserSession.getUser() ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = ApiClient.api.getCounselorProfile(user.id)
                if (response.isSuccessful && response.body()?.status == "success") {
                    val profile = response.body()?.profile
                    _state.value = _state.value.copy(
                        isLoading = false,
                        fullName = profile?.fullName ?: user.fullName,
                        specialization = profile?.specialization ?: "",
                        bio = profile?.bio ?: "",
                        experienceYears = profile?.experienceYears?.toString() ?: ""
                    )
                } else {
                    // No profile yet, pre-fill with user name
                    _state.value = _state.value.copy(
                        isLoading = false,
                        fullName = user.fullName
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    fullName = user.fullName,
                    errorMessage = "Could not load profile: ${e.localizedMessage}"
                )
            }
        }
    }

    fun updateFullName(value: String) {
        _state.value = _state.value.copy(fullName = value)
    }

    fun updateSpecialization(value: String) {
        _state.value = _state.value.copy(specialization = value)
    }

    fun updateBio(value: String) {
        _state.value = _state.value.copy(bio = value)
    }

    fun updateExperienceYears(value: String) {
        _state.value = _state.value.copy(experienceYears = value)
    }

    fun saveProfile() {
        val user = UserSession.getUser() ?: return
        val s = _state.value

        if (s.fullName.isBlank() || s.specialization.isBlank()) {
            _state.value = s.copy(errorMessage = "Name and specialization are required")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, errorMessage = null, saveSuccess = false)
            try {
                val request = SaveCounselorProfileRequest(
                    userId = user.id,
                    specialization = s.specialization,
                    bio = s.bio,
                    experienceYears = s.experienceYears.toIntOrNull() ?: 0,
                    consultationFee = 0.0,
                    profileImageUrl = null
                )
                val response = ApiClient.api.saveCounselorProfile(request)
                if (response.isSuccessful && response.body()?.status == "success") {
                    // Also update the full name in users table
                    val nameRequest = com.simats.genecare.data.model.ProfileUpdateRequest(
                        userId = user.id,
                        fullName = s.fullName,
                        dateOfBirth = "",
                        gender = "",
                        phone = "",
                        address = ""
                    )
                    ApiClient.api.updateProfile(nameRequest)

                    _state.value = _state.value.copy(isSaving = false, saveSuccess = true)
                } else {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        errorMessage = response.body()?.message ?: "Save failed"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false,
                    errorMessage = "Error: ${e.localizedMessage}"
                )
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(errorMessage = null, saveSuccess = false)
    }
}
