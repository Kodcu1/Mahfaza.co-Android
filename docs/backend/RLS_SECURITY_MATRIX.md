# Mahfaza.co V34.1 — Row Level Security (RLS) Matrix

Bu belge, Mahfaza.co PostgreSQL veritabanındaki tüm tablolara uygulanan sıfır-güven (Zero-Trust) yetki kurallarını ve rol bazlı izolasyon matrisini detaylandırır.

---

## 1. Yetki ve Rol Hiyerarşisi

Sistemde 3 temel kullanıcı rolü bulunur:
1. **STUDENT (Öğrenci):** Yalnızca kendi akademik kayıtlarına erişebilir. Başka öğrencilerin verilerini göremez.
2. **COACH (Koç):** Kendisine atanmış (`assigned_coach_id = auth.uid()`) öğrencilerin akademik süreçlerini, görevlerini, sınavlarını ve veli görüşmelerini yönetebilir. Başka koçların öğrencilerini göremez.
3. **ADMIN (Yönetici):** Tüm platform tablolarına, kullanıcı yönetimine, finansal verilere ve sistem ayarlarına tam erişim yetkisine sahiptir.

---

## 2. RLS Matrisi Tablosu

| Tablo Adı | STUDENT İzni | COACH İzni | ADMIN İzni | Özel Güvenlik Kısıtlaması |
|:---|:---|:---|:---|:---|
| **`profiles`** | SELECT (Kendi profili)<br>UPDATE (Kendi profili, rol hariç) | SELECT (Kendi profili + Kendi öğrencileri)<br>UPDATE (Kendi profili) | FULL ACCESS (Tüm profiller) | Rol ve paket değişikliği istemciden yapılamaz; veritabanı trigger/RPC korumalıdır. |
| **`tasks`** | SELECT (Kendi görevleri)<br>INSERT (Kendi görevleri)<br>UPDATE (Sadece onaysızlar) | SELECT (Kendi öğrencilerinin görevleri)<br>INSERT (Öğrencisine görev atama)<br>UPDATE (Onaylama/Reddetme/Arşivleme) | FULL ACCESS | Öğrenci onaylanmış görevin içeriğini veya onay durumunu değiştiremez. |
| **`exam_results`** | SELECT (Kendi denemeleri)<br>INSERT/UPDATE (Kendi denemeleri) | SELECT (Öğrencisinin denemeleri)<br>INSERT (Öğrenci adına deneme girme) | FULL ACCESS | Sınav netleri ve ders dökümleri koç-öğrenci ikilisi dışında tamamen izoledir. |
| **`subject_results`**| SELECT/INSERT (İlgili sınavın sahibi) | SELECT/INSERT (İlgili öğrencinin koçu) | FULL ACCESS | Üst `exam_results` tablosunun RLS kontrolüne bağlıdır. |
| **`xp_ledger`** | SELECT (Kendi geçmişi)<br>INSERT: YASAK (Sadece RPC) | SELECT (Öğrencisinin XP geçmişi)<br>INSERT: YASAK (Sadece RPC) | SELECT / INSERT (Düzeltme) | Değiştirilemez (Immutable) defter. UPDATE ve DELETE kimseye açık değildir. |
| **`pomodoro_sessions`** | SELECT/INSERT (Kendi oturumları) | SELECT (Öğrencisinin oturumları) | FULL ACCESS | - |
| **`messages`** | SELECT/INSERT (Gönderen veya Alıcı ise) | SELECT/INSERT (Gönderen veya Alıcı ise) | FULL ACCESS | Üçüncü şahıslar mesajları göremez. Yalnızca alıcı `is_read` alanını güncelleyebilir. |
| **`notifications`** | SELECT/UPDATE (Sadece Alıcı ise) | SELECT/UPDATE (Sadece Alıcı ise) | FULL ACCESS | Bildirimler başka kullanıcılara görünmez. |
| **`rewards`** | SELECT (Tüm aktif liste) | SELECT (Tüm aktif liste) | FULL ACCESS (Ekle/Sil/Düzenle) | Yalnızca Admin ödül tanımlayabilir veya stok güncelleyebilir. |
| **`reward_requests`**| SELECT/INSERT (Kendi talebi) | SELECT/UPDATE (Öğrencisinin talebini onayla) | FULL ACCESS | Öğrenci talep açar, koç onaylar. Çift onay engellenmiştir. |
| **`parent_meetings`**| **ERİŞİM YOK (KAPALI)** | FULL ACCESS (Kendi öğrencileri için) | FULL ACCESS | **Öğrencinin veli görüşme notlarını görmesi kesinlikle yasaklanmıştır.** |
| **`financial_transactions`** | **ERİŞİM YOK (KAPALI)** | **ERİŞİM YOK (KAPALI)** | FULL ACCESS | Finansal gelir, gider ve muhasebe dökümleri yalnızca Admin rolüne açıktır. |
| **`package_configs`**| SELECT (Katalog fiyatları) | SELECT (Katalog fiyatları) | FULL ACCESS | Paket ve fiyat düzenleme yetkisi sadece Admin'dedir. |
| **`reminder_settings`**| FULL ACCESS (Kendi ayarları) | ERİŞİM YOK | FULL ACCESS | Kişisel alarm ve hatırlatıcı ayarları kullanıcıya özeldir. |
| **`invitations`** | ERİŞİM YOK (Doğrulama public RPC ile) | SELECT (Kendi oluşturduğu davetler) | FULL ACCESS | Davet kodları açık metin tutulmaz, SHA-256 ile korunur. |
| **`file_metadata`** | SELECT (Kendi dosyaları)<br>INSERT (Yetkili yüklemeler) | SELECT (Öğrencisinin kanıt ve belgeleri) | FULL ACCESS | R2 presigned URL üretilmeden doğrudan dosya okunamaz. |
| **`sync_operations`**| SELECT/INSERT (Kendi işlem kayıtları) | ERİŞİM YOK | FULL ACCESS | Çevrimdışı eşitleme çakışmalarını izler. |
| **`system_settings`**| SELECT (Sistem ayarları okunabilir) | SELECT (Sistem ayarları okunabilir) | FULL ACCESS | İstemciden düzenlenemez; yalnızca Admin değiştirebilir. |

