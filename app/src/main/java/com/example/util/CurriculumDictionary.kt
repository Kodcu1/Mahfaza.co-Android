package com.example.util

import com.example.model.EducationLevel

data class SubjectTopicItem(
    val subject: String,
    val topics: List<TopicItem>
)

data class TopicItem(
    val topicName: String,
    val subtopics: List<String> = emptyList()
)

object CurriculumDictionary {

    val lgsCurriculum = listOf(
        SubjectTopicItem(
            subject = "Türkçe",
            topics = listOf(
                TopicItem("Sözcükte Anlam", listOf("Gerçek ve Mecaz Anlam", "Eş ve Zıt Anlam", "Söz Sanatları")),
                TopicItem("Cümlede Anlam", listOf("Öznel ve Nesnel Cümleler", "Neden-Sonuç / Amaç-Sonuç", "Örtülü Anlam")),
                TopicItem("Paragrafta Anlam", listOf("Paragrafın Ana Fikri", "Yardımcı Fikirler", "Anlatım Biçimleri")),
                TopicItem("Fiilimsiler (Eylemsiler)", listOf("İsim-Fiil", "Sıfat-Fiil", "Zarf-Fiil")),
                TopicItem("Cümlenin Ögeleri", listOf("Temel Ögeler", "Yardımcı Ögeler")),
                TopicItem("Fiilde Çatı", listOf("Öznesine Göre Çatı", "Nesnesine Göre Çatı")),
                TopicItem("Cümle Türleri", listOf("Anlamına Göre", "Yapısına Göre")),
                TopicItem("Yazım Kuralları", listOf("Büyük Harflerin Kullanımı", "de/ki/mi Yazımı")),
                TopicItem("Noktalama İşaretleri", listOf("Virgül ve Noktalı Virgül", "İki Nokta ve Üç Nokta")),
                TopicItem("Sözel Mantık ve Görsel Okuma", listOf("Mantık Muhakeme", "Grafik ve Tablo Okuma"))
            )
        ),
        SubjectTopicItem(
            subject = "Matematik",
            topics = listOf(
                TopicItem("Çarpanlar ve Katlar", listOf("EBOB ve EKOK", "Aralarında Asal Sayılar")),
                TopicItem("Üslü İfadeler", listOf("Tam Sayıların Üsleri", "Bilimsel Gösterim")),
                TopicItem("Kareköklü İfadeler", listOf("Tam Kare Sayılar", "Kareköklü Sayılarda İşlemler")),
                TopicItem("Veri Analizi", listOf("Sütun ve Daire Grafiği")),
                TopicItem("Basit Olayların Olma Olasılığı", listOf("Olasılık Hesaplama")),
                TopicItem("Cebirsel İfadeler ve Özdeşlikler", listOf("Özdeşlikler", "Çarpanlara Ayırma")),
                TopicItem("Doğrusal Denklemler", listOf("Birinci Dereceden Denklemler", "Eğim")),
                TopicItem("Eşitsizlikler", listOf("Eşitsizlik Çözümü")),
                TopicItem("Üçgenler", listOf("Açıkenar Bağıntıları", "Pisagor Bağıntısı")),
                TopicItem("Eşlik ve Benzerlik", listOf("Benzerlik Oranı")),
                TopicItem("Dönüşüm Geometrisi", listOf("Yansıma ve Öteleme")),
                TopicItem("Geometrik Cisimler", listOf("Dik Prizmalar", "Dik Piramit ve Konik"))
            )
        ),
        SubjectTopicItem(
            subject = "Fen Bilimleri",
            topics = listOf(
                TopicItem("Mevsimler ve İklim", listOf("Mevsimlerin Oluşumu", "İklim ve Hava Hareketleri")),
                TopicItem("DNA ve Genetik Kod", listOf("DNA Yapısı", "Kalıtım", "Mutasyon ve Modifikasyon")),
                TopicItem("Basınç", listOf("Katı Basıncı", "Sıvı Basıncı", "Gaz Basıncı")),
                TopicItem("Madde ve Endüstri", listOf("Periyodik Sistem", "Kimyasal Tepkimeler", "Asitler ve Bazlar")),
                TopicItem("Basit Makineler", listOf("Kaldıraçlar", "Makaralar ve Palangalar", "Eğik Düzlem")),
                TopicItem("Enerji Dönüşümleri ve Çevre Bilimi", listOf("Besin Zinciri", "Fotosentez ve Solunum")),
                TopicItem("Elektrik Yükleri ve Elektrik Enerjisi", listOf("Elektriklenme", "Elektrik Enerjisinin Dönüşümü"))
            )
        ),
        SubjectTopicItem(
            subject = "T.C. İnkılap Tarihi ve Atatürkçülük",
            topics = listOf(
                TopicItem("Bir Kahraman Doğuyor", listOf("Mustafa Kemal'in Öğrenim Hayatı")),
                TopicItem("Milli Uyanış: Bağımsızlık Yolunda Atılan Adımlar", listOf("Birinci Dünya Savaşı", "Kongreler")),
                TopicItem("Milli Bir Destan: Ya İstiklal Ya Ölüm!", listOf("Doğu ve Güney Cephesi", "Batı Cephesi")),
                TopicItem("Atatürkçülük ve Çağdaşlaşan Türkiye", listOf("Atatürk İlkeleri", "İnkılaplar")),
                TopicItem("Demokratikleşme Çabaları", listOf("Çok Partili Hayat Denemeleri")),
                TopicItem("Atatürk Dönemi Türk Dış Politikası", listOf("Dış Politika Esasları"))
            )
        ),
        SubjectTopicItem(
            subject = "Din Kültürü ve Ahlak Bilgisi",
            topics = listOf(
                TopicItem("Kader İnancı", listOf("Kaza ve Kader Kavramları", "İnsanın İradesi")),
                TopicItem("Zekat ve Sadaka", listOf("Zekat ve Sadaka İbadeti")),
                TopicItem("Din ve Hayat", listOf("Dinin Birey ve Toplumdaki Yeri")),
                TopicItem("Hz. Muhammed'in Örnekliği", listOf("Doğruluğu ve Güvenilirliği")),
                TopicItem("Kur'an-ı Kerim ve Özellikleri", listOf("Kur'an'ın Ana Konuları"))
            )
        ),
        SubjectTopicItem(
            subject = "İngilizce",
            topics = listOf(
                TopicItem("Friendship", listOf("Accepting/Refusing Invitations")),
                TopicItem("Teen Life", listOf("Daily Routines")),
                TopicItem("In The Kitchen", listOf("Cooking Processes")),
                TopicItem("On The Phone", listOf("Phone Conversations")),
                TopicItem("The Internet", listOf("Internet Safety Rules")),
                TopicItem("Adventures", listOf("Extreme Sports")),
                TopicItem("Tourism", listOf("Tourist Attractions")),
                TopicItem("Chores", listOf("Household Responsibilities"))
            )
        )
    )

