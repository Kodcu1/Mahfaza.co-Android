# Mahfaza.co V34.1 — Room SQLite V13 & Supabase Hibrit Eşitleme Mimarisi

## 1. Temel Felsefe: Offline-First (Önce Çevrimdışı)

Mahfaza.co bir eğitim ve sınav koçluğu platformudur. Öğrenci metroda, kütüphanede veya internetin çekmediği bir sınıfta soru çözerken, deneme analizi yaparken veya Pomodoro çalıştırırken **asla internet hatasıyla durdurulamaz**.

- **Yerel Okuma/Yazma:** UI bileşenleri her zaman Room veritabanından veri okur ve doğrudan Room'a yazar. Arayüzde hiçbir gecikme (zero latency) yaşanmaz.
- **Asenkron Arka Plan Eşitleme:** İnternet bağlantısı mevcut olduğunda Android `WorkManager` arka planda çalışarak yerel değişiklikleri Supabase'e aktarır ve buluttan gelen yeni görevleri Room'a yansıtır.

---

## 2. Çevrimdışı Senkronizasyon Kuyruğu (`sync_queue`)

Room V13 veritabanında yer alacak yeni `sync_queue` tablosu:

```kotlin
@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val entityType: String,      // "TASK", "EXAM", "POMODORO", "MESSAGE"
    val entityId: String,
    val operation: String,       // "INSERT", "UPDATE", "DELETE"
    val payloadJson: String,
    val idempotencyKey: String,  // Tekilleştirme anahtarı
    val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val status: String = "PENDING" // "PENDING", "IN_PROGRESS", "FAILED"
)
```

---

## 3. Çakışma Çözüm Stratejileri (Conflict Resolution)

| Veri Türü | Çakışma Senaryosu | Çözüm Kuralı (Resolution Rule) |
|:---|:---|:---|
| **Görev Tamamlama (`tasks`)** | Öğrenci çevrimdışı görevi tamamladı; koç ise aynı anda görevin soru sayısını güncelledi. | **Durum Önceliği (State Priority):** Öğrencinin `is_completed = true` ve kanıt fotoğrafı korunur; koçun metin açıklaması birleştirilir. Görev koçun onay paneline "Tamamlandı" olarak düşer. |
| **XP İşlemleri (`xp_ledger`)** | Aynı işlem çevrimdışı kuyruktan iki kez gönderildi. | **Mutlak İdempotency:** Sunucu tarafındaki `source_reference` UNIQUE kısıtı sayesinde ikinci kayıt yoksayılır (`ON CONFLICT DO NOTHING`). Asla mükerrer XP verilmez. |
| **Deneme Sınavları (`exam_results`)** | Öğrenci çevrimdışı sınav kaydetti. | **Ekleme Önceliği (Append-Only):** Her sınav yeni bir UUID ile eklenir; çakışma riski bulunmaz. |
| **Mesajlaşma (`messages`)** | Çevrimdışı yazılan mesajlar. | Gönderim zaman damgasına (`client_timestamp`) göre kronolojik sırayla kuyruktan teslim edilir. |

---

## 4. Çoklu Kullanıcı & Cihaz Güvenliği (Logout İzolasyonu)

Farklı öğrencilerin aynı aile tabletini veya telefonunu kullanma ihtimaline karşı:
1. `viewModel.logout()` çağrıldığında:
   - Mevcut kullanıcının yerel oturum token'ları güvenli depolamadan (`EncryptedSharedPreferences`) silinir.
   - Room üzerindeki aktif kullanıcıya özel önbellek kayıtları temizlenir.
   - Bir sonraki giriş yapan kullanıcı yalnızca Supabase'den kendisine ait RLS korumalı verileri çeker.