---

## 3. Güvenlik Tasarım Kuralları ve V34.2 Sıkılaştırmaları

1. **İstemci Beyanına Güvenmeme İlkesi (No Client Trust):** RLS kuralları JWT içerisindeki `user_metadata` yerine `public.profiles` tablosundaki doğrulanmış `role` sütununu `get_auth_user_role()` fonksiyonu üzerinden sorgular.
2. **Sabit `search_path` (Search Path Injection Koruması):** Bütün `SECURITY DEFINER` fonksiyonları `SET search_path = public, auth` direktifi ile çalıştırılarak kötü niyetli şema manipülasyonları engellenir.
3. **Yetkisiz Rol Yükseltme ve XP Manipülasyon Koruması (`guard_profile_immutable_fields`):**
   - `profiles` tablosuna `BEFORE UPDATE` trigger bağlanmıştır.
   - Admin olmayan kullanıcıların `role`, `current_xp`, `lifetime_xp`, `package_id`, `assigned_coach_id` ve `streak` alanlarını doğrudan UPDATE etmesi veritabanı motoru seviyesinde engellenmiştir (`RAISE EXCEPTION`).
4. **Öğrenci Görev Ekleme Sıkılaştırması (`tasks_insert_policy`):**
   - Öğrenciler görev eklerken `is_approved = false`, `is_rejected = false` ve `is_archived = false` olmak zorundadır.
   - Kendi ekledikleri görevlerin `xp_reward` değeri azami sistem çalışma tavanı (`max_question_xp = 40`) ile sınırlandırılmıştır; kendi kendilerine yüksek XP tanımlayamazlar.
5. **Eşzamanlılık Korumalı Görev Onayı ve Günlük XP Tavanı:**
   - `approve_task_and_award_xp` fonksiyonunda görev kilitlendikten hemen sonra öğrencinin `profiles` satırı `FOR UPDATE` ile kilitlenir.
   - Böylece aynı öğrencinin iki farklı görevi paralel olarak onaylansa dahi günlük XP tavanı (`xp_daily_cap = 500`) aşılamaz; işlemler kesin olarak sıraya dizilir.
6. **Fonksiyon Çalıştırma Yetkilerinin Sınırlandırılması:**
   - `public` şemasındaki kritik fonksiyonların varsayılan `EXECUTE ON ALL TO PUBLIC` izni `REVOKE` edilerek yalnızca `authenticated` rolüne devredilmiştir.
