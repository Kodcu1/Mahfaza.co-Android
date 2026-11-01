# Mahfaza.co V34.1 — Cloudflare R2 Özel Depolama Mimarisi

## 1. Mimari Prensipler

1. **Sıfır İstemci Gizli Anahtarı (Zero-Secret Client):**
   - Cloudflare R2 `AccessKeyId` veya `SecretAccessKey` değerleri **kesinlikle** Android APK içine gömülmez.
   - Android istemcisi dosya işlemlerini doğrudan R2 API anahtarlarıyla değil, sunucu tarafından üretilen **kısa süreli imzalı URL'ler (Presigned URLs)** üzerinden yürütür.

2. **Özel Bucket (Private Bucket):**
   - Bucket: `mahfaza-assets-private`
   - Public erişim ve doğrudan listeleme tamamen kapalıdır.
   - Öğrencilerin deneme sınavı fotoğrafları, çalışma kanıtları ve veli raporları internete açık URL'lerle barındırılamaz.

---

## 2. Dosya Yükleme Akışı (Presigned PUT)

```
[Android İstemci]                     [Supabase Edge Function]               [Cloudflare R2]
       │                                         │                                  │
       │ 1. POST /functions/v1/r2-presign        │                                  │
       │    (Bearer JWT, entity_type, task_id)   │                                  │
       │────────────────────────────────────────>│                                  │
       │                                         │ 2. JWT & Koç/Öğrenci             │
       │                                         │    Yetkisi Doğrulanır            │
       │                                         │ 3. S3 Presigned PUT URL Üretilir │
       │                                         │    (Geçerlilik: 5 Dakika)        │
       │<────────────────────────────────────────│                                  │
       │ 4. { upload_url, object_key } döner     │                                  │
       │                                                                            │
       │ 5. HTTP PUT (Dosya binary doğrudan R2'ye yüklenir)                         │
       │───────────────────────────────────────────────────────────────────────────>│
       │                                                                            │
       │ 6. Başarılı ise PostgreSQL'e metadata yazılır                              │
       │    (file_metadata & tasks.photo_proof_url = object_key)                   │
```

---

## 3. Nesne Anahtarı (Object Key) Standardı

Dosyalar rastgele isimlerle değil, erişim denetimini kolaylaştıracak hiyerarşik dizin yapısıyla saklanır:

- **Profil Avatarları:** `avatars/{user_id}.jpg`
- **Görev Kanıt Fotoğrafları:** `proofs/{student_id}/{task_id}_{timestamp}.jpg`
- **Akademik PDF Raporları:** `reports/{student_id}/{report_type}_{timestamp}.pdf`

---

## 4. Dosya Doğrulama ve Sınırları

Sunucu tarafı Presigned URL üretilmeden önce katı kontroller uygulanır:
- **Azami Dosya Boyutu:**
  - Avatarlar: 2 MB
  - Görev Kanıt Fotoğrafları: 5 MB
  - PDF Raporları: 15 MB
- **İzin Verilen MIME Türleri:**
  - Fotoğraf: `image/jpeg`, `image/png`, `image/webp`
  - Belge: `application/pdf`
- **Erişim Seviyesi:**
  - Yalnızca ilgili öğrenci ve o öğrenciye atanmış koç (`is_coach_of(student_id)`) veya Admin dosyaya erişim için indirme URL'i talep edebilir.
