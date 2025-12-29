// Paket nomi - bu fayl qayerda joylashganini bildiradi.
package com.example.eslatmalar

// Android konteksti bilan ishlash uchun kerak
import android.content.Context

// WorkManager kutubxonasi - fon ishlarini rejalashtirish uchun
import androidx.work.*
import java.util.concurrent.TimeUnit

// Bu object — Singleton. Ya'ni faqat bitta nusxada mavjud bo‘ladi.
object ReminderScheduler {

    // Bu funksiya kelajakdagi biror vaqtda eslatma yuborish uchun ishlatiladi
    fun scheduleReminder(context: Context, title: String, desc: String, reminderTime: Long) {

        // Nechta millisekund qoldi — hozirgi vaqt bilan reminderTime orasidagi farq
        val delay = reminderTime - System.currentTimeMillis()

        // Agar vaqt allaqachon o‘tib ketgan bo‘lsa (delay <= 0), eslatma qo‘shilmaydi
        if (delay <= 0) return

        // WorkManager ga yuboriladigan ma'lumotlar (eslatma sarlavhasi va tavsifi)
        val data = Data.Builder()
            .putString("title", title)         // "title" nomi orqali ReminderWorker ichida o‘qiladi
            .putString("description", desc)    // "description" orqali matn o‘qiladi
            .build()

        // Bitta marta ishga tushiriladigan (OneTime) ish yaratiladi
        val work = OneTimeWorkRequestBuilder<ReminderWorker>() // ReminderWorker ishini bajaradi
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)     // Belgilangan kechikish (delay) qo‘yiladi
            .setInputData(data)                                // Yuqoridagi title va description beriladi
            .build()

        // Ishni WorkManager orqali navbatga qo‘shish
        WorkManager.getInstance(context).enqueue(work)
    }
}
