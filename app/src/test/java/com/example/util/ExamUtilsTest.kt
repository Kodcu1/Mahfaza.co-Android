package com.example.util

import com.example.model.EducationLevel
import org.junit.Assert.*
import org.junit.Test

class ExamUtilsTest {

    @Test
    fun testLgsNetCalculation() {
        // 3 wrong deletes 1 correct
        val net = ExamUtils.calculateNet(correct = 10, wrong = 3, level = EducationLevel.LGS_8TH)
        assertEquals(9.0, net, 0.01)
        
        val net2 = ExamUtils.calculateNet(correct = 20, wrong = 1, level = EducationLevel.LGS_8TH)
        assertEquals(19.67, net2, 0.01)
    }

    @Test
    fun testYksNetCalculation() {
        // 4 wrong deletes 1 correct
        val net = ExamUtils.calculateNet(correct = 10, wrong = 4, level = EducationLevel.YKS_TYT_AYT)
        assertEquals(9.0, net, 0.01)
        
        val net2 = ExamUtils.calculateNet(correct = 40, wrong = 2, level = EducationLevel.YKS_TYT_AYT)
        assertEquals(39.5, net2, 0.01)
    }

    @Test
    fun testTytSubBranchesAndAlternatifFelsefe() {
        val standardTyt = ExamUtils.getTytSubjects(isAlternatifFelsefe = false)
        assertTrue(standardTyt.any { it.first == "Din Kültürü ve Ahlak Bilgisi" })
        assertFalse(standardTyt.any { it.first == "Alternatif Felsefe" })

        val altFelsefeTyt = ExamUtils.getTytSubjects(isAlternatifFelsefe = true)
        assertTrue(altFelsefeTyt.any { it.first == "Alternatif Felsefe" })
        assertFalse(altFelsefeTyt.any { it.first == "Din Kültürü ve Ahlak Bilgisi" })
        
        // Check total TYT question sum
        val totalQuestions = standardTyt.sumOf { it.second }
        assertEquals(120, totalQuestions)
    }

    @Test
    fun testAutoCalculateBlank() {
        val blank = ExamUtils.autoCalculateBlank(maxQ = 40, correct = 30, wrong = 5)
        assertEquals(5, blank)

        val overflowBlank = ExamUtils.autoCalculateBlank(maxQ = 40, correct = 35, wrong = 10)
        assertEquals(0, overflowBlank)
    }

    @Test
    fun testXpPolicyMathAndCap() {
        // Study XP math: base 5 + 1 per 10 Qs, max 25
        assertEquals(5, XpPolicy.calculateStudyXp(0))
        assertEquals(10, XpPolicy.calculateStudyXp(50))
        assertEquals(25, XpPolicy.calculateStudyXp(300)) // Capped at 25 XP

        // Exam XP fixed 15
        assertEquals(15, XpPolicy.calculateExamXp())

        // Pomodoro XP
        assertEquals(3, XpPolicy.calculatePomodoroXp(25))
        assertEquals(6, XpPolicy.calculatePomodoroXp(50))

        // Daily Cap Evaluation
        val uncapped = XpPolicy.evaluateCap(earnedToday = 50, proposedXp = 20)
        assertEquals(20, uncapped.actualAward)
        assertFalse(uncapped.isCapped)

        val partiallyCapped = XpPolicy.evaluateCap(earnedToday = 90, proposedXp = 20)
        assertEquals(10, partiallyCapped.actualAward)
        assertTrue(partiallyCapped.isCapped)

        val fullyCapped = XpPolicy.evaluateCap(earnedToday = 100, proposedXp = 20)
        assertEquals(0, fullyCapped.actualAward)
        assertTrue(fullyCapped.isCapped)
    }
}
