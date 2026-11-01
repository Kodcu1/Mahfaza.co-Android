# Mahfaza.co V34.1 — Davet Motoru ve Güvenli Kayıt Tasarımı

## 1. Amaç ve Kapsam

500–1.000 öğrenci ölçeğinde platformun güvenliğini sağlamak için:
- Herhangi bir kullanıcının doğrudan "Koç" veya "Yönetici" olarak sisteme kayıt olması engellenmelidir.
- Öğrencinin doğru koçla eşleşmesi kayıt anında garantilenmelidir.
- Davet kodları açık metin olarak veritabanında saklanmamalıdır.
- Eşzamanlı (concurrency) kayıt denemelerinde aynı kodun iki kez kullanılması atomik düzeyde engellenmelidir.

---

## 2. Davet Kodu Yaşam Döngüsü

```
[Admin / Koç] ──> create_invitation(role, coach_id, days)
                          │
                          ▼
            [Kod Üretilir (MHFZ-A7B8)]
                          │
                          ▼ SHA-256
            [DB: invitations tablosuna code_hash yazılır]
                          │
   ┌──────────────────────┴──────────────────────┐
   │                                             │
   ▼                                             ▼
[Öğrenci Kodu Girer]                   [Süresi Dolar / İptal Edilir]
   │                                             │
   ▼ validate_invitation()                       ▼
[Geçerli Koç & Rol Gösterilir]             [Geçersiz Kılınır]
   │
   ▼ consume_invitation_atomically() (FOR UPDATE Lock)
[Kod used_at ile işaretlenir + Öğrenci Koça Bağlanır]
```

---

## 3. Güvenlik Mekanizmaları

### 3.1. SHA-256 Tek Yönlü Özetleme
- Veritabanına sızıntı olsa dahi davet kodları okunamaz.
- Kod yalnızca üretildiği an çağıran Admin/Koç kullanıcısına tek seferlik gösterilir.
- Veritabanında yalnızca `code_hash = encode(digest(code, 'sha256'), 'hex')` tutulur.

### 3.2. Eşzamanlılık Koruması (`SELECT ... FOR UPDATE`)
İki farklı cihaz aynı anda aynı davet kodunu gönderdiğinde:
```sql
SELECT id, role, assigned_coach_id 
FROM public.invitations
WHERE code_hash = v_hash AND used_at IS NULL AND revoked_at IS NULL
FOR UPDATE;
```
Satır kilitlenir; ilk transaction kodu `used_at = NOW()` yapar, ikinci transaction ise satırı kilit açıldıktan sonra okur ve "Geçersiz veya kullanılmış kod" hatası alır.

### 3.3. Zorunlu Davet Kuralı ve Atomik Rollback Koruması (Zero-Uninvited Policy)
Platformda davetsiz veya sahte hesap açılmasını tamamen engellemek için:
1. Kayıt ekranında davet kodu zorunludur. `auth.users` tablosuna INSERT yapıldığında tetiklenen `handle_new_auth_user` trigger'ı çalışır.
2. `raw_user_meta_data->>'invitation_code'` parametresi doğrulanır ve `consume_invitation_atomically` çağrılır.
3. Eğer davet kodu eksik, geçersiz, süresi dolmuş veya zaten kullanılmışsa, trigger anında `RAISE EXCEPTION` fırlatır!
4. PostgreSQL mimarisi gereği, trigger içindeki `RAISE EXCEPTION` ana `auth.users` INSERT işlemini ve tüm transaction'ı **ROLLBACK** eder.
5. Sonuç: Geçersiz davet koduyla ne `auth.users` içinde hesap açılabilir ne de `public.profiles` içinde yetim (orphaned) profil oluşabilir. Davetsiz kayıt oranı kesin olarak %0'dır.
6. Admin ve Koç rolleri istemciden seçilemez; yalnızca davet kodunun üretildiği andaki atanmış rol ve koç ID geçerli kılınır.

---

## 4. Kullanılacak RPC Fonksiyonları

1. **`public.create_invitation(p_role, p_assigned_coach_id, p_valid_days)`**
   - Yalnızca Admin veya Koç tarafından çağrılabilir.
   - 8 haneli rastgele kod üretir (`MHFZ-XXXX`).
   - Hash'i kaydeder, ham kodu döner.

2. **`public.validate_invitation(p_plain_code)`**
   - Herkese açık (Anonim çağrılabilir).
   - Kodun geçerli olup olmadığını, atanacak koçun adını ve son kullanma tarihini döner.

3. **`public.consume_invitation_atomically(p_plain_code, p_user_id)`**
   - Kayıt anında tetiklenir.
   - Kodu tüketir ve kullanıcı profilini koça bağlar.