    val tytCurriculum = listOf(
        SubjectTopicItem(
            subject = "Türkçe",
            topics = listOf(
                TopicItem("Sözcükte Anlam"),
                TopicItem("Cümlede Anlam"),
                TopicItem("Paragrafta Anlam ve Yapı"),
                TopicItem("Ses Bilgisi"),
                TopicItem("Yazım Kuralları"),
                TopicItem("Noktalama İşaretleri"),
                TopicItem("Sözcük Türleri"),
                TopicItem("Edat, Bağlaç, Ünlem"),
                TopicItem("Fiiller ve Fiilimsi"),
                TopicItem("Cümlenin Ögeleri"),
                TopicItem("Cümle Çeşitleri"),
                TopicItem("Anlatım Bozuklukları")
            )
        ),
        SubjectTopicItem(
            subject = "Temel Matematik",
            topics = listOf(
                TopicItem("Temel Kavramlar ve Sayı Kümeleri"),
                TopicItem("Bölme, Bölünebilme, EBOB-EKOK"),
                TopicItem("Rasyonel ve Ondalık Sayılar"),
                TopicItem("Birinci Dereceden Denklemler ve Eşitsizlikler"),
                TopicItem("Mutlak Değer"),
                TopicItem("Üslü ve Köklü İfadeler"),
                TopicItem("Çarpanlara Ayırma"),
                TopicItem("Oran - Orantı"),
                TopicItem("Problemler (Sayı, Kesir, Yaş, Yüzde, Hız, Karışım)"),
                TopicItem("Kümeler ve Mantık"),
                TopicItem("Fonksiyonlar"),
                TopicItem("Permütasyon, Kombinasyon, Olasılık"),
                TopicItem("Veri ve İstatistik")
            )
        ),
        SubjectTopicItem(
            subject = "Geometri",
            topics = listOf(
                TopicItem("Doğroda ve Üçgende Açılar"),
                TopicItem("Dik Üçgen ve Trigonometri"),
                TopicItem("İkizkenar ve Eşkenar Üçgen"),
                TopicItem("Üçgende Alan ve Benzerlik"),
                TopicItem("Çokgenler ve Dörtgenler"),
                TopicItem("Paralelkenar, Eşkenar Dörtgen, Kare"),
                TopicItem("Çember ve Daire"),
                TopicItem("Analitik Geometri"),
                TopicItem("Katı Cisimler (Prizma, Piramit, Küre)")
            )
        ),
        SubjectTopicItem(
            subject = "Fizik",
            topics = listOf(
                TopicItem("Fizik Bilimine Giriş ve Madde"),
                TopicItem("Basınç ve Kaldırma Kuvveti"),
                TopicItem("Isı, Sıcaklık ve Genleşme"),
                TopicItem("Hareket ve Kuvvet"),
                TopicItem("İş, Güç ve Enerji"),
                TopicItem("Elektrostatik ve Elektrik Akımı"),
                TopicItem("Optik ve Dalgalar")
            )
        ),
        SubjectTopicItem(
            subject = "Kimya",
            topics = listOf(
                TopicItem("Kimya Bilimi ve Atomun Yapısı"),
                TopicItem("Periyodik Sistem"),
                TopicItem("Kimyasal Türler Arası Etkileşimler"),
                TopicItem("Maddenin Halleri"),
                TopicItem("Kimyanın Temel Kanunları ve Mol"),
                TopicItem("Asitler, Bazlar ve Tuzlar"),
                TopicItem("Karışımlar")
            )
        ),
        SubjectTopicItem(
            subject = "Biyoloji",
            topics = listOf(
                TopicItem("Yaşam Bilimi Biyoloji ve Hücre"),
                TopicItem("Canlıların Sınıflandırılması"),
                TopicItem("Hücre Bölünmeleri"),
                TopicItem("Kalıtımın Temel İlkeleri"),
                TopicItem("Ekosistem Ekolojisi")
            )
        ),
        SubjectTopicItem(
            subject = "Tarih",
            topics = listOf(
                TopicItem("Tarih ve Zaman"),
                TopicItem("İnsanlığın İlk Dönemleri"),
                TopicItem("İlk ve Orta Çağlarda Türk Dünyası"),
                TopicItem("İslam Medeniyetinin Doğuşu"),
                TopicItem("Türklerin İslamiyet'i Kabulü"),
                TopicItem("Osmanlı Tarihi ve Teşkilatı"),
                TopicItem("20. Yüzyıl Başlarında Osmanlı Devleti"),
                TopicItem("Milli Mücadele Dönemi")
            )
        ),
        SubjectTopicItem(
            subject = "Coğrafya",
            topics = listOf(
                TopicItem("Doğal Sistemler ve Harita Bilgisi"),
                TopicItem("İklim Bilgisi"),
                TopicItem("Yerin Şekillenmesi ve İç/Dış Kuvvetler"),
                TopicItem("Beşeri Sistemler ve Nüfus"),
                TopicItem("Türkiye'nin Konumu ve Yüzey Şekilleri"),
                TopicItem("Çevre ve Toplum")
            )
        ),
        SubjectTopicItem(
            subject = "Felsefe",
            topics = listOf(
                TopicItem("Felsefeyi Tanıma ve Düşünme"),
                TopicItem("Varlık ve Bilgi Felsefesi"),
                TopicItem("Ahlak ve Sanat Felsefesi"),
                TopicItem("Din ve Siyaset Felsefesi")
            )
        ),
        SubjectTopicItem(
            subject = "Din Kültürü ve Ahlak Bilgisi",
            topics = listOf(
                TopicItem("Bilgi ve İnanç"),
                TopicItem("İbadet ve Ahlak"),
                TopicItem("Hz. Muhammed ve Gençlik"),
                TopicItem("İslam ve Bilim")
            )
        )
    )

