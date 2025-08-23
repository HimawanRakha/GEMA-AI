package com.example.stecu.data.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

// === BAGIAN 1: DATA CLASS UNTUK PARSING JSON (Cocok dengan struktur JSON) ===

data class CareerPlanFromJson(
    val goal: String,
    val milestones: List<MilestoneFromJson>
)

data class MilestoneFromJson(
    val id: String,
    val title: String,
    val duration_weeks: Int,
    val quests: List<QuestFromJson>
)

data class QuestFromJson(
    val id: String,
    val title: String,
    val steps: List<String>, // Perhatikan: ini adalah List<String>
    val resources: List<Resource>
)


// === BAGIAN 2: DATA CLASS UNTUK STATE UI (Yang digunakan oleh Composable) ===

data class CareerPlan(
    val goal: String,
    val milestones: List<Milestone>
)

data class Milestone(
    val id: String,
    val title: String,
    val duration_weeks: Int,
    val quests: List<Quest>
)

data class Quest(
    val id: String,
    val title: String,
    val steps: List<CheckableStep>, // Perhatikan: ini adalah List<CheckableStep>
    val resources: List<Resource>
)

// Ini adalah data class yang sudah ada di CareerDetailScreen.kt, kita pindahkan ke sini
data class CheckableStep(
    val text: String,
    val isChecked: MutableState<Boolean> = mutableStateOf(false)
)

// Resource bisa digunakan oleh keduanya
data class Resource(
    val title: String,
    val url: String
)


// === BAGIAN 3: FUNGSI MAPPER (Untuk mengubah dari model JSON ke model UI) ===

fun CareerPlanFromJson.toUiModel(): CareerPlan {
    return CareerPlan(
        goal = this.goal,
        milestones = this.milestones.map { milestoneFromJson ->
            Milestone(
                id = milestoneFromJson.id,
                title = milestoneFromJson.title,
                duration_weeks = milestoneFromJson.duration_weeks,
                quests = milestoneFromJson.quests.map { questFromJson ->
                    Quest(
                        id = questFromJson.id,
                        title = questFromJson.title,
                        // Di sinilah konversi penting terjadi!
                        steps = questFromJson.steps.map { stepText -> CheckableStep(stepText) },
                        resources = questFromJson.resources
                    )
                }
            )
        }
    )
}