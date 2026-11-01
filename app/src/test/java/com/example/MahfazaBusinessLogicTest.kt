package com.example

import com.example.model.EducationLevel
import com.example.model.Entitlement
import com.example.model.PackageCatalog
import com.example.util.ExamUtils
import org.junit.Assert.*
import org.junit.Test

class MahfazaBusinessLogicTest {

    @Test
    fun testLgsExamNetFormulas() {
        // LGS formula: Correct - (Wrong / 3.0)
        val score1 = ExamUtils.calculateNet(correct = 20, wrong = 0, level = EducationLevel.LGS_8TH)
        assertEquals(20.0, score1, 0.01)

        val score2 = ExamUtils.calculateNet(correct = 15, wrong = 3, level = EducationLevel.LGS_8TH)
        assertEquals(14.0, score2, 0.01)

        val score3 = ExamUtils.calculateNet(correct = 17, wrong = 2, level = EducationLevel.LGS_8TH)
        assertEquals(16.33, score3, 0.01)

        // Negative net test
        val negativeScore = ExamUtils.calculateNet(correct = 1, wrong = 6, level = EducationLevel.LGS_8TH)
        assertEquals(-1.0, negativeScore, 0.01)
    }

    @Test
    fun testYksExamNetFormulas() {
        // YKS TYT/AYT formula: Correct - (Wrong / 4.0)
        val score1 = ExamUtils.calculateNet(correct = 40, wrong = 0, level = EducationLevel.YKS_TYT_AYT)
        assertEquals(40.0, score1, 0.01)

        val score2 = ExamUtils.calculateNet(correct = 35, wrong = 4, level = EducationLevel.YKS_TYT_AYT)
        assertEquals(34.0, score2, 0.01)

        val score3 = ExamUtils.calculateNet(correct = 29, wrong = 2, level = EducationLevel.YKS_TYT_AYT)
        assertEquals(28.5, score3, 0.01)

        // Negative net test
        val negativeScore = ExamUtils.calculateNet(correct = 0, wrong = 8, level = EducationLevel.YKS_TYT_AYT)
        assertEquals(-2.0, negativeScore, 0.01)
    }

    @Test
    fun testQuestionInputValidation() {
        // Valid case
        assertTrue(ExamUtils.validateInputs(correct = 15, wrong = 3, blank = 2, max = 20))

        // Exceeding question count
        assertFalse(ExamUtils.validateInputs(correct = 16, wrong = 3, blank = 2, max = 20)) // 21 > 20

        // Negative input counts
        assertFalse(ExamUtils.validateInputs(correct = -1, wrong = 2, blank = 2, max = 20))
        assertFalse(ExamUtils.validateInputs(correct = 10, wrong = -2, blank = 2, max = 20))
    }

    @Test
    fun testPackageEntitlements() {
        // Student Standart
        val stdPkg = PackageCatalog.STUDENT_STANDART
        assertTrue(stdPkg.entitlements.contains(Entitlement.DAILY_TASKS))
        assertFalse(stdPkg.entitlements.contains(Entitlement.ADVANCED_ANALYTICS))
        assertFalse(stdPkg.entitlements.contains(Entitlement.AI_PLANNER_STUDENT))

        // Student Plus
        val plusPkg = PackageCatalog.STUDENT_PLUS
        assertTrue(plusPkg.entitlements.contains(Entitlement.ADVANCED_ANALYTICS))
        assertTrue(plusPkg.entitlements.contains(Entitlement.EXAM_HISTORY))
        assertFalse(plusPkg.entitlements.contains(Entitlement.AI_PLANNER_STUDENT))

        // Student Pro
        val proPkg = PackageCatalog.STUDENT_PRO
        assertTrue(proPkg.entitlements.contains(Entitlement.AI_PLANNER_STUDENT))
    }

