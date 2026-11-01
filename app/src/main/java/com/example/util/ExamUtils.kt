package com.example.util

import com.example.model.EducationLevel
import com.example.model.SubjectResult
import kotlin.math.roundToInt

object ExamUtils {

    /**
     * Calculates net score based on correct and wrong answers.
     * LGS: 3 wrong deletes 1 correct (1/3 penalty)
     * YKS (TYT/AYT) & KPSS: 4 wrong deletes 1 correct (1/4 penalty)
     */
    fun calculateNet(correct: Int, wrong: Int, level: EducationLevel): Double {
        val penaltyFactor = when (level) {
            EducationLevel.LGS_8TH -> 3.0
            EducationLevel.YKS_TYT_AYT -> 4.0
            EducationLevel.KPSS -> 4.0
        }
        val net = correct - (wrong / penaltyFactor)
        return (net * 100.0).roundToInt() / 100.0
    }

    fun autoCalculateBlank(maxQ: Int, correct: Int, wrong: Int): Int {
        return (maxQ - correct - wrong).coerceAtLeast(0)
    }

    fun validateInputs(correct: Int, wrong: Int, blank: Int, max: Int): Boolean {
        if (correct < 0 || wrong < 0 || blank < 0) return false
        if (correct + wrong + blank > max) return false
        return true
    }

    // LGS Verbal (Sözel) Subjects (Total: 50 Questions)
    val LGS_SOZEL_SUBJECTS = listOf(
        "Türkçe" to 20,
        "T.C. İnkılap Tarihi ve Atatürkçülük" to 10,
        "Din Kültürü ve Ahlak Bilgisi" to 10,
        "Yabancı Dil (İngilizce)" to 10
    )

    // LGS Numerical (Sayısal) Subjects (Total: 40 Questions)
    val LGS_SAYISAL_SUBJECTS = listOf(
        "Matematik" to 20,
        "Fen Bilimleri" to 20
    )

    fun getLgsSubjects() = LGS_SOZEL_SUBJECTS + LGS_SAYISAL_SUBJECTS

    fun getTytSubjects(isAlternatifFelsefe: Boolean = false) = listOf(
        "Türkçe" to 40,
        "Temel Matematik" to 40,
        "Tarih" to 5,
        "Coğrafya" to 5,
        "Felsefe" to 5,
        (if (isAlternatifFelsefe) "Alternatif Felsefe" else "Din Kültürü ve Ahlak Bilgisi") to 5,
        "Fizik" to 7,
        "Kimya" to 7,
        "Biyoloji" to 6
    )

    fun getAytSubjects() = listOf(
        "Matematik" to 40,
        "Fizik" to 14,
        "Kimya" to 13,
        "Biyoloji" to 13,
        "Türk Dili ve Edebiyatı" to 24,
        "Tarih-1" to 10,
        "Coğrafya-1" to 6,
        "Tarih-2" to 11,
        "Coğrafya-2" to 11,
        "Felsefe Grubu" to 12,
        "Din Kültürü / Ek Felsefe" to 6
    )

    // AYT Preset Filters (Track Categories)
    val AYT_SAYISAL_SUBJECTS = listOf("Matematik", "Fizik", "Kimya", "Biyoloji")
    val AYT_ESIT_AGIRLIK_SUBJECTS = listOf("Matematik", "Türk Dili ve Edebiyatı", "Tarih-1", "Coğrafya-1")
    val AYT_SOZEL_SUBJECTS = listOf("Türk Dili ve Edebiyatı", "Tarih-1", "Coğrafya-1", "Tarih-2", "Coğrafya-2", "Felsefe Grubu", "Din Kültürü / Ek Felsefe")

    fun getKpssSubjects() = listOf(
        "Genel Yetenek (Türkçe - Matematik)" to 60,
        "Genel Kültür (Tarih - Coğrafya - Vatandaşlık)" to 60
    )
}
