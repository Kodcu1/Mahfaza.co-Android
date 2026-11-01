package com.example.util

enum class RankTier(
    val displayName: String,
    val badgeSymbol: String,
    val primaryColorHex: Long,
    val surfaceColorHex: Long,
    val borderHex: Long,
    val perkBonusText: String
) {
    BRONZE("Bronz Lig", "🥉", 0xFFCD7F32, 0xFFFFF8F0, 0xFFE8A87C, "Temel XP Çarpanı • Bronz Profil Rozeti"),
    SILVER("Gümüş Lig", "🥈", 0xFF64748B, 0xFFF8FAFC, 0xFFCBD5E1, "+%5 XP Bonusu • Gümüş Çerçeve"),
    GOLD("Altın Lig", "🥇", 0xFFD97706, 0xFFFFFDF9, 0xFFFCD34D, "+%10 XP Bonusu • Altın Parıltı Efekti"),
    PLATINUM("Platin Lig", "💎", 0xFF0284C7, 0xFFF0F9FF, 0xFF7DD3FC, "+%15 XP Bonusu • Platin Taç Rozeti"),
    DIAMOND("Elmas Lig", "👑", 0xFF7C3AED, 0xFFF5F3FF, 0xFFC4B5FD, "+%20 XP Bonusu • Elmas Alev Efekti"),
    LEGENDARY("Efsanevi Lig", "🏆", 0xFFE11D48, 0xFFFFF1F2, 0xFFFDA4AF, "+%25 XP Bonusu • Kozmik Üstat Rozeti & Özel Unvan")
}

data class LevelData(
    val levelNumber: Int,
    val title: String,
    val reqXp: Int,
    val tier: RankTier,
    val badgeSymbol: String,
    val perkDescription: String
)

data class LevelInfo(
    val levelNumber: Int,
    val title: String,
    val tier: RankTier,
    val badgeSymbol: String,
    val minXp: Int,
    val maxXp: Int,
    val currentLifetimeXp: Int,
    val xpInLevel: Int,
    val xpNeededForNextLevel: Int,
    val remainingXpToNextLevel: Int,
    val progress: Float, // 0f to 1f
    val isMaxLevel: Boolean,
    val perkDescription: String
)

object LevelCalculator {

    val levelThresholds: List<LevelData> = listOf(
        // Tier 1: Bronz Lig (1 - 5)
        LevelData(1, "Çırak Öğrenci", 0, RankTier.BRONZE, "🌱", "Öğrenim yolculuğunun başlangıcı"),
        LevelData(2, "İlk Adım Yolcusu", 100, RankTier.BRONZE, "👣", "Düzenli çalışma alışkanlığı başlangıcı"),
        LevelData(3, "Disiplin Arayıcısı", 250, RankTier.BRONZE, "🎯", "Günlük hedef tamamlama yetisi"),
        LevelData(4, "İstikrarlı Başlangıç", 450, RankTier.BRONZE, "⚡", "Hızlı soru çözümü yeteneği"),
        LevelData(5, "Bronz Muhafız", 700, RankTier.BRONZE, "🥉", "Bronz Lig unvanı ve özel rozet"),

        // Tier 2: Gümüş Lig (6 - 10)
        LevelData(6, "Bilgi Avcısı", 1000, RankTier.SILVER, "🔍", "Gümüş Lig'e terfi & +%5 XP bonusu"),
        LevelData(7, "Odak Ustası", 1400, RankTier.SILVER, "🧘", "Derin çalışma oturumları ustalığı"),
        LevelData(8, "Çalışma Stratejisti", 1900, RankTier.SILVER, "♟️", "Haftalık planlama uzmanlığı"),
        LevelData(9, "Soru Bükücü", 2500, RankTier.SILVER, "🌀", "Farklı soru tiplerine hakimiyet"),
        LevelData(10, "Gümüş Şövalye", 3200, RankTier.SILVER, "🥈", "Gümüş Zirve unvanı"),

        // Tier 3: Altın Lig (11 - 15)
        LevelData(11, "Akademik Mimar", 4000, RankTier.GOLD, "🏛️", "Altın Lig'e terfi & +%10 XP bonusu"),
        LevelData(12, "Net Savaşçısı", 5000, RankTier.GOLD, "⚔️", "Denemelerde sürekli net artışı"),
        LevelData(13, "Sınav Fatihi", 6200, RankTier.GOLD, "🏹", "Süre ve zaman yönetimi uzmanı"),
        LevelData(14, "Kütüphane Hakimi", 7500, RankTier.GOLD, "📚", "Sürekli ve azimli çalışma gücü"),
        LevelData(15, "Altın Muhafız", 9000, RankTier.GOLD, "🥇", "Altın Zirve unvanı ve özel rozet"),

        // Tier 4: Platin Lig (16 - 20)
        LevelData(16, "Zaman Bükücü", 10800, RankTier.PLATINUM, "⏳", "Platin Lig'e terfi & +%15 XP bonusu"),
        LevelData(17, "Derece Adayı", 12800, RankTier.PLATINUM, "🎓", "Sıralama hedeflerine yakınlaşma"),
        LevelData(18, "Paragraf & Problem Dâhisi", 15000, RankTier.PLATINUM, "📖", "Kritik soru tiplerinde ustalaşma"),
        LevelData(19, "Analiz Üstadı", 17500, RankTier.PLATINUM, "📐", "Yanlış soruları sıfırlama başarısı"),
        LevelData(20, "Platin Şövalye", 20200, RankTier.PLATINUM, "💎", "Platin Zirve unvanı ve taç simgesi"),

        // Tier 5: Elmas Lig (21 - 25)
        LevelData(21, "Zirve Takipçisi", 23200, RankTier.DIAMOND, "🏔️", "Elmas Lig'e terfi & +%20 XP bonusu"),
        LevelData(22, "Hata Avcısı", 26500, RankTier.DIAMOND, "🛡️", "Sıfır hata yaklaşımı ustalığı"),
        LevelData(23, "Net Rekortmeni", 30000, RankTier.DIAMOND, "🚀", "Kendi rekorlarını kırma başarısı"),
        LevelData(24, "Şampiyonlar Ligi", 34000, RankTier.DIAMOND, "👑", "Top %1 akademik derece dilimi"),
        LevelData(25, "Elmas Üstat", 38500, RankTier.DIAMOND, "✨", "Elmas Zirve unvanı ve alev rozeti"),

        // Tier 6: Efsanevi Lig (26 - 30)
        LevelData(26, "Mahfaza Efsanesi", 43500, RankTier.LEGENDARY, "🏆", "Efsanevi Lig'e terfi & +%25 XP bonusu"),
        LevelData(27, "Gök Kubbe Hakimi", 49000, RankTier.LEGENDARY, "🌌", "Kusursuz disiplin ve azim takdiri"),
        LevelData(28, "Sınav Ustası", 55000, RankTier.LEGENDARY, "⚡", "Tüm konularda tam hakimiyet"),
        LevelData(29, "Kozmik Bilge", 62000, RankTier.LEGENDARY, "🔮", "Efsanevi derece sahibi"),
        LevelData(30, "Ölümsüz Üstat (MAX)", 70000, RankTier.LEGENDARY, "🌟", "Mahfaza.co En Yüksek Akademik Unvanı")
    )

