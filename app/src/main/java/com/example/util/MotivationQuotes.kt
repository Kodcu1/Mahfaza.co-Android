package com.example.util

import android.content.Context
import android.content.SharedPreferences

object MotivationQuotes {

    val studentQuotes = listOf(
        "Bugün attığın her küçük adım, yarın hayallerine açılan büyük bir kapıdır.",
        "Disiplin, ne istediğinle şu an ne istediğin arasındaki seçimdir.",
        "Zorluklar, başarının değerini artıran gizli basamaklardır.",
        "Başarı tesadüf değil; çalışmanın, azmin ve tutkunun meyvesidir.",
        "Gelecek, bugün hazırlananlara aittir.",
        "Hedefine odaklan, yolundaki engeller sadece birer tecrübedir.",
        "Her sabah yeni bir başlangıç, her soru yeni bir kazanımdır.",
        "Yorulabilirsin ama asla vazgeçme; zirve sabredenlerindir.",
        "Küçük günlük disiplinler, zamanla büyük başarılara dönüşür.",
        "Yapabileceğine inandığın an, yolun yarısını katetmiş olursun.",
        "Çalışmanın bedeli geçicidir; başaramamanın pişmanlığı kalıcıdır.",
        "Zamanını yöneten, geleceğini yönetir.",
        "Bugünkü emeğin, yarınki özgürlüğündür.",
        "Hayal etmek yetmez; harekete geçmek gerekir.",
        "Her yanlış soru, doğruya giden en öğretici rehberdir.",
        "Engeller seni durdurmasın; her engel bir güçlenme fırsatıdır.",
        "Netlerin artışı bir günde değil, her gün sabırla işlenen emekle gelir.",
        "Kendine olan inancın, en büyük güç kaynağındır.",
        "Bugün ertelemediğin her ders, yarınki huzurundur.",
        "Başarı, her gün tekrarlanan küçük çabaların toplamıdır.",
        "Sınav sadece bilgini değil, kriz anındaki iradeni de ölçer.",
        "Derin odaklanma, zekayı başarıya dönüştüren anahtardır.",
        "Pes etmediğin sürece yenilmiş sayılmazsın.",
        "Aklına koyduğun her hedef, doğru stratejiyle ulaşılabilirdir.",
        "Bugün çözdüğün her paragraf, okuma hızını ve kavrayışını geliştirir.",
        "Matematik bir engel değil, mantığını geliştiren zihinsel bir spordur.",
        "Fen derslerinde merakın artarsa, başarı kendiliğinden gelir.",
        "Düzenli deneme analizi, eksiklerini zafere dönüştürme sanatıdır.",
        "Çalışma masana oturduğun her an, geleceğine bir yatırım yaparsın.",
        "Günün sonunda 'elimden gelenin en iyisini yaptım' diyebilmek harika bir histir.",
        "Zor sorular seni korkutmasın; onlar seni rakiplerinden ayıracak fırsatlardır.",
        "Sınav maratonu bir sprint değil, istikrar koşusudur.",
        "Mola vermek dinlendirir, vazgeçmek kaybettirir. Molanı ver ve devam et.",
        "Bugünün yoğunluğu, yarının gurur vesilesi olacaktır.",
        "Odaklandığın şey büyür; zorluklara değil, hedefine odaklan.",
        "Kendi rekorunu kırmaya çalış, başkalarıyla değil kendinle yarış.",
        "Düzenli uyku ve planlı çalışma, en yüksek verimin gizli formülüdür.",
        "İnanç, imkansız gibi görünen kapıları aralar.",
        "Her deneme sınavı bir tecrübe deposudur.",
        "Hatalarından ders çıkardığın sürece her gün daha güçlüsün.",
        "Hedefin ne kadar büyükse, adımların o kadar kararlı olmalı.",
        "Başarı bir alışkanlıktır; disiplini alışkanlık haline getir.",
        "Geleceğin sen, bugünkü emeğin için sana teşekkür edecek.",
        "Zihnini olumlu düşüncelerle besle, başarma gücün katlansın.",
        "Sabır ve sürekli çalışma, her kilitli kapıyı açar.",
        "Işığını parlatmak için önce dirsek çürütmek gerekir.",
        "Bugünkü gayretin, yarın istediğin üniversite/lise kapısından içeri girmendir.",
        "Konu tekrarlarını ihmal etme; sağlam temel yüksek bina inşa eder.",
        "Zamanı boşa harcamamak, hedefe giden en kestirme yoldur.",
        "Kendine inan ve sürece güven; başaranlar senle aynı yoldan geçti.",
        "Soru çözerken dikkatin, sınav günü en büyük kozun olacak.",
        "Anlamadığın konunun üstüne gitmek, gerçek gelişimin başladığı yerdir.",
        "Her gün biraz daha ileri gitmek, yerinde saymaktan iyidir.",
        "Planına sadık kal, sonuçlar seni şaşırtacak kadar güzel olacak.",
        "Motivasyon başlamanı sağlar, disiplin devam etmeni.",
        "Unutma: Başarı bir varış noktası değil, yolculuğun kendisidir.",
        "Hayallerinin büyüklüğü, çalışma azmine yansısın.",
        "Zorlandığın anlarda, neden başladığını hatırla.",
        "Her gün yeni bir şans, yeni bir öğrenme fırsatıdır.",
        "Başaracaksın, çünkü pes etmemeyi öğreniyorsun!"
    )