    val aytCurriculum = listOf(
        SubjectTopicItem(
            subject = "Matematik",
            topics = listOf(
                TopicItem("Polinomlar"),
                TopicItem("İkinci Dereceden Denklemler"),
                TopicItem("Karmaşık Sayılar"),
                TopicItem("Parabol"),
                TopicItem("Eşitsizlikler"),
                TopicItem("Trigonometri"),
                TopicItem("Logaritma"),
                TopicItem("Diziler"),
                TopicItem("Limit ve Süreklilik"),
                TopicItem("Türev ve Uygulamaları"),
                TopicItem("İntegral ve Alan Hesabı")
            )
        ),
        SubjectTopicItem(
            subject = "Geometri",
            topics = listOf(
                TopicItem("Dönüşüm Geometrisi"),
                TopicItem("Analitik Geometri"),
                TopicItem("Çemberin Analitik İncelenmesi"),
                TopicItem("Katı Cisimler")
            )
        ),
        SubjectTopicItem(
            subject = "Fizik",
            topics = listOf(
                TopicItem("Vektörler ve Bağıl Hareket"),
                TopicItem("Newton'ın Hareket Yasaları"),
                TopicItem("İtme ve Momentum"),
                TopicItem("Tork, Denge ve Basit Makineler"),
                TopicItem("Elektriksel Alan ve Potansiyel"),
                TopicItem("Manyetizma ve İndüksiyon"),
                TopicItem("Çembersel Hareket ve Basit Harmonik Hareket"),
                TopicItem("Dalga Mekaniği"),
                TopicItem("Modern Fizik ve Fotoelektrik")
            )
        ),
        SubjectTopicItem(
            subject = "Kimya",
            topics = listOf(
                TopicItem("Modern Atom Teorisi"),
                TopicItem("Gazlar"),
                TopicItem("Sıvı Çözeltiler ve Derişim"),
                TopicItem("Tepkimelerde Enerji ve Hız"),
                TopicItem("Kimyasal Denge ve Asit-Baz Dengesi"),
                TopicItem("Kimya ve Elektrik"),
                TopicItem("Organik Kimyaya Giriş"),
                TopicItem("Organik Bileşikler")
            )
        ),
        SubjectTopicItem(
            subject = "Biyoloji",
            topics = listOf(
                TopicItem("İnsan Fizyolojisi ve Sistemler"),
                TopicItem("Genden Proteine (DNA ve RNA)"),
                TopicItem("Canlılarda Enerji Dönüşümleri (Fotosentez, Solunum)"),
                TopicItem("Bitki Biyolojisi"),
                TopicItem("Canlılar ve Çevre")
            )
        ),
        SubjectTopicItem(
            subject = "Türk Dili ve Edebiyatı",
            topics = listOf(
                TopicItem("Edebi Sanatlar ve Şiir Bilgisi"),
                TopicItem("İslamiyet Öncesi ve Halk Edebiyatı"),
                TopicItem("Divan Edebiyatı"),
                TopicItem("Tanzimat ve Servet-i Fünun Edebiyatı"),
                TopicItem("Milli Edebiyat ve Cumhuriyet Dönemi Edebiyatı"),
                TopicItem("Edebi Akımlar")
            )
        ),
        SubjectTopicItem(
            subject = "Tarih-1",
            topics = listOf(
                TopicItem("Tarih Bilimi ve İlk Çağ"),
                TopicItem("Türk-İslam Tarihi"),
                TopicItem("Osmanlı Siyasi Tarihi"),
                TopicItem("Atatürk İlkeleri ve İnkılap Tarihi")
            )
        ),
        SubjectTopicItem(
            subject = "Coğrafya-1",
            topics = listOf(
                TopicItem("Ekosistem ve Madde Döngüleri"),
                TopicItem("Şehirler ve Etki Alanları"),
                TopicItem("Türkiye'de Ekonomi ve Arazi Kullanımı"),
                TopicItem("Küresel Ortam: Bölgeler ve Ülkeler")
            )
        ),
        SubjectTopicItem(
            subject = "Tarih-2",
            topics = listOf(
                TopicItem("Orta Çağ ve Yeni Çağ'da Avrupa"),
                TopicItem("Osmanlı Teşkilat ve Kültürü"),
                TopicItem("20. Yüzyıl Başlarında Dünya"),
                TopicItem("Çağdaş Türk ve Dünya Tarihi")
            )
        ),
        SubjectTopicItem(
            subject = "Coğrafya-2",
            topics = listOf(
                TopicItem("Çevre ve Toplum"),
                TopicItem("Uluslararası Örgütler"),
                TopicItem("Küreselleşen Dünya")
            )
        ),
        SubjectTopicItem(
            subject = "Felsefe Grubu",
            topics = listOf(
                TopicItem("Mantık"),
                TopicItem("Psikoloji"),
                TopicItem("Sosyoloji"),
                TopicItem("Felsefî Metin Okumaları")
            )
        ),
        SubjectTopicItem(
            subject = "Din Kültürü ve Ahlak Bilgisi",
            topics = listOf(
                TopicItem("İslam Düşüncesinde Yorumlar"),
                TopicItem("Güncel Dini Meseleler"),
                TopicItem("Dünya Dinleri")
            )
        )
    )

