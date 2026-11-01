package com.example.service

import com.example.model.EducationLevel
import com.example.model.Task
import com.example.model.TaskPriority
import java.util.UUID

object LocalPlannerService {

    /**
     * Generates a deterministic Monday-Sunday plan based on student level and priorities.
     */
    fun generateDraft(level: EducationLevel, priorities: List<String>): List<Task> {
        val tasks = mutableListOf<Task>()
        val subjects = when (level) {
            EducationLevel.LGS_8TH -> listOf("Türkçe", "Matematik", "Fen Bilimleri", "İnkılap Tarihi")
            EducationLevel.YKS_TYT_AYT -> listOf("Temel Matematik", "Türkçe", "Fizik", "Biyoloji", "Tarih")
            EducationLevel.KPSS -> listOf("Genel Yetenek (Türkçe, Matematik)", "Genel Kültür (Tarih, Coğrafya, Vatandaşlık)")
        }

        // Simple deterministic logic: 2 tasks per day
        for (day in 1..7) {
            subjects.forEachIndexed { index, subject ->
                if ((day + index) % 3 == 0) {
                    tasks.add(
                        Task(
                            id = UUID.randomUUID().toString(),
                            title = "$subject Çalışması",
                            subject = subject,
                            topic = "Konu Tekrarı ve Soru Çözümü",
                            targetQuestionCount = if (level == EducationLevel.LGS_8TH) 40 else 60,
                            priority = if (priorities.contains(subject)) TaskPriority.HIGH else TaskPriority.MEDIUM,
                            dayOfWeek = day
                        )
                    )
                }
            }
        }
        return tasks
    }
}