    val coachNotes = listOf(
        "Öğrencilerinin potansiyelini ortaya çıkaran en büyük güç, senin tutarlı rehberliğindir.",
        "Bugün bir öğrencine katacağın farkındalık, onun tüm geleceğini şekillendirebilir.",
        "İyi bir koç sadece ders planlamaz; özgüven ve disiplin aşılar.",
        "Öğrencilerinin motivasyonu düştüğünde, senin inancın onların tutunacak dalıdır.",
        "Gelişim sabır ister; küçük adımları takdir etmek büyük başarıları getirir.",
        "Veri odaklı koçluk, doğru müdahaleleri yapmanın en sağlam yoludur.",
        "Öğrenciye özel strateji geliştirmek, başarının en kritik anahtarıdır.",
        "Empati ve disiplin dengesi, koçluk profesyonelliğinin temelidir.",
        "Bugün kontrol edeceğin onaylar ve geri bildirimler öğrencinin çalışma ivmesini artıracak.",
        "Her öğrenci benzersizdir; doğru dokunuşla her cevher parlar.",
        "Onay bekleyen çalışmaları inceleyip motive edici geri bildirimler sunma zamanı!",
        "Veli görüşmelerinde yapıcı ve çözüm odaklı olmak öğrenci desteğini ikiye katlar.",
        "İyi bir takip sistemi, aksamaları anında tespit etmeni sağlar.",
        "Öğrencine rol model olduğunu unutma; senin enerjin onlara yansır.",
        "Bugün dokunduğun her hayat, yarının başarılı bir bireyi olacak."
    )

    fun getTodayStudentQuote(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences("mahfaza_quotes", Context.MODE_PRIVATE)
        val todaySessionKey = "student_quote_idx"
        val lastSessionIdKey = "last_session_time"

        val currentTime = System.currentTimeMillis()
        val lastTime = prefs.getLong(lastSessionIdKey, 0L)
        var savedIdx = prefs.getInt(todaySessionKey, -1)

        // If more than 4 hours have passed or first launch, pick a new quote index
        if (savedIdx == -1 || (currentTime - lastTime) > (4 * 3600 * 1000L)) {
            val nextIdx = ((savedIdx + 1) + (0 until studentQuotes.size).random()) % studentQuotes.size
            savedIdx = nextIdx
            prefs.edit()
                .putInt(todaySessionKey, savedIdx)
                .putLong(lastSessionIdKey, currentTime)
                .apply()
        }

        return studentQuotes[savedIdx.coerceIn(0, studentQuotes.size - 1)]
    }

    fun getTodayCoachNote(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences("mahfaza_quotes", Context.MODE_PRIVATE)
        val todaySessionKey = "coach_note_idx"
        val lastSessionIdKey = "last_coach_session_time"

        val currentTime = System.currentTimeMillis()
        val lastTime = prefs.getLong(lastSessionIdKey, 0L)
        var savedIdx = prefs.getInt(todaySessionKey, -1)

        if (savedIdx == -1 || (currentTime - lastTime) > (4 * 3600 * 1000L)) {
            val nextIdx = ((savedIdx + 1) + (0 until coachNotes.size).random()) % coachNotes.size
            savedIdx = nextIdx
            prefs.edit()
                .putInt(todaySessionKey, savedIdx)
                .putLong(lastSessionIdKey, currentTime)
                .apply()
        }

        return coachNotes[savedIdx.coerceIn(0, coachNotes.size - 1)]
    }
}
