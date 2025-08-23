package com.example.stecu.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stecu.data.db.AppDatabase
import com.example.stecu.data.db.CareerPlanEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CareerReportViewModel(application: Application): AndroidViewModel(application) {
    private val careerPlanDao = AppDatabase.getDatabase(application).careerPlanDao()

    val careerPlans: StateFlow<List<CareerPlanEntity>> = careerPlanDao.getAllCareerPlans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteCareerPlan(planId: Long) {
        viewModelScope.launch {
            careerPlanDao.deleteCareerPlanById(planId)
        }
    }
}