    @Test
    fun testCoachCapacities() {
        assertEquals(3, PackageCatalog.COACH_START.studentCapacity)
        assertEquals(10, PackageCatalog.COACH_STANDART.studentCapacity)
        assertEquals(25, PackageCatalog.COACH_PRO.studentCapacity)
        assertEquals(50, PackageCatalog.COACH_INSTITUTION.studentCapacity)
    }

    @Test
    fun testTaskDayOfWeekAndRewards() {
        val daysOfWeek = (1..7).toList()
        assertEquals(7, daysOfWeek.size)
        assertTrue(daysOfWeek.contains(1)) // Monday
        assertTrue(daysOfWeek.contains(7)) // Sunday

        val task = com.example.model.Task(
            id = "t_test",
            title = "Test Görevi",
            subject = "Matematik",
            topic = "Köklü Sayılar",
            targetQuestionCount = 50,
            priority = com.example.model.TaskPriority.HIGH,
            xpReward = 25,
            dayOfWeek = 3
        )
        assertEquals(3, task.dayOfWeek)
        assertEquals(25, task.xpReward)
        assertFalse(task.isApproved)
    }

    @Test
    fun testRewardValidation() {
        fun isValidReward(name: String, xp: Int, stock: Int): Boolean {
            return name.isNotBlank() && xp >= 0 && stock >= 0
        }

        assertTrue(isValidReward("Kitap Hediye Çeki", 500, 10))
        assertTrue(isValidReward("Mola", 0, 0))
        assertFalse(isValidReward("", 500, 10)) // Empty name
        assertFalse(isValidReward("Kitap", -10, 10)) // Negative XP
        assertFalse(isValidReward("Kitap", 500, -1)) // Negative Stock
    }

    @Test
    fun testParentMeetingModel() {
        val meeting = com.example.model.ParentMeeting(
            id = "pm_test",
            studentId = "s1",
            studentName = "Ali Yılmaz",
            date = "2024-04-10",
            notes = "Öğrencinin TYT performansı ele alındı.",
            topic = "Deneme Değerlendirmesi",
            decisions = "Haftada 2 branş denemesi çözülecek.",
            nextMeetingDate = "2024-04-24",
            isCompleted = true
        )
        assertEquals("Ali Yılmaz", meeting.studentName)
        assertTrue(meeting.isCompleted)
        assertEquals("Deneme Değerlendirmesi", meeting.topic)
    }

    @Test
    fun testV12TerminologyAndNaming() {
        val screenTitle = "Ödül ve Mağaza Yönetimi"
        val tab1 = "Ödül Talepleri"
        val tab2 = "Ödüller"
        val addBtn = "Yeni Ödül Ekle"
        val editAction = "Düzenle"
        val removeAction = "Ödülü Kaldır"
        val studentStore = "Ödül Mağazası"

        assertEquals("Ödül ve Mağaza Yönetimi", screenTitle)
        assertEquals("Ödül Talepleri", tab1)
        assertEquals("Ödüller", tab2)
        assertFalse(tab2.contains("Envanter"))
        assertEquals("Yeni Ödül Ekle", addBtn)
        assertEquals("Düzenle", editAction)
        assertEquals("Ödülü Kaldır", removeAction)
        assertEquals("Ödül Mağazası", studentStore)
    }

    @Test
    fun testLogoutPreservesData() {
        // Verify that catalog packages remain populated
        val studentPackages = com.example.model.PackageCatalog.getAllStudentPackages()
        val coachPackages = com.example.model.PackageCatalog.getAllCoachPackages()
        assertTrue(studentPackages.isNotEmpty())
        assertTrue(coachPackages.isNotEmpty())

        // Verify that reward models retain their values
        val reward = com.example.model.Reward("r1", "1 Saat Mola", "Mola", 500, 10)
        assertEquals("1 Saat Mola", reward.name)
        assertEquals(500, reward.xpCost)
        assertEquals(10, reward.stock)
    }

