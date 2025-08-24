package com.example.stecu.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stecu.data.db.AppDatabase
import com.example.stecu.data.db.CareerPlanEntity
import com.example.stecu.data.model.CareerPlan
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Data class untuk merepresentasikan semua state di UI
data class CareerDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val careerPlan: CareerPlan? = null, // Hasil parsing JSON untuk ditampilkan
    val careerPlanEntity: CareerPlanEntity? = null // Entitas asli dari DB untuk update
)

class CareerDetailViewModel(application: Application) : ViewModel() {

    private val careerPlanDao = AppDatabase.getDatabase(application).careerPlanDao()
    private val gson = Gson()

    private val _uiState = MutableStateFlow(CareerDetailUiState())
    val uiState: StateFlow<CareerDetailUiState> = _uiState.asStateFlow()

    fun loadCareerPlan(id: Long) {
        viewModelScope.launch {
            _uiState.value = CareerDetailUiState(isLoading = true)
            try {
                val entity = careerPlanDao.getCareerPlanById(id)
                if (entity != null) {
                    val plan = gson.fromJson(entity.fullJsonData, CareerPlan::class.java)
                    _uiState.value = CareerDetailUiState(
                        isLoading = false,
                        careerPlan = plan,
                        careerPlanEntity = entity
                    )
                } else {
                    _uiState.value = CareerDetailUiState(isLoading = false, error = "Rencana karir tidak ditemukan.")
                }
            } catch (e: Exception) {
                _uiState.value = CareerDetailUiState(isLoading = false, error = e.message ?: "Terjadi error")
            }
        }
    }

    fun updateStepCheckedState(questId: String, stepText: String, isChecked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _uiState.value
            val currentPlan = currentState.careerPlan ?: return@launch

            val updatedMilestones = currentPlan.milestones.map { milestone ->
                milestone.copy(
                    quests = milestone.quests.map { quest ->
                        if (quest.id == questId) {
                            quest.copy(
                                steps = quest.steps.map { step ->
                                    if (step.text == stepText) {
                                        step.copy(isChecked = isChecked)
                                    } else {
                                        step
                                    }
                                }
                            )
                        } else {
                            quest
                        }
                    }
                )
            }

            val updatedPlan = currentPlan.copy(milestones = updatedMilestones)

            // Update UI State di Main thread agar UI langsung merespon
            withContext(Dispatchers.Main) {
                _uiState.value = currentState.copy(careerPlan = updatedPlan)
            }

            val newJsonData = gson.toJson(updatedPlan)

            // Ambil entitas saat ini dari state, yang sekarang sudah pasti ada
            val currentEntity = currentState.careerPlanEntity ?: return@launch
            val updatedEntity = currentEntity.copy(fullJsonData = newJsonData)

            careerPlanDao.update(updatedEntity)
        }
    }
}