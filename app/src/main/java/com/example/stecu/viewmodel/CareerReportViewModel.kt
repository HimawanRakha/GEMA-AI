package com.example.stecu.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stecu.data.db.AppDatabase
import com.example.stecu.data.db.CareerPlanEntity
import com.example.stecu.data.model.CareerPlan
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CareerReportViewModel(application: Application): AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).careerPlanDao()
    private val gson = Gson()

    private val _careerPlans = MutableStateFlow<List<CareerPlanEntity>>(emptyList())
    val careerPlans = _careerPlans.asStateFlow()

    init {
        viewModelScope.launch {
            // Kita gunakan .map untuk mentransformasi data yang datang dari database
            dao.getAllCareerPlans().map { entities ->
                // Untuk setiap list entitas yang kita dapat...
                entities.map { entity ->
                    // 1. Parse JSON menjadi objek CareerPlan
                    val plan = try {
                        gson.fromJson(entity.fullJsonData, CareerPlan::class.java)
                    } catch (e: Exception) {
                        null
                    }

                    // 2. Jika parsing berhasil, hitung progress dan masukkan ke properti @Ignore
                    if (plan != null) {
                        entity.progress = plan.calculateProgress()
                    }

                    // 3. Kembalikan entitas yang sudah diisi progress-nya
                    entity
                }
            }.collect { plansWithProgress ->
                // Kirim list yang sudah lengkap dengan data progress ke UI
                _careerPlans.value = plansWithProgress
            }
        }
    }


    fun deleteCareerPlan(planId: Long) {
        viewModelScope.launch {
            dao.deleteCareerPlanById(planId)
        }
    }
}