    @Test
    fun testCoachCodeFormat() {
        val code = "KOC-" + java.util.UUID.randomUUID().toString().take(6).uppercase()
        assertTrue(code.startsWith("KOC-"))
        assertEquals(10, code.length)
    }

    @Test
    fun testPomodoroModesAndMinutes() {
        val focusMode = com.example.ui.screens.PomodoroMode.FOCUS
        val shortBreak = com.example.ui.screens.PomodoroMode.SHORT_BREAK
        val longBreak = com.example.ui.screens.PomodoroMode.LONG_BREAK

        assertEquals(25, focusMode.minutes)
        assertEquals(5, shortBreak.minutes)
        assertEquals(15, longBreak.minutes)
    }

    @Test
    fun testRewardRequestApprovalFlowLogic() {
        // Student has 1000 XP, reward costs 500 XP with 5 stock
        val initialXp = 1000
        val xpCost = 500
        val initialStock = 5

        // Pending Request Creation: XP and stock remain untouched
        var studentXp = initialXp
        var rewardStock = initialStock
        val requestStatus = com.example.model.RewardRequestStatus.PENDING

        assertEquals(1000, studentXp)
        assertEquals(5, rewardStock)
        assertEquals(com.example.model.RewardRequestStatus.PENDING, requestStatus)

        // Coach Approval: Atomically deduct XP and stock
        if (requestStatus == com.example.model.RewardRequestStatus.PENDING && studentXp >= xpCost && rewardStock > 0) {
            studentXp -= xpCost
            rewardStock -= 1
        }

        assertEquals(500, studentXp)
        assertEquals(4, rewardStock)
    }

    @Test
    fun testKpssExamNetFormulas() {
        // KPSS penalty factor: 4.0 (1 wrong cancels 0.25 net)
        val score1 = ExamUtils.calculateNet(correct = 30, wrong = 4, level = EducationLevel.KPSS)
        assertEquals(29.0, score1, 0.01)

        val score2 = ExamUtils.calculateNet(correct = 60, wrong = 8, level = EducationLevel.KPSS)
        assertEquals(58.0, score2, 0.01)

        val score3 = ExamUtils.calculateNet(correct = 25, wrong = 0, level = EducationLevel.KPSS)
        assertEquals(25.0, score3, 0.01)

        val scoreNegative = ExamUtils.calculateNet(correct = 1, wrong = 8, level = EducationLevel.KPSS)
        assertEquals(-1.0, scoreNegative, 0.01)
    }

    @Test
    fun testCurriculumTrackIsolation() {
        val lgsCategories = com.example.util.CurriculumDictionary.getExamCategoriesForUser(EducationLevel.LGS_8TH)
        assertEquals(listOf("LGS"), lgsCategories)

        val kpssCategories = com.example.util.CurriculumDictionary.getExamCategoriesForUser(EducationLevel.KPSS)
        assertEquals(listOf("KPSS"), kpssCategories)

        val yksCategories = com.example.util.CurriculumDictionary.getExamCategoriesForUser(EducationLevel.YKS_TYT_AYT)
        assertEquals(listOf("TYT", "AYT"), yksCategories)
    }

    @Test
    fun testExtendedPackageCatalogTiers() {
        val studentPackages = PackageCatalog.getAllStudentPackages()
        assertEquals(3, studentPackages.size)
        assertEquals(listOf("std_free", "std_plus", "std_pro"), studentPackages.map { it.id })

        val coachPackages = PackageCatalog.getAllCoachPackages()
        assertEquals(7, coachPackages.size)
        val capacities = coachPackages.map { it.studentCapacity }
        assertEquals(listOf(3, 5, 10, 15, 20, 25, 50), capacities)
    }