    fun calculateLevel(lifetimeXp: Int): LevelInfo {
        val safeXp = lifetimeXp.coerceAtLeast(0)

        var currentData = levelThresholds.first()
        var minXp = currentData.reqXp
        var maxXp = levelThresholds[1].reqXp

        for (i in levelThresholds.indices) {
            val data = levelThresholds[i]
            if (safeXp >= data.reqXp) {
                currentData = data
                minXp = data.reqXp
                maxXp = if (i + 1 < levelThresholds.size) levelThresholds[i + 1].reqXp else data.reqXp
            } else {
                break
            }
        }

        val maxLevelNumber = levelThresholds.last().levelNumber
        val isMaxLevel = currentData.levelNumber >= maxLevelNumber
        val xpInLevel = (safeXp - minXp).coerceAtLeast(0)
        val xpNeededForNextLevel = (maxXp - minXp).coerceAtLeast(1)
        val remainingXpToNextLevel = if (isMaxLevel) 0 else (maxXp - safeXp).coerceAtLeast(0)

        val progress = if (isMaxLevel) 1.0f else (xpInLevel.toFloat() / xpNeededForNextLevel.toFloat()).coerceIn(0f, 1f)

        return LevelInfo(
            levelNumber = currentData.levelNumber,
            title = currentData.title,
            tier = currentData.tier,
            badgeSymbol = currentData.badgeSymbol,
            minXp = minXp,
            maxXp = maxXp,
            currentLifetimeXp = safeXp,
            xpInLevel = xpInLevel,
            xpNeededForNextLevel = xpNeededForNextLevel,
            remainingXpToNextLevel = remainingXpToNextLevel,
            progress = progress,
            isMaxLevel = isMaxLevel,
            perkDescription = currentData.perkDescription
        )
    }

    fun getNextLevelTitle(currentLevel: Int): String {
        val nextLevelNumber = (currentLevel + 1).coerceAtMost(levelThresholds.last().levelNumber)
        return levelThresholds.find { it.levelNumber == nextLevelNumber }?.title ?: "Ölümsüz Üstat (MAX)"
    }

    fun getAllLevels(currentLifetimeXp: Int = 0): List<LevelInfo> {
        return levelThresholds.map { data ->
            calculateLevelForData(data, currentLifetimeXp)
        }
    }

    private fun calculateLevelForData(data: LevelData, safeXp: Int): LevelInfo {
        val index = levelThresholds.indexOf(data)
        val minXp = data.reqXp
        val maxXp = if (index + 1 < levelThresholds.size) levelThresholds[index + 1].reqXp else data.reqXp
        val maxLevelNumber = levelThresholds.last().levelNumber
        val isMaxLevel = data.levelNumber >= maxLevelNumber

        val isReached = safeXp >= minXp
        val xpInLevel = if (isReached) (safeXp - minXp).coerceAtLeast(0) else 0
        val xpNeeded = (maxXp - minXp).coerceAtLeast(1)
        val remaining = if (isReached) {
            if (isMaxLevel) 0 else (maxXp - safeXp).coerceAtLeast(0)
        } else {
            minXp - safeXp
        }

        val progress = if (!isReached) 0f else if (isMaxLevel) 1f else (xpInLevel.toFloat() / xpNeeded.toFloat()).coerceIn(0f, 1f)

        return LevelInfo(
            levelNumber = data.levelNumber,
            title = data.title,
            tier = data.tier,
            badgeSymbol = data.badgeSymbol,
            minXp = minXp,
            maxXp = maxXp,
            currentLifetimeXp = safeXp,
            xpInLevel = xpInLevel,
            xpNeededForNextLevel = xpNeeded,
            remainingXpToNextLevel = remaining,
            progress = progress,
            isMaxLevel = isMaxLevel,
            perkDescription = data.perkDescription
        )
    }
}

