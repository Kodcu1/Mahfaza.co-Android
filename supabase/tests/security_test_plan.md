# Mahfaza.co V34.1 — Backend Güvenlik ve Doğrulama Test Planı

Bu test planı, Supabase PostgreSQL şeması, RLS politikaları, davet motoru ve R2 entegrasyonunun 500–1.000 kullanıcı ölçeğinde güvenliğini ve bütünlüğünü doğrulamak için hazırlanmıştır.

---

## 1. Test Senaryoları Matrisi

### Senaryo 1: Davet Kodu ve Kayıt Güvenliği
- [ ] **TC-1.1 (Geçerli Kod ile Kayıt):** Öğrenci geçerli `MHFZ-XXXX` kodu ile kayıt olur. Profilde `role = 'STUDENT'` ve ilgili `assigned_coach_id` doğru atanmalıdır.
- [ ] **TC-1.2 (Süresi Dolmuş Kod):** `expires_at < NOW()` olan bir kod ile kayıt denendiğinde işlem `400 Bad Request` ile reddedilmeli, profil oluşturulmamalıdır.
- [ ] **TC-1.3 (İptal Edilmiş Kod):** `revoked_at IS NOT NULL` olan kod reddedilmelidir.
- [ ] **TC-1.4 (Tekrar Kullanım Engeli):** Bir kez tüketilen (`used_at IS NOT NULL`) kod ile ikinci bir hesap açılamamalıdır.
- [ ] **TC-1.5 (Eşzamanlılık Yarışı - Race Condition):** Aynı davet koduyla aynı anda 2 paralel HTTP POST isteği gönderildiğinde PostgreSQL `FOR UPDATE` kilidi devreye girmeli; yalnızca 1 tanesi başarılı olmalı, diğeri reddedilmelidir.
- [ ] **TC-1.6 (Yetkisiz Rol Seçimi Engeli):** Öğrencinin kayıt payload'ında `role: "ADMIN"` göndermesi durumunda sunucu tetikleyicisi bu değeri yok saymalı, kullanıcıyı `STUDENT` olarak kaydetmelidir.

### Senaryo 2: Veri İzolasyonu ve RLS Denetimi
- [ ] **TC-2.1 (Öğrenci - Öğrenci İzolasyonu):** Öğrenci A'nın JWT'si ile `SELECT * FROM tasks WHERE user_id = 'Ogrenci_B_ID'` sorgusu çalıştırıldığında sıfır (0) satır dönmelidir.
- [ ] **TC-2.2 (Koç - Koç İzolasyonu):** Koç 1'in JWT'si ile Koç 2'ye atanmış öğrencilerin görevleri, sınavları veya mesajları listelenememelidir.
- [ ] **TC-2.3 (Veli Görüşmeleri Gizliliği):** Öğrenci JWT'si ile `SELECT * FROM parent_meetings` sorgulandığında RLS nedeniyle boş sonuç dönmelidir.
- [ ] **TC-2.4 (Finansal Veri Koruması):** Öğrenci veya Koç JWT'si ile `financial_transactions` veya `expenses` tablolarına yapılan tüm `SELECT/INSERT/UPDATE` istekleri `403 Forbidden` almalıdır.

### Senaryo 3: Görev ve XP Bütünlüğü
- [ ] **TC-3.1 (Öğrencinin Kendi Görevini Onaylama Girişimi):** Öğrenci `tasks.is_approved = true` güncellemesi yapmaya çalıştığında RLS kuralı işlemi engellemelidir.
- [ ] **TC-3.2 (Onaylı Görevin Değiştirilmezliği):** Koç tarafından onaylanmış bir görevin soru sayısı veya başlığı öğrenci tarafından değiştirilememelidir.
- [ ] **TC-3.3 (Mükerrer XP / Replay Attack Engeli):** `approve_task_and_award_xp` fonksiyonu aynı görev için arka arkaya 5 kez çağrılsa dahi `xp_ledger` tablosuna yalnızca 1 kez kayıt yazılmalı, öğrenciye sadece 1 kez XP verilmelidir.
- [ ] **TC-3.4 (Günlük XP Tavan Sınırı):** Bir öğrenciye gün içinde 500 XP'den fazla kazandıracak onaylar gönderildiğinde sistem XP'yi otomatik olarak tavanda (500) kesmelidir.
- [ ] **TC-3.5 (Ödül Bakiyesi Yetersizliği):** 1000 XP'lik bir ödülü 200 XP'si olan öğrenci talep edememelidir.

### Senaryo 4: Dosya Depolama ve R2 Yetkilendirmesi
- [ ] **TC-4.1 (Yabancı Öğrencinin Kanıtına Erişim Engeli):** Öğrenci A, Öğrenci B'nin `proofs/...` dosyasını indirmek için presigned URL istediğinde Edge Function yetki hatası (`403`) dönmelidir.
- [ ] **TC-4.2 (Presigned URL Süre Aşımı):** Üretilen yükleme URL'i 5 dakika sonra geçersiz hale gelmelidir.

---

## 2. 1.000 Öğrenci Ölçek Testi (Load Testing Stratejisi)

1. **Senaryo Dağılımı (K6 / Locust):**
   - 800 Öğrenci: Günlük görev tamamlama, kronometre çalıştırma, deneme kaydetme.
   - 150 Koç: Onay merkezi incelemesi, görev atama, veli notu girişi.
   - 50 Admin: Finansal rapor izleme, toplu davet kodu üretimi.
2. **Kritik Metrik Hedefleri:**
   - Görev tamamlama P95 gecikmesi: < 150ms
   - XP onaylama RPC P99 gecikmesi: < 200ms
   - Veritabanı bağlantı havuzu (PgBouncer): 50 aktif bağlantı altında stabilite.
