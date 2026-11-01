package com.example.util

import kotlin.math.min

object XpPolicy {
    const val DAILY_MAX_XP = 100
    const val BASE_STUDY_XP = 5
    const val MAX_STUDY_XP = 25
    const val EXAM_XP = 15
    const val POMODORO_XP = 3

    /**
     * Calculates estimated XP for study question submission.
     * Base 5 XP + 1 XP per 10 questions, max 25 XP.
     */
    fun calculateStudyXp(questionCount: Int): Int {
        val q = questionCount.coerceAtLeast(0)
        val bonus = q / 10
        return (BASE_STUDY_XP + bonus).coerceAtMost(MAX_STUDY_XP)
    }

    /**
     * Calculates estimated XP for saving an exam result.
     * Fixed 15 XP.
     */
    fun calculateExamXp(): Int = EXAM_XP

    /**
     * Calculates estimated XP for completing a Pomodoro session.
     * 3 XP per 25-minute completed session.
     */
    fun calculatePomodoroXp(durationMinutes: Int): Int {
        val sessions = durationMinutes / 25
        return if (sessions >= 1) sessions * POMODORO_XP else if (durationMinutes >= 15) POMODORO_XP else 0
    }

    /**
     * Calculates estimated XP for task completion.
     * Task's defined XP reward (5 to 50 XP).
     */
    fun calculateTaskXp(taskXp: Int): Int = taskXp.coerceIn(5, 50)

    data class XpCapStatus(
        val proposedXp: Int,
        val actualAward: Int,
        val earnedToday: Int,
        val remainingCap: Int,
        val isCapped: Boolean,
        val explanationMessage: String
    )

    fun evaluateCap(earnedToday: Int, proposedXp: Int): XpCapStatus {
        val currentEarned = earnedToday.coerceAtLeast(0)
        val remaining = (DAILY_MAX_XP - currentEarned).coerceAtLeast(0)
        val actual = min(proposedXp, remaining)
        val isCapped = actual < proposedXp

        val message = if (remaining == 0) {
            "Günlük maksimum XP sınırına ulaştın ($DAILY_MAX_XP/$DAILY_MAX_XP XP). Çalışmaların kaydedilmeye devam eder ancak bugün daha fazla XP eklenmez."
        } else if (isCapped) {
            "Günlük $DAILY_MAX_XP XP sınırına yaklaşılıyor ($currentEarned/$DAILY_MAX_XP XP). Bu işlemden +$actual XP kazanacaksın."
        } else {
            "Bu işlemden +$actual XP kazanacaksın (Günlük: $currentEarned/$DAILY_MAX_XP XP)."
        }

        return XpCapStatus(
            proposedXp = proposedXp,
            actualAward = actual,
            earnedToday = currentEarned,
            remainingCap = remaining,
            isCapped = isCapped,
            explanationMessage = message
        )
    }
}
