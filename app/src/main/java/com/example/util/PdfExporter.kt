package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import com.example.model.ExamResult
import com.example.model.ParentMeeting
import com.example.model.Task
import com.example.model.User
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    private val primaryNavy = Color.rgb(35, 59, 93)
    private val deepNavy = Color.rgb(23, 43, 77)
    private val goldAccent = Color.rgb(217, 119, 6)
    private val lightGray = Color.rgb(241, 245, 249)
    private val darkText = Color.rgb(15, 23, 42)
    private val subText = Color.rgb(71, 85, 105)
    private val successGreen = Color.rgb(5, 150, 105)
    private val borderGray = Color.rgb(226, 232, 240)

    fun exportStudent360(context: Context, student: User, exams: List<ExamResult>) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint().apply { isAntiAlias = true }

        // Top Header Banner
        paint.color = primaryNavy
        canvas.drawRect(0f, 0f, 595f, 90f, paint)

        // Gold brand stripe
        paint.color = goldAccent
        canvas.drawRect(0f, 86f, 595f, 90f, paint)

        // Brand Title
        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("MAHFAZA.CO", 40f, 44f, paint)

        paint.textSize = 11f
        paint.isFakeBoldText = false
        canvas.drawText("Akademik Eğitim Koçluğu • Öğrenci 360° Raporu", 40f, 66f, paint)

        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR"))
        val dateStr = "Rapor Tarihi: " + dateFormat.format(Date())
        paint.textSize = 9.5f
        canvas.drawText(dateStr, 400f, 66f, paint)

        var y = 115f

        // Student Overview Box
        paint.color = lightGray
        canvas.drawRoundRect(40f, y, 555f, y + 95f, 8f, 8f, paint)

        paint.color = deepNavy
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("ÖĞRENCİ KİMLİK & AKADEMİK BİLGİLERİ", 55f, y + 22f, paint)

        paint.color = darkText
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("Ad Soyad: ${student.name}", 55f, y + 42f, paint)
        canvas.drawText("E-posta: ${student.email}", 55f, y + 58f, paint)
        canvas.drawText("Eğitim Seviyesi: ${student.educationLevel?.displayName ?: "Belirtilmemiş"}", 55f, y + 74f, paint)

        canvas.drawText("Mevcut XP: ${student.currentXp} XP", 330f, y + 42f, paint)
        canvas.drawText("Toplam Kazanılan: ${student.lifetimeXp} XP", 330f, y + 58f, paint)
        canvas.drawText("Çalışma Serisi: ${student.streak} Gün", 330f, y + 74f, paint)

        y += 115f

        // Academic Trend Section Header
        paint.color = primaryNavy
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("DENEME SINAVI SONUÇLARI & NET ANALİZİ", 40f, y, paint)
        y += 16f

        // Table Header
        paint.color = lightGray
        canvas.drawRect(40f, y, 555f, y + 22f, paint)

        paint.color = deepNavy
        paint.textSize = 9.5f
        paint.isFakeBoldText = true
        canvas.drawText("Sınav Adı", 50f, y + 15f, paint)
        canvas.drawText("Tarih", 210f, y + 15f, paint)
        canvas.drawText("Ders Dağılımı", 310f, y + 15f, paint)
        canvas.drawText("Toplam Net", 480f, y + 15f, paint)

        y += 24f

        if (exams.isEmpty()) {
            paint.color = subText
            paint.textSize = 10f
            paint.isFakeBoldText = false
            canvas.drawText("Henüz sisteme girilmiş bir deneme sonucu bulunmamaktadır.", 50f, y + 18f, paint)
            y += 35f
        } else {
            paint.isFakeBoldText = false
            paint.textSize = 9.5f
            exams.takeLast(7).reversed().forEach { exam ->
                val totalNet = exam.subjectResults.sumOf { it.net }
                val subjectSummary = exam.subjectResults.take(2).joinToString(", ") { "${it.subjectName}: ${it.net}N" }

                paint.color = darkText
                canvas.drawText(exam.name.take(25), 50f, y + 14f, paint)
                paint.color = subText
                canvas.drawText(exam.date, 210f, y + 14f, paint)
                canvas.drawText(subjectSummary.take(28), 310f, y + 14f, paint)

                paint.color = primaryNavy
                paint.isFakeBoldText = true
                canvas.drawText("%.2f Net".format(totalNet), 480f, y + 14f, paint)
                paint.isFakeBoldText = false

                paint.color = borderGray
                canvas.drawLine(40f, y + 20f, 555f, y + 20f, paint)
                y += 22f
            }
        }

        y += 15f

        // General Academic Evaluation Note
        paint.color = lightGray
        canvas.drawRoundRect(40f, y, 555f, y + 70f, 6f, 6f, paint)

        paint.color = primaryNavy
        paint.textSize = 10.5f
        paint.isFakeBoldText = true
        canvas.drawText("KOÇLUK DEĞERLENDİRMESİ VE GELİŞİM PLANI", 55f, y + 20f, paint)

        paint.color = darkText
        paint.textSize = 9f
        paint.isFakeBoldText = false
        val note1 = "• Öğrencinin soru çözüm stabilitesi düzenli olarak takip edilmektedir."
        val note2 = "• Haftalık görev tamamlanma oranı hedeflenen akademik disiplin sınırları içerisindedir."
        val note3 = "• Eksik konular için etüt ve branş deneme takvimi güncellenmiştir."
        canvas.drawText(note1, 55f, y + 36f, paint)
        canvas.drawText(note2, 55f, y + 48f, paint)
        canvas.drawText(note3, 55f, y + 60f, paint)

        // Footer
        paint.color = subText
        paint.textSize = 8.5f
        canvas.drawText("Mahfaza.co Akademik Koçluk Platformu • Resmi 360° Öğrenci Raporu • mahfaza.co", 40f, 810f, paint)
        canvas.drawLine(40f, 798f, 555f, 798f, paint)

        pdfDocument.finishPage(page)

        val cleanName = student.name.replace(" ", "_")
        val file = File(context.getExternalFilesDir(null), "ogrenci_360_${cleanName}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, "Öğrenci 360° PDF oluşturuldu: ${file.name}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "PDF hatası: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    fun exportWeeklyPlan(context: Context, student: User, tasks: List<Task>) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint().apply { isAntiAlias = true }

        // Top Header Banner
        paint.color = primaryNavy
        canvas.drawRect(0f, 0f, 595f, 90f, paint)

        // Gold brand stripe
        paint.color = goldAccent
        canvas.drawRect(0f, 86f, 595f, 90f, paint)

        // Title
        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("MAHFAZA.CO", 40f, 44f, paint)

        paint.textSize = 11f
        paint.isFakeBoldText = false
        canvas.drawText("Haftalık Akademik Çalışma ve Görev Programı", 40f, 66f, paint)

        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR"))
        val dateStr = "Tarih: " + dateFormat.format(Date())
        paint.textSize = 9.5f
        canvas.drawText(dateStr, 440f, 66f, paint)

        var y = 110f

        // Student Info & Stats Box
        paint.color = lightGray
        canvas.drawRoundRect(40f, y, 555f, y + 65f, 8f, 8f, paint)

        paint.color = primaryNavy
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("ÖĞRENCİ: ${student.name.uppercase(Locale("tr", "TR"))} (${student.educationLevel?.displayName ?: ""})", 55f, y + 22f, paint)

        paint.color = darkText
        paint.textSize = 9.5f
        paint.isFakeBoldText = false
        val totalQ = tasks.sumOf { it.targetQuestionCount }
        val completedQ = tasks.sumOf { it.completedQuestionCount }
        val totalXp = tasks.sumOf { it.xpReward }
        val completedTasks = tasks.count { it.isCompleted }

        canvas.drawText("Toplam Planlanan Soru: $totalQ Soru", 55f, y + 42f, paint)
        canvas.drawText("Tamamlanan Soru: $completedQ Soru", 55f, y + 54f, paint)
        canvas.drawText("Toplam Görev Sayısı: ${tasks.size} (${completedTasks} Tamamlandı)", 300f, y + 42f, paint)
        canvas.drawText("Toplam Kazanılabilir XP: $totalXp XP", 300f, y + 54f, paint)

        y += 85f

        // Days of Week Table
        val dayNames = listOf(
            1 to "Pazartesi",
            2 to "Salı",
            3 to "Çarşamba",
            4 to "Perşembe",
            5 to "Cuma",
            6 to "Cumartesi",
            7 to "Pazar"
        )

        dayNames.forEach { (dayIndex, dayName) ->
            val dayTasks = tasks.filter { it.dayOfWeek == dayIndex }

            // Day Header Strip
            paint.color = deepNavy
            canvas.drawRoundRect(40f, y, 555f, y + 20f, 4f, 4f, paint)

            paint.color = Color.WHITE
            paint.textSize = 10f
            paint.isFakeBoldText = true
            canvas.drawText(dayName.uppercase(Locale("tr", "TR")), 50f, y + 14f, paint)

            val dayTotalQ = dayTasks.sumOf { it.targetQuestionCount }
            paint.textSize = 8.5f
            paint.isFakeBoldText = false
            canvas.drawText("${dayTasks.size} Görev • $dayTotalQ Soru Hedefi", 420f, y + 14f, paint)

            y += 24f

            if (dayTasks.isEmpty()) {
                paint.color = subText
                paint.textSize = 9f
                canvas.drawText("— Bu gün için planlanmış görev bulunmuyor (Serbest Çalışma / Tekrar) —", 55f, y + 12f, paint)
                y += 18f
            } else {
                dayTasks.forEach { task ->
                    paint.color = darkText
                    paint.textSize = 9f
                    paint.isFakeBoldText = true
                    canvas.drawText("• ${task.subject}: ${task.title}", 55f, y + 12f, paint)

                    paint.isFakeBoldText = false
                    paint.color = subText
                    val statusText = if (task.isApproved) "✓ Koç Onaylı" else if (task.isCompleted) "✓ Tamamlandı" else "○ Bekliyor"
                    val detail = "${task.topic} | Hedef: ${task.targetQuestionCount} Soru | ${task.xpReward} XP | $statusText"
                    canvas.drawText(detail, 310f, y + 12f, paint)

                    paint.color = borderGray
                    canvas.drawLine(55f, y + 16f, 545f, y + 16f, paint)
                    y += 18f
                }
            }
            y += 6f
        }

        // Footer
        paint.color = subText
        paint.textSize = 8.5f
        canvas.drawText("Mahfaza.co Akademik Haftalık Planlama Çizelgesi • Disiplinini Koru, Hedefine Ulaş.", 40f, 810f, paint)
        canvas.drawLine(40f, 798f, 555f, 798f, paint)

        pdfDocument.finishPage(page)

        val cleanName = student.name.replace(" ", "_")
        val file = File(context.getExternalFilesDir(null), "haftalik_program_${cleanName}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, "Haftalık Program PDF oluşturuldu: ${file.name}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "PDF hatası: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    fun exportParentMeeting(context: Context, meeting: ParentMeeting, student: User?) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint().apply { isAntiAlias = true }

        // Top Header Banner
        paint.color = primaryNavy
        canvas.drawRect(0f, 0f, 595f, 90f, paint)

        // Gold brand stripe
        paint.color = goldAccent
        canvas.drawRect(0f, 86f, 595f, 90f, paint)

        // Title
        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("MAHFAZA.CO", 40f, 44f, paint)

        paint.textSize = 11f
        paint.isFakeBoldText = false
        canvas.drawText("Resmi Veli Görüşme ve Akademik Değerlendirme Raporu", 40f, 66f, paint)

        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR"))
        val dateStr = "Rapor Tarihi: " + dateFormat.format(Date())
        paint.textSize = 9.5f
        canvas.drawText(dateStr, 400f, 66f, paint)

        var y = 115f

        // Meeting Summary Card
        paint.color = lightGray
        canvas.drawRoundRect(40f, y, 555f, y + 80f, 8f, 8f, paint)

        paint.color = deepNavy
        paint.textSize = 11.5f
        paint.isFakeBoldText = true
        canvas.drawText("GÖRÜŞME VE KATILIMCI BİLGİLERİ", 55f, y + 22f, paint)

        paint.color = darkText
        paint.textSize = 9.5f
        paint.isFakeBoldText = false
        canvas.drawText("Öğrenci Adı: ${meeting.studentName}", 55f, y + 42f, paint)
        canvas.drawText("Görüşme Tarihi: ${meeting.date}", 55f, y + 58f, paint)
        canvas.drawText("Eğitim Kademesi: ${student?.educationLevel?.displayName ?: "LGS / YKS"}", 55f, y + 70f, paint)

        val statusText = if (meeting.isCompleted) "Tamamlandı (Onaylandı)" else "Planlandı (Bekliyor)"
        canvas.drawText("Görüşme Durumu: $statusText", 320f, y + 42f, paint)
        canvas.drawText("Bir Sonraki Görüşme: ${meeting.nextMeetingDate.ifEmpty { "Belirlenmedi" }}", 320f, y + 58f, paint)

        y += 100f

        // Topic Section
        paint.color = primaryNavy
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("GÖRÜŞME KONUSU VE ODAK NOKTALARI", 40f, y, paint)
        y += 16f

        paint.color = lightGray
        canvas.drawRoundRect(40f, y, 555f, y + 36f, 6f, 6f, paint)
        paint.color = darkText
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText(meeting.topic, 55f, y + 22f, paint)

        y += 54f

        // Notes Section
        paint.color = primaryNavy
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("GÖRÜŞME DETAYLARI VE AKADEMİK DEĞERLENDİRME", 40f, y, paint)
        y += 16f

        val notesBoxTop = y
        paint.color = lightGray
        canvas.drawRoundRect(40f, notesBoxTop, 555f, notesBoxTop + 130f, 6f, 6f, paint)

        paint.color = darkText
        paint.textSize = 9.5f
        paint.isFakeBoldText = false
        val notesContent = meeting.notes.ifEmpty { "Görüşme notu bulunmuyor." }
        drawWrappedText(canvas, notesContent, 55f, notesBoxTop + 22f, 480f, paint, 15f)

        y = notesBoxTop + 145f

        // Decisions / Action Items Section
        paint.color = primaryNavy
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("ALINAN KARARLAR VE ÖĞRENCİ EYLEM PLANI", 40f, y, paint)
        y += 16f

        val decBoxTop = y
        paint.color = lightGray
        canvas.drawRoundRect(40f, decBoxTop, 555f, decBoxTop + 110f, 6f, 6f, paint)

        paint.color = darkText
        paint.textSize = 9.5f
        paint.isFakeBoldText = false
        val decContent = meeting.decisions.ifEmpty { "Belirtilen somut eylem kararı bulunmuyor." }
        drawWrappedText(canvas, decContent, 55f, decBoxTop + 22f, 480f, paint, 15f)

        y = decBoxTop + 130f

        // Signature Blocks
        paint.color = borderGray
        canvas.drawLine(55f, y + 50f, 230f, y + 50f, paint)
        canvas.drawLine(360f, y + 50f, 535f, y + 50f, paint)

        paint.color = subText
        paint.textSize = 9f
        canvas.drawText("Eğitim Koçu İmzası", 100f, y + 64f, paint)
        canvas.drawText("Veli Adı & İmzası", 415f, y + 64f, paint)

        // Footer
        paint.color = subText
        paint.textSize = 8.5f
        canvas.drawText("Mahfaza.co Akademik Koçluk Platformu • Resmi Veli İletişim Arşivi • mahfaza.co", 40f, 810f, paint)
        canvas.drawLine(40f, 798f, 555f, 798f, paint)

        pdfDocument.finishPage(page)

        val cleanName = meeting.studentName.replace(" ", "_")
        val file = File(context.getExternalFilesDir(null), "veli_gorusme_${cleanName}_${meeting.date}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, "Veli Görüşme PDF oluşturuldu: ${file.name}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "PDF hatası: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    private fun drawWrappedText(canvas: Canvas, text: String, x: Float, startY: Float, maxWidth: Float, paint: Paint, lineHeight: Float = 14f): Float {
        var curY = startY
        val paragraphs = text.split("\n")
        for (paragraph in paragraphs) {
            val words = paragraph.split(" ")
            var line = StringBuilder()
            for (w in words) {
                val testLine = if (line.isEmpty()) w else "$line $w"
                if (paint.measureText(testLine) > maxWidth) {
                    canvas.drawText(line.toString(), x, curY, paint)
                    curY += lineHeight
                    line = StringBuilder(w)
                } else {
                    line = StringBuilder(testLine)
                }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line.toString(), x, curY, paint)
                curY += lineHeight
            }
        }
        return curY
    }
}