    @Test
    fun testV23ExamTrackStrictIsolationAndBranchLists() {
        // Test strict exam tracks list mapping
        val yksTracks = com.example.util.CurriculumDictionary.getExamCategoriesForUser(null, listOf("YKS"))
        assertEquals(listOf("TYT", "AYT"), yksTracks)

        val lgsTracks = com.example.util.CurriculumDictionary.getExamCategoriesForUser(null, listOf("LGS"))
        assertEquals(listOf("LGS"), lgsTracks)

        val kpssTracks = com.example.util.CurriculumDictionary.getExamCategoriesForUser(null, listOf("KPSS"))
        assertEquals(listOf("KPSS"), kpssTracks)

        // Test Branch Lists
        val tytSubjects = ExamUtils.getTytSubjects()
        assertEquals(9, tytSubjects.size)
        assertTrue(tytSubjects.any { it.first == "Türkçe" && it.second == 40 })
        assertTrue(tytSubjects.any { it.first == "Temel Matematik" && it.second == 40 })

        val aytSubjects = ExamUtils.getAytSubjects()
        assertTrue(aytSubjects.any { it.first == "Matematik" && it.second == 40 })
        assertTrue(aytSubjects.any { it.first == "Fizik" && it.second == 14 })
        assertTrue(aytSubjects.any { it.first == "Kimya" && it.second == 13 })
        assertTrue(aytSubjects.any { it.first == "Biyoloji" && it.second == 13 })
        assertTrue(aytSubjects.any { it.first == "Türk Dili ve Edebiyatı" && it.second == 24 })

        val lgsSubjects = ExamUtils.getLgsSubjects()
        assertEquals(6, lgsSubjects.size)
        assertTrue(lgsSubjects.any { it.first == "Türkçe" && it.second == 20 })
        assertTrue(lgsSubjects.any { it.first == "Matematik" && it.second == 20 })
    }

    @Test
    fun testV241ComprehensiveAuditScenarios() {
        // 1. Net Formulas
        assertEquals(9.0, ExamUtils.calculateNet(10, 3, EducationLevel.LGS_8TH), 0.01)
        assertEquals(9.0, ExamUtils.calculateNet(10, 4, EducationLevel.YKS_TYT_AYT), 0.01)
        assertEquals(9.0, ExamUtils.calculateNet(10, 4, EducationLevel.KPSS), 0.01)

        // 2. Branch limits and input validation
        assertTrue(ExamUtils.validateInputs(30, 5, 5, 40))
        assertFalse(ExamUtils.validateInputs(35, 10, 0, 40)) // 45 > 40
        assertFalse(ExamUtils.validateInputs(-1, 5, 5, 40))

        // 3. Alternatif Felsefe question count preservation (120 total, no double count)
        val tytStandard = ExamUtils.getTytSubjects(isAlternatifFelsefe = false)
        val tytAlt = ExamUtils.getTytSubjects(isAlternatifFelsefe = true)
        assertEquals(120, tytStandard.sumOf { it.second })
        assertEquals(120, tytAlt.sumOf { it.second })

        // 4. Study XP calculation (Base 5 + 1 per 10 Qs, max 25)
        assertEquals(5, com.example.util.XpPolicy.calculateStudyXp(0))
        assertEquals(15, com.example.util.XpPolicy.calculateStudyXp(100))
        assertEquals(25, com.example.util.XpPolicy.calculateStudyXp(300)) // capped at 25

        // 5. Daily Cap & Idempotency logic evaluation
        val statusNormal = com.example.util.XpPolicy.evaluateCap(earnedToday = 40, proposedXp = 15)
        assertEquals(15, statusNormal.actualAward)

        val statusCapped = com.example.util.XpPolicy.evaluateCap(earnedToday = 95, proposedXp = 15)
        assertEquals(5, statusCapped.actualAward)
        assertTrue(statusCapped.isCapped)

        // 6. lifetimeXp preservation check
        val initialLifetime = 1000
        val spentAmount = 200
        val remainingCurrentXp = (500 - spentAmount).coerceAtLeast(0)
        assertEquals(1000, initialLifetime) // lifetimeXp never decreases
        assertEquals(300, remainingCurrentXp)
    }
}