    val kpssCurriculum = listOf(
        SubjectTopicItem(
            subject = "Genel Yetenek",
            topics = listOf(
                TopicItem("Türkçe"),
                TopicItem("Matematik")
            )
        ),
        SubjectTopicItem(
            subject = "Genel Kültür",
            topics = listOf(
                TopicItem("Tarih"),
                TopicItem("Coğrafya"),
                TopicItem("Vatandaşlık")
            )
        ),
        SubjectTopicItem(
            subject = "Eğitim Bilimleri",
            topics = listOf(
                TopicItem("Gelişim Psikolojisi"),
                TopicItem("Öğrenme Psikolojisi"),
                TopicItem("Öğretim Yöntem ve Teknikleri")
            )
        )
    )

    fun getExamCategoriesForUser(educationLevel: EducationLevel?, examTracks: List<String> = emptyList()): List<String> {
        if (examTracks.isNotEmpty()) {
            val tracks = examTracks.map { it.uppercase().trim() }
            val categories = mutableListOf<String>()
            if (tracks.contains("LGS")) categories.add("LGS")
            if (tracks.contains("YKS")) {
                categories.add("TYT")
                categories.add("AYT")
            }
            if (tracks.contains("KPSS")) categories.add("KPSS")
            if (categories.isNotEmpty()) return categories
        }
        return when (educationLevel) {
            EducationLevel.LGS_8TH -> listOf("LGS")
            EducationLevel.KPSS -> listOf("KPSS")
            EducationLevel.YKS_TYT_AYT -> listOf("TYT", "AYT")
            null -> listOf("TYT", "AYT")
        }
    }

    fun getSubjectsForExamCategory(category: String): List<SubjectTopicItem> {
        return when (category.uppercase()) {
            "TYT" -> tytCurriculum
            "AYT" -> aytCurriculum
            "LGS" -> lgsCurriculum
            "KPSS" -> kpssCurriculum
            else -> tytCurriculum
        }
    }

    fun getTopicsForSubject(category: String, subjectName: String): List<TopicItem> {
        val subjects = getSubjectsForExamCategory(category)
        val match = subjects.find { it.subject.equals(subjectName, ignoreCase = true) }
        val topics = match?.topics ?: emptyList()
        return topics + TopicItem("Diğer / Serbest Konu")
    }

    fun getSubjectsForLevel(level: EducationLevel?): List<SubjectTopicItem> {
        return when(level) {
            EducationLevel.LGS_8TH -> lgsCurriculum
            EducationLevel.KPSS -> kpssCurriculum
            else -> tytCurriculum
        }
    }
}
