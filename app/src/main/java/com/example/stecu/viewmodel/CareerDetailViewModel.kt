package com.example.stecu.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stecu.data.db.AppDatabase
import com.example.stecu.data.model.CareerPlan // Import model UI
import com.example.stecu.data.model.CareerPlanFromJson // Import model JSON
import com.example.stecu.data.model.toUiModel // Import fungsi mapper
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CareerDetailUiState(
    val careerPlan: CareerPlan? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class CareerDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val careerPlanDao = AppDatabase.getDatabase(application).careerPlanDao()
    private val gson = Gson()

    private val _uiState = MutableStateFlow(CareerDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun loadCareerPlan(planId: Long) {
        viewModelScope.launch {
            _uiState.value = CareerDetailUiState(isLoading = true)
            try {
                val entity = careerPlanDao.getCareerPlanById(planId)
                if (entity != null) {
                    // 1. Parse JSON ke model ...FromJson
                    val planFromJson = gson.fromJson(entity.fullJsonData, CareerPlanFromJson::class.java)

                    // 2. Ubah (map) ke model UI menggunakan fungsi toUiModel()
                    val planForUi = planFromJson.toUiModel()

                    // 3. Update state dengan model yang siap digunakan UI
                    _uiState.value = CareerDetailUiState(careerPlan = planForUi, isLoading = false)
                } else {
                    _uiState.value = CareerDetailUiState(error = "Career plan not found", isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = CareerDetailUiState(error = "Failed to load data: ${e.message}", isLoading = false)
            }
        }
    }